package com.zer0.historyprice;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Module implements IXposedHookLoadPackage{
    private static final String PACKAGE_NAME = "com.jingdong.app.mall";

    private String shareUrl;
    private String btnAdded = "button_added";
    private String clicked = "item_clicked";
    private Activity shareActivity;
    private static final String SHARE_ACTIVITY_NAME = "com.jingdong.app.mall.basic.ShareActivity";
    private static final String XPOSED_BRIDGE = "de.robv.android.xposed.XposedBridge";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(PACKAGE_NAME)){
            return;
        }
        LogUtil.log(lpparam.packageName);

        XposedHelpers.findAndHookMethod(Class.class, "forName",
                String.class, boolean.class, ClassLoader.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        LogUtil.log("Class.forName: "+param.args[0]);
                        if ("de.robv.android.xposed.XposedHelpers".equals(param.args[0])){
                            LogUtil.log("Class.forName: " + param.args[0]);
                            param.setResult(null);
                        }
                    }
                });


        XposedHelpers.findAndHookMethod(ClassLoader.class, "loadClass", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.hasThrowable()) return;
                Class<?> cls = (Class<?>) param.getResult();
                if (cls != null && cls.getName().contains(XPOSED_BRIDGE)) {
                    LogUtil.log("try to load " + XPOSED_BRIDGE);
                    param.setResult(null);
                }
            }
        });


        XposedHelpers.findAndHookMethod("com.jd.stat.security.jma.JMA",
                lpparam.classLoader, "needXposedDialog", new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        LogUtil.log("let needXposedDialog return false");
                        return false;
                    }
                });


        XposedHelpers.findAndHookMethod(Activity.class,
                "findViewById", int.class, new XC_MethodHook(){
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                        Activity activity = (Activity) param.thisObject;
//                        LogUtil.log("activity: " + activity);
                        if (!activity.toString().contains(SHARE_ACTIVITY_NAME) ||
                                XposedHelpers.getAdditionalInstanceField(activity, btnAdded) != null){
                            return;
                        }
                        shareActivity = activity;
                        LogUtil.log("shareActivity:  " + shareActivity);

                        View view = (View) param.getResult();
                        if (view.getClass().toString().contains("JDGridView")){
                            Field list = XposedHelpers.findFirstFieldByExactType(activity.getClass(), List.class);
                            list.setAccessible(true);
                            try {
                                // 添加按钮
                                List<Map<String, Object>> shareChannels = (List) list.get(param.thisObject);
                                Map<String, Object> lastView = shareChannels.get(shareChannels.size() - 1);
                                LogUtil.log("lastView: " + lastView);

                                Map<String, Object> map = new HashMap<>();
                                map.put("text", "其他");
                                map.put("channel", "Other");
                                map.put("image", lastView.get("image"));
                                shareChannels.add(map);
                                XposedHelpers.setAdditionalInstanceField(activity, btnAdded, true);
                                LogUtil.log("added share button");


                                // 获取分享链接
                                Class clazz = XposedHelpers.findClassIfExists(
                                        "com.jingdong.common.entity.ShareInfo", lpparam.classLoader);
                                Field field = XposedHelpers.findFirstFieldByExactType(activity.getClass() ,clazz);
                                field.setAccessible(true);
                                shareUrl = (String) XposedHelpers.callMethod(
                                        field.get(activity), "getUrl");;
                                LogUtil.log("shareUrl:" + shareUrl);

                            }catch (Exception e){
                                LogUtil.log("JD shareActivity error: " + e.getMessage());
                                e.printStackTrace();
                            }

                        }
                    }
                });

        XposedHelpers.findAndHookMethod(AdapterView.class, "setOnItemClickListener",
                AdapterView.OnItemClickListener.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        LogUtil.log("item clicked: " + lpparam.classLoader + ", " + Arrays.toString(param.args));
                        Object itemClickListener = param.args[0];
                        LogUtil.log("ItemClickListener class: " + itemClickListener.getClass().getName());
                        XposedHelpers.findAndHookMethod(itemClickListener.getClass(), "onItemClick",
                                AdapterView.class, View.class, int.class, long.class, new XC_MethodHook() {
                                    @Override
                                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                        if (XposedHelpers.getAdditionalInstanceField(shareActivity, clicked) != null){
                                            return;
                                        }

                                        GridView gridView = (GridView) param.args[0];
                                        int position = (int) param.args[2];
                                        if (position == gridView.getAdapter().getCount() - 1){
                                            XposedHelpers.setAdditionalInstanceField(shareActivity, clicked, true);
                                            Intent shareIntent = new Intent();
                                            shareIntent.setAction(Intent.ACTION_SEND);
                                            shareIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
                                            shareIntent.setType("text/plain");
                                            shareActivity.startActivity(shareIntent);
                                        }
                                    }
                                });
                    }
                });

    }

}

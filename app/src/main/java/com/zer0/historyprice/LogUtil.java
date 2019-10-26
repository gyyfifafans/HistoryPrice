package com.zer0.historyprice;

import de.robv.android.xposed.XposedBridge;

public class LogUtil {

    private static boolean debug = BuildConfig.DEBUG;

    public static void log(String text){
        if (debug){
            XposedBridge.log("HP.Hook " + text);
        }
    }
}

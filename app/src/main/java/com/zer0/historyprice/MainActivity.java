package com.zer0.historyprice;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class MainActivity extends Activity {

    private Progress mProgress;
    private WebView mWebView;

    private String js = "var skip = false;" +
            "    [].forEach.call(document.querySelectorAll('div'),function(v){" +
            "        if(v.className === 'spinfo')skip = true;" +
            "        if(!skip && v.className != 'lijg-box')v.remove();" +
            "        if(v.className === 'trend_remark')skip = false;" +
            "    });";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = findViewById(R.id.web_view);

        mProgress = new Progress(MainActivity.this);
        mProgress.setMessage(getString(R.string.loading_msg));
        mProgress.setCancelable(false);
        mProgress.show();

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setAppCacheEnabled(true);
        webSettings.setBlockNetworkImage(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if ((Intent.ACTION_SEND.equals(action)) && (type != null) && ("text/plain".equals(type))) {
            String url = intent.getStringExtra(Intent.EXTRA_TEXT);
            String api = "http://tool.manmanbuy.com/m/history.aspx?url=" + url;
            mWebView.loadUrl(api);
        }

        //        String api = "http://tool.manmanbuy.com/m/history.aspx?url=https://item.m.jd.com/product/3759005.html";
        //        mWebView.loadUrl(api);
        mWebView.setVisibility(View.INVISIBLE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:(document.getElementsByClassName(\"t\")[0]).remove()");
                view.evaluateJavascript(js, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        mWebView.setVisibility(View.VISIBLE);
                        closeDialog();
                    }
                });
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (!url.contains(".manmanbuy.com/") || url.contains("/favicon.ico")){
                    return new WebResourceResponse("", "", null);
                }
                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDialog();
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeDialog();
    }

    private void closeDialog(){
        if (mProgress != null && mProgress.isShowing()){
            mProgress.dismiss();
        }
    }
}

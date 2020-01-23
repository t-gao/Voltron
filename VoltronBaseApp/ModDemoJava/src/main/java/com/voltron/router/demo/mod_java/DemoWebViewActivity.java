package com.voltron.router.demo.mod_java;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.voltron.router.annotation.EndPoint;

@EndPoint("/main/webview")
public class DemoWebViewActivity extends AppCompatActivity {
    private WebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mWebView = findViewById(R.id.wv_show_web);
        initWebView();
        String url = getIntent().getStringExtra("url");
        if (!TextUtils.isEmpty(url)){
            mWebView.loadUrl(url);
        }
    }

    private void initWebView() {
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
    }
}

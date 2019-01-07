package com.voltron.demo.app;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.voltron.router.annotation.EndPoint;
import com.voltron.router.api.VRouter;

@EndPoint(scheme = "hfqdl", host = "m.haofenqi.com")
public class SchemeFilterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = getIntent().getData();
        String deepLink = getIntent().getStringExtra(Constants.DeepLinks.DEEPLINK);
        if (uri == null) {
            if (TextUtils.isEmpty(deepLink)) {
                finish();
            }
            processInternalDispatch(deepLink);
        } else {
            processOutterDispatch(uri);
        }
    }

    // 处理内部DEEPLINK跳转
    private void processInternalDispatch(String deepLink) {
        if (deepLink.startsWith("http")) {
            VRouter.with(this)
                    .path("/main/webview")
                    .stringExtra("url", deepLink)
                    .go();
        } else {
            VRouter.with(this)
                    .path(deepLink)
                    .go();
        }
        finish();
    }

    // 处理不同类型外部Uri的跳转 - 目前先直接跳转至对应Activity，不经过SplashActivity
    private void processOutterDispatch(Uri uri) {
        String scheme = uri.getScheme();
        if (TextUtils.isEmpty(scheme)) {
            return;
        }
        switch (scheme) {
            case Constants.DeepLinks.SCHEME_HFQDL:
                String path = uri.getPath();
                VRouter.with(this)
                        .path(path)
                        .go();
                break;
            case Constants.DeepLinks.SCHEME_HTTP:
            case Constants.DeepLinks.SCHEME_HTTPS:
                VRouter.with(this)
                        .path("/main/webview")
                        .stringExtra("url", uri.toString())
                        .go();
                break;
        }
        finish();
    }
}

package com.voltron.demo.app;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.voltron.router.annotation.EndPoint;
import com.voltron.router.api.Postcard;
import com.voltron.router.api.VRouter;

import java.util.Map;
import java.util.Set;

@EndPoint("/main/dispatch")
public class SchemeFilterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = getIntent().getData();
        if (uri == null) {
            // 内部跳转
            Bundle extrasData = getIntent().getExtras();
            String deepLink = "";
            if (extrasData == null || TextUtils.isEmpty(deepLink = extrasData.getString(Constants.DeepLinks.DEEPLINK))) {
                finish();
                return;
            }
            Map<String, String> params = (Map<String, String>) extrasData.getSerializable(Constants.DeepLinks.PARAMS);
            processInternalDispatch(deepLink, params);
        } else {
            processOutterDispatch(uri);
        }
    }

    // 处理内部DEEPLINK跳转
    private void processInternalDispatch(String deepLink, Map<String, String> params) {
        if (deepLink.startsWith("http")) {
            VRouter.with(this)
                    .path("/main/webview")
                    .stringExtra("url", deepLink)
                    .go();
        } else {
            if (params == null || params.size() == 0) {
                VRouter.with(this)
                        .path(deepLink).go();
            } else {
                Postcard.Builder builder = VRouter.with(this)
                        .path(deepLink);
                // 将携带过来的参数带入
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    builder.stringExtra(entry.getKey(), entry.getValue());
                }
                builder.go();
            }
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
            case Constants.DeepLinks.SCHEME_HTTP:
            case Constants.DeepLinks.SCHEME_HTTPS:
                VRouter.with(this)
                        .path("/main/webview")
                        .stringExtra("url", uri.toString())
                        .go();
                break;
            default:
                String path = uri.getPath();
                Set<String> queryNames = uri.getQueryParameterNames();
                Postcard.Builder builder = VRouter.with(this)
                        .path(path);
                if (queryNames == null || queryNames.size() == 0) {
                    builder.go();
                } else {
                    for (String name : queryNames) {
                        builder.stringExtra(name, uri.getQueryParameter(name));
                    }
                    builder.go();
                }
                break;
        }
        finish();
    }
}

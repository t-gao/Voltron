package com.voltron.router.api;

import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.voltron.router.base.AnnotationUtil;

import java.util.ArrayList;

class PostcardInternal {
    Context context;
    Fragment fragment;

    String scheme;
    String host;
    String path;
    String route;
    Bundle extras;
    int intentFlags;
    boolean forResult;
    int requestCode = -1;

    boolean bindService = false; //作用于bindservice, 默认非bind启动
    ServiceConnection conn = null; //作用于bindservice
    int flags ; //作用于bindservice

    private ArrayList<Interceptor> interceptors = new ArrayList<>();
    Interceptor.Chain interceptorChain;

    PostcardInternal(Context context, Fragment fragment, String path, Bundle extras, int intentFlags,
                     boolean forResult, int requestCode) {

        this.context = context;
        this.fragment = fragment;
        this.path = path;
        this.extras = extras;
        this.intentFlags = intentFlags;
        this.forResult = forResult;
        this.requestCode = requestCode;
    }

    PostcardInternal(Context context) {
        this.context = context;
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    String getPath() {
        return path;
    }

    public String getRoute() {
        if (TextUtils.isEmpty(route)) {
            route = AnnotationUtil.buildRouteFromSchemeHostPath(scheme, host, path);
        }
        return route;
    }

    public boolean isForResult() {
        return forResult;
    }

    public int getRequestCode() {
        return requestCode;
    }

    Context getContext() {
        return context;
    }

    Fragment getFragment() {
        return fragment;
    }

    String getGroup() {
        String group = AnnotationUtil.extractGroupNameFromRoute(route);
        if (TextUtils.isEmpty(group)) {
            group = AnnotationUtil.extractGroupNameFromSchemeHost(scheme, host);
        }
        return group;
    }

    Bundle getExtras() {
        return extras;
    }

    int getIntentFlags() {
        return intentFlags;
    }

    @NonNull
    Bundle myExtras() {
        Bundle ext = this.extras;
        if (ext == null) {
            ext = new Bundle();
            this.extras = ext;
        }
        return ext;
    }

    void addInterceptor(Interceptor interceptor) {
        this.interceptors.add(interceptor);
    }

    void setIntentFlags(int flags) {
        intentFlags = flags;
    }

    void setRoute(String route) {
        this.route = route;
    }

    void addIntentFlags(int flags) {
        intentFlags |= flags;
    }

    void forResult(int requestCode) {
        this.forResult = true;
        this.requestCode = requestCode;
    }

    void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    void setContext(Context context) {
        this.context = context;
    }

    void setScheme(String scheme) {
        this.scheme = scheme;
    }

    void setHost(String host) {
        this.host = host;
    }

    void setPath(String path) {
        this.path = path;
    }

    void setExtras(Bundle value) {
        this.extras = value;
    }

    void make(Postcard postcard) {
        this.interceptorChain = new VRouterInterceptorChain(postcard, this.interceptors);
    }

    boolean go() {
        return VRouterInternal.go(this);
    }

    void removeInterceptor(Interceptor interceptor) {
        this.interceptors.remove(interceptor);
    }

    void clearInterceptors() {
        this.interceptors.clear();
    }

    void clearExtras() {
        this.extras = null;
    }

    boolean hasExtra(String key) {
        return this.extras != null && this.extras.containsKey(key);
    }
}

package com.voltron.router.api;

import android.app.Activity;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.core.util.Pair;
import android.text.TextUtils;
import android.view.View;

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
    Bundle options;
    int intentFlags;
    boolean forResult;
    int requestCode = -1;

    Integer overrideEnterAnim = null;
    Integer overrideExitAnim = null;

    boolean bindService = false; //作用于bindservice, 默认非bind启动
    ServiceConnection conn = null; //作用于bindservice
    int flags ; //作用于bindservice

    private ArrayList<Interceptor> interceptors = new ArrayList<>();
    Interceptor.Chain interceptorChain;
    private NavCallback callback;

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

    String getScheme() {
        return scheme;
    }

    String getHost() {
        return host;
    }

    String getPath() {
        return path;
    }

    String getRoute() {
        if (TextUtils.isEmpty(route)) {
            route = AnnotationUtil.buildRouteFromSchemeHostPath(scheme, host, path);
        }
        return route;
    }

    boolean isForResult() {
        return forResult;
    }

    int getRequestCode() {
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

    void callback(@Nullable NavCallback callback) {
        this.callback = callback;
    }

    @Nullable
    NavCallback getCallback() {
        return callback;
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

    void forResult(boolean forResult, int requestCode) {
        this.forResult = forResult;
        this.requestCode = requestCode;
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

    void setExtras(@Nullable Bundle value) {
        this.extras = value;
    }

    void withSceneTransitionAnimation(View sharedElement, String sharedElementName) {
        if (sharedElement != null && sharedElementName != null) {
            Activity activity = context instanceof Activity ? (Activity) context : (fragment != null ? fragment.getActivity() : null);
            if (activity != null) {
                addOptions(ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElement, sharedElementName).toBundle());
            }
        }
    }

    void withSceneTransitionAnimation(Pair<View, String>... sharedElements) {
        if (sharedElements != null && sharedElements.length > 0) {
            Activity activity = context instanceof Activity ? (Activity) context : (fragment != null ? fragment.getActivity() : null);
            if (activity != null) {
                addOptions(ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElements).toBundle());
            }
        }
    }

    void addOptions(@Nullable Bundle options) {
        if (options == null || options.isEmpty()) {
            return;
        }

        if (this.options == null) {
            this.options = options;
        } else {
            this.options.putAll(options);
        }
    }

    void setOptions(@Nullable Bundle options) {
        this.options = options;
    }

    void overridePendingTransition(int enterAnim, int exitAnim) {
        this.overrideEnterAnim = enterAnim;
        this.overrideExitAnim = exitAnim;
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

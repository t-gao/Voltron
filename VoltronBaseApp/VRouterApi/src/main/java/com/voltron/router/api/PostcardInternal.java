package com.voltron.router.api;

import android.app.Activity;
import android.content.Context;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.View;

import com.voltron.router.base.AnnotationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class PostcardInternal {
    Context context;
    Fragment fragment;

    String scheme;
    String host;
    String path;
    String route;
    String endPointKey;
    Bundle extras;
    Bundle options;
    int intentFlags;
    boolean forResult;
    int requestCode = -1;

    // 用于记录上层自定义的标记
    int flags;

    private String originalScheme;

    Integer overrideEnterAnim = null;
    Integer overrideExitAnim = null;

    boolean bindService = false; //作用于bindservice, 默认非bind启动
    ServiceConnection conn = null; //作用于bindservice
    int bindServiceFlags; //作用于bindservice

    private ArrayList<Interceptor> interceptors = new ArrayList<>();
    private ArrayList<Interceptor> routeInterceptors = new ArrayList<>();

    Interceptor.Chain interceptorChain;
    Interceptor.Chain routeInterceptorChain;

    private NavCallback callback;
    private NavHandler navHandler;

    boolean dontInterceptRoute = false;
    boolean dontInterceptNavigation = false;

    private int pendingCode; // 上层自定义的pending类型码，非0表示当前postcard处于pending状态
    private CharSequence pendingMessage; // 上层自定义的pending描述

    private boolean canceled = false; // 是否已取消本次路由
    private int cancelCode; // 上层自定义的取消类型码
    private CharSequence cancelMessage; // 上层自定义的取消描述

    // 是否已完成跳转
    private boolean navigated = false;

    // 完成跳转后，回调监听是否已回调
    private boolean navigatedCallbackCalled = false;

    PostcardInternal(Context context, Fragment fragment, String path, Bundle extras, int intentFlags,
                     boolean forResult, int requestCode) {

        this.context = context;
        this.fragment = fragment;
        this.path = path;
        this.extras = extras;
        this.intentFlags = intentFlags;
        this.forResult = forResult;
        this.requestCode = requestCode;
        this.routeInterceptors.addAll(VRouterInternal.commonRouteInterceptors());
        this.navHandler = VRouterInternal.defaultNavHandler;
    }

    PostcardInternal(Context context) {
        this.context = context;
        this.routeInterceptors.addAll(VRouterInternal.commonRouteInterceptors());
        this.navHandler = VRouterInternal.defaultNavHandler;
    }

    String getScheme() {
        if (TextUtils.isEmpty(scheme)) {
            scheme = AnnotationUtil.getSchemeFromRoute(route);
        }
        return scheme;
    }

    String getHost() {
        if (TextUtils.isEmpty(host)) {
            host = AnnotationUtil.getHostFromRoute(route);
        }
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

    String getEndPointKey() {
        if (TextUtils.isEmpty(endPointKey)) {
            endPointKey = AnnotationUtil.getEndPointKeyFromRoute(route);
        }
        return endPointKey;
    }

    /**
     * 获取 route url 里自带的参数（即 url 的 query 部分），返回一个 Map
     * @return
     */
    @Nullable
    Map<String, String> getRouteUrlQuery() {
        String route = getRoute();
        if (route == null) return null;

        try {
            Uri uri = Uri.parse(route);
            Set<String> names = uri.getQueryParameterNames();
            Map<String, String> query = new HashMap<>();
            for (String name : names) {
                String v = uri.getQueryParameter(name);
                if (v != null) {
                    query.put(name, v);
                }
            }
            return query;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取 route url 里自带的参数（即 url 的 query 部分），返回一个 Bundle
     * @return
     */
    @Nullable
    Bundle getRouteUrlQueryAsBundle() {
        String route = getRoute();
        if (route == null) return null;

        try {
            Uri uri = Uri.parse(route);
            Set<String> names = uri.getQueryParameterNames();
            Bundle query = new Bundle();
            for (String name : names) {
                String v = uri.getQueryParameter(name);
                if (v != null) {
                    query.putString(name, v);
                }
            }
            return query;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    String getEncodedQueryStringOfRouteUrl() {
        String route = getRoute();
        if (route == null) return null;

        try {
            Uri uri = Uri.parse(route);
            return uri.getEncodedQuery();
        } catch (Exception e) {
            return null;
        }
    }

    boolean isPending() {
        return this.pendingCode != 0;
    }

    boolean isPendingOnCode(int pendingCode) {
        return (this.pendingCode & pendingCode) > 0;
    }

    int getPendingCode() {
        return pendingCode;
    }

    void clearPendingCode(int pendingCode) {
        this.pendingCode &= ~pendingCode;
    }

    void clearAllPendingCodes() {
        pendingCode = 0;
    }

    void setPending(int pendingCode, CharSequence pendingMessage) {
        this.pendingCode |= pendingCode;
        this.pendingMessage = pendingMessage;
    }

    boolean isCanceled() {
        return canceled;
    }

    void setCanceled(boolean canceled, int cancelCode, CharSequence cancelMessage) {
        this.canceled = canceled;
        this.cancelCode = cancelCode;
        this.cancelMessage = cancelMessage;
    }

    public int getCancelCode() {
        return cancelCode;
    }

    public CharSequence getCancelMessage() {
        return cancelMessage;
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
        return AnnotationUtil.extractGroupNameFromRoute(route);
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

    @Nullable
    public NavHandler getNavHandler() {
        return navHandler;
    }

    void addInterceptor(@Nullable Interceptor interceptor) {
        if (interceptor != null) {
            this.interceptors.add(interceptor);
        }
    }

    void addRouteInterceptor(@Nullable Interceptor routeInterceptor) {
        if (routeInterceptor != null) {
            this.routeInterceptors.add(routeInterceptor);
        }
    }

    void removeRouteInterceptor(@Nullable Interceptor routeInterceptor) {
        this.routeInterceptors.remove(routeInterceptor);
    }

    void clearRouteInterceptors() {
        this.routeInterceptors.clear();
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

    public void setEndPointKey(String endPointKey) {
        this.endPointKey = endPointKey;
    }

    public void setDontInterceptRoute(boolean dontIntercept) {
        this.dontInterceptRoute = dontIntercept;
    }

    public void setDontInterceptNavigation(boolean dontIntercept) {
        this.dontInterceptNavigation = dontIntercept;
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

    public void setNavHandler(@Nullable NavHandler navHandler) {
        this.navHandler = navHandler;
    }

    void overridePendingTransition(int enterAnim, int exitAnim) {
        this.overrideEnterAnim = enterAnim;
        this.overrideExitAnim = exitAnim;
    }

    void make(@NonNull Postcard postcard) {
        this.originalScheme = getScheme();

        this.interceptorChain = new VRouterInterceptorChain(postcard, this.interceptors);
        this.routeInterceptorChain = new VRouterInterceptorChain(postcard, this.routeInterceptors);
    }

    void removeInterceptor(@Nullable Interceptor interceptor) {
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

    String getOriginalScheme() {
        return originalScheme;
    }

    void setFlags(int flags) {
        this.flags = flags;
    }

    void addFlags(int flags) {
        this.flags |= flags;
    }

    void clearFlags(int flags) {
        this.flags &= ~flags;
    }

    void clearFlags() {
        this.flags = 0;
    }

    boolean isNavigated() {
        return navigated;
    }

    void setNavigated(boolean navigated) {
        this.navigated = navigated;
    }

    boolean isNavigatedCallbackCalled() {
        return navigatedCallbackCalled;
    }

    void setNavigatedCallbackCalled(boolean navigatedCallbackCalled) {
        this.navigatedCallbackCalled = navigatedCallbackCalled;
    }
}

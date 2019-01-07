package com.voltron.router.api;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import com.voltron.router.EndPointType;
import com.voltron.router.base.AnnotationConsts;
import com.voltron.router.base.StringUtils;

public class VRouter {

    public static void init() {
        VRouterInternal.init();
    }

    @Nullable
    public static Pair<EndPointType, Class> resolveEndPoint(String scheme, String host, String path) {
        host = StringUtils.ensureNoneNullString(host);
        path = StringUtils.ensureNoneNullString(path);
        String route = TextUtils.isEmpty(scheme) ? (host + path) : (scheme + AnnotationConsts.SCHEME_SUFFIX + host + path);
        return resolveEndPoint(route);
    }

    @Nullable
    public static Pair<EndPointType, Class> resolveEndPoint(String route) {
        return VRouterInternal.resolveEndPoint(route);
    }

    public static Postcard.Builder with(Context context) {
        return new Postcard.Builder(context);
    }

    /**
     * In general , obj is Activity or Fragment
     * @param obj
     */
    public static void inject(Object obj){
        VRouterInternal.inject(obj);
    }

    /**
     * register scheme Handler
     *
     * @param scheme  eg: http/https/myscheme
     * @param handler the handler to handle the route to dispatch
     */
    public static void registerSchemeHandler(String scheme, IRouteSchemeHandler handler) {
        VRouterInternal.registerSchemeHandler(scheme, handler);
    }
}

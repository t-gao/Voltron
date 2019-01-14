package com.voltron.router.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
    public static void inject(Object obj) {
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

    /**
     * Same effect as in {@link Activity#startActivities(Intent[])}
     *
     * @param starter the starter
     * @param postcards one or more postcards of corresponding activities to start
     * @return true if startActivities is successfully invoked
     */
    public static boolean startActivities(Activity starter, Postcard... postcards) {
        return VRouterInternal.startActivities(starter, postcards);
    }
}

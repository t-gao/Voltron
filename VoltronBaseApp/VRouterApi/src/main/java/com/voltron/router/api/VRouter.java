package com.voltron.router.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.voltron.router.EndPointType;

public class VRouter {

    public static void init() {
        init(null, (Interceptor[]) null);
    }

    public static void init(@Nullable NavHandler navHandler, @Nullable Interceptor... commonRouteInterceptors) {
        VRouterInternal.init(navHandler, commonRouteInterceptors);
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
     * Same effect as in {@link Activity#startActivities(Intent[])}.
     * If there are more than one postcards, their interceptors will be ignored.
     *
     * @param starter the starter
     * @param postcards one or more postcards of corresponding activities to start
     * @return true if startActivities is successfully invoked
     */
    public static boolean startActivities(Activity starter, Postcard... postcards) {
        return VRouterInternal.startActivities(starter, postcards);
    }

    /**
     * 正数表示已处理，VRouter 内部将不再处理；
     * 负数表示未处理，需要VRouter 内部处理跳转；
     * 0 表示不支持的 scheme.
     */
    public static final class NavigationTypes {
        public static final int NOT_SUPPORTED = 0;          // 不支持的 scheme

        public static final int NATIVE_HANDLED = 1;         // 原生实现，已处理
        public static final int RN_HANDLED = 2;             // RN实现，已处理
        public static final int WEB_HANDLED = 3;            // Web实现，已处理
        public static final int UNKNOWN_HANDLED = 10000;   // 未知scheme，已处理
        // 其他正数 - 其他，已处理；

        public static final int NATIVE_NOT_HANDLED = -1;    // 原生实现，未处理
        public static final int RN_NOT_HANDLED = -2;        // RN实现，未处理
        public static final int WEB_NOT_HANDLED = -3;       // Web实现，未处理
        public static final int UNKNOWN_NOT_HANDLED = -10000;   // 未知scheme，未处理
        // 其他负数 - 其他，未处理；

        /**
         * 判断给定的跳转类型是否表示已处理。
         * @param navType
         * @return
         */
        public static boolean isHandled(int navType) {
            return navType > 0;
        }

    }
}

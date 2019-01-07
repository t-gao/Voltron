package com.voltron.router.api;

import android.content.Context;

import com.voltron.router.EndPointType;

public class VRouter {
    public static void init() {
        VRouterInternal.init();
    }

    public static EndPointType resolveType(String path) {
        return EndPointType.OTHER;//FIXME
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
}

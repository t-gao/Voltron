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
     * In general , clz is Activity or Fragment
     * @param clz
     */
    public static void inject(Object clz){
        VRouterInternal.inject(clz);
    }
}

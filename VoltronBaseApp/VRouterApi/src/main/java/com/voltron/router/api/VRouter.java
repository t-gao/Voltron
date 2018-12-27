package com.voltron.router.api;

import android.app.Activity;

public class VRouter {
    public static void init() {
        VRouterInternal.init();
    }

    // TODO: 简单演示跳转，后续需优化方法命名、传递参数等
    public static boolean go(Activity activity, String groupName, String path) {
        if (activity == null || path == null || path.isEmpty()) {
            return false;
        }

        return VRouterInternal.go(activity, groupName, path);
    }
}

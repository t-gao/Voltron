package com.voltron.router.api;

import androidx.annotation.NonNull;

/**
 * Navigation handler, 路由跳转处理器。
 * 调用者向 VRouter 注册路由跳转处理器后，可以自行处理相关跳转。
 */
public interface NavHandler {

    /**
     * 返回值 见 {@link com.voltron.router.api.VRouter.NavigationTypes}
     *
     * @param route
     * @return
     */
    int getNavigationType(String route);

    /**
     * 返回值 正数表示已处理，VRouter 内部将不再处理；负数表示未处理，需要VRouter 内部处理跳转；0 表示不支持的 scheme：
     *     1 - 原生实现，已处理；
     *     2 - RN实现，已处理；
     *     3 - web实现，已处理；
     *     其他正数 - 其他，已处理；
     *     0 - 不支持的 scheme；
     *     -1 - 原生实现，未处理；
     *     -2 - RN实现，未处理；
     *     -3 - web实现，未处理；
     *     其他负数 - 其他，未处理；
     *
     * 见 {@link com.voltron.router.api.VRouter.NavigationTypes}
     *
     * @param postcard
     * @return
     */
    int onNavigation(@NonNull Postcard postcard);

    void onPending(@NonNull Postcard postcard);
}

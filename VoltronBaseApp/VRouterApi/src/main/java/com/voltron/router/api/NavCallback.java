package com.voltron.router.api;

public interface NavCallback {

    /**
     * called if no route found for the specified postcard
     * 如果指定的 postcard 未找到对应的路由信息，回调该方法
     */
    void onNotFound();

    /**
     * called when the final real call to navigate to a page or to start or bind a service has been made
     * 当最终的 startActivity 或者 startService/bindService 调用后回调改方法
     */
    void onNavigated();

    /**
     * 出现了除未找到之外的其他异常情况
     */
    void onError();
}

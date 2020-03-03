package com.voltron.router.api;

import androidx.annotation.Nullable;

public interface NavCallback {

    /**
     * called if no route found for the specified postcard
     * 如果指定的 postcard 未找到对应的路由信息，或对应的scheme等不支持，回调该方法
     */
    void onNotFound();

    /**
     * 被拦截器拦截时回调。
     * 可能回调多次。
     */
    void onIntercepted();

    /**
     * 被pending时回调。
     * 默认情况下，不需要处理pending情况，后续内部会自动处理
     */
    void onPending();

    /**
     * called when the final real call to navigate to a page or to start or bind a service has been made
     * 当最终的 startActivity 或者 startService/bindService 调用后回调改方法
     */
    void onNavigated();

    /**
     * 当本次跳转被取消是回调，指定取消的code和message
     *
     * @param cancelCode
     * @param cancelMessage
     */
    void onCancelled(int cancelCode, @Nullable CharSequence cancelMessage);

    /**
     * 出现了除未找到之外的其他异常情况
     */
    void onError(@Nullable Throwable e);
}

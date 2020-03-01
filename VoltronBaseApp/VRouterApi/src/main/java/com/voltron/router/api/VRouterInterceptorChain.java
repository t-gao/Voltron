package com.voltron.router.api;

import android.support.annotation.NonNull;

import java.util.ArrayList;

class VRouterInterceptorChain implements Interceptor.Chain {

    @NonNull
    private final Postcard postcard;

    @NonNull
    private final ArrayList<Interceptor> interceptors;

    private int index = 0;

    VRouterInterceptorChain(@NonNull Postcard postcard, @NonNull ArrayList<Interceptor> interceptors) {
        this.postcard = postcard;
        this.interceptors = interceptors;
    }

    @Override
    @NonNull
    public Postcard postcard() {
        return postcard;
    }

    @Override
    public boolean proceed() {
        return VRouterInternal.go(postcard);
    }

    @Override
    public Interceptor.Chain insertToNextPosition(@NonNull Interceptor interceptor) {
        interceptors.add(index, interceptor);
        return this;
    }

    boolean hasNextInterceptor() {
        return interceptors.size() > index;
    }

    Interceptor nextInterceptor() {
        if (index >= interceptors.size()) {
            return null;
        }

        return interceptors.get(index++);
    }
}

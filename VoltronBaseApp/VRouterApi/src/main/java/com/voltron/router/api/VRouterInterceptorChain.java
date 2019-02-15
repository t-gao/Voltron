package com.voltron.router.api;

import java.util.ArrayList;

class VRouterInterceptorChain implements Interceptor.Chain {

    private final Postcard postcard;
    private final ArrayList<Interceptor> interceptors;

    private int index = 0;

    VRouterInterceptorChain(Postcard postcard, ArrayList<Interceptor> interceptors) {
        this.postcard = postcard;
        this.interceptors = interceptors;
    }

    @Override
    public Postcard postcard() {
        return postcard;
    }

    @Override
    public boolean proceed() {
        return VRouterInternal.go(postcard.getPostcardInternal());
    }

    boolean hasNextInterceptor() {
        int len = interceptors == null ? 0 : interceptors.size();
        return len > index;
    }

    Interceptor nextInterceptor() {
        if (interceptors == null || index >= interceptors.size()) {
            return null;
        }

        return interceptors.get(index++);
    }
}

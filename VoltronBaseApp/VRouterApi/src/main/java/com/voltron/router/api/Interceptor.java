package com.voltron.router.api;

public interface Interceptor {

    void intercept(Chain chain);

    interface Chain {

        Postcard postcard();

        /**
         *
         * @return true if the routing process finishes successfully
         */
        boolean proceed();
    }
}

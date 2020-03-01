package com.voltron.router.api;

import android.support.annotation.NonNull;

public interface Interceptor {

    void intercept(@NonNull Chain chain);

    interface Chain {

        @NonNull
        Postcard postcard();

        /**
         *
         * @return true if the routing process finishes successfully
         */
        boolean proceed();

        /**
         * Insert an interceptor to the next position of the chain.
         * The newly inserted interceptor will be handled right after the current interceptor.
         *
         * @param interceptor
         */
        Chain insertToNextPosition(@NonNull Interceptor interceptor);
    }
}

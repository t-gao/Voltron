package com.voltron.router.api;

/**
 * This adapter class provides empty implementations of the methods from {@link NavCallback}.
 * Any custom callback that cares only about a subset of the methods of this callback can
 * simply subclass this adapter class instead of implementing the interface directly.
 */
public abstract class NavCallbackAdapter implements NavCallback {

    @Override
    public void onNotFound() {
    }

    @Override
    public void onNavigated() {
    }

    @Override
    public void onError() {
    }

}

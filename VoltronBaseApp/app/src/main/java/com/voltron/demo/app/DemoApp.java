package com.voltron.demo.app;

import android.app.Application;

import com.voltron.router.api.VRouter;

public class DemoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VRouter.init();
    }
}

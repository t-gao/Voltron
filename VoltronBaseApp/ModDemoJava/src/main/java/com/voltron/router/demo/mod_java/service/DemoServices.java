package com.voltron.router.demo.mod_java.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.voltron.router.annotation.EndPoint;

/**
 * @author wenzhihao
 * @description service for test
 * @date 2019/2/11
 */
@EndPoint(value = "/modulea/service")
public class DemoServices extends Service {
    public final String TAG = "DemoServices";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "DemoServices startup >>>>>") ;
        Log.e(TAG, "DemoServices url = "+intent.getStringExtra("url")) ;
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

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
@EndPoint(value = "/modulea/bindservice")
public class DemoBindServices extends Service {

    public final String TAG = "DemoBindServices";

    private final IBinder binder = new DemoBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "DemoBindServices startup1 >>>>>") ;
        Log.e(TAG, "DemoBindServices url1 = "+intent.getStringExtra("url")) ;
        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "DemoBindServices onBind >>>>>");
        Log.e(TAG, "DemoBindServices url = "+intent.getStringExtra("url")) ;
        return binder;
    }
}

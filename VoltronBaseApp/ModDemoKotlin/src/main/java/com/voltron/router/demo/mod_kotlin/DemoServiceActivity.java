package com.voltron.router.demo.mod_kotlin;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.voltron.route.interfaces.ModDemoInterface;
import com.voltron.router.annotation.EndPoint;
import com.voltron.router.api.VRouter;

/**
 * @author wenzhihao
 * @description 测试独立module启动服务
 * @date 2019/2/12
 */
@EndPoint(scheme = "voltron", host = "kotlin.com", path = "/test/service")
public class DemoServiceActivity extends AppCompatActivity {
    public static final String TAG = "DemoServiceActivity";

    //自定义ServiceConnection去实现具体逻辑
    private  ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ModDemoInterface localBinder = (ModDemoInterface) service;
            localBinder.sayHello();
            Log.e(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_service);

        //startService  启动ModDemoJava中的DemoServices
        findViewById(R.id.button_start_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VRouter.with(DemoServiceActivity.this)
                        .route("/modulea/service")
                        .stringExtra("url", "https://www.haohuan.com")
                        .go();
            }
        });

        //bindService 启动ModDemoJava中的DemoBindServices
        findViewById(R.id.button_bind_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VRouter.with(DemoServiceActivity.this)
                        .route("/modulea/bindservice")
                        .stringExtra("url", "https://www.baidu.com")
                        .bindService(connection, Context.BIND_AUTO_CREATE)
                        .go();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
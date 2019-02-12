package com.voltron.router.demo.mod_java.service;

import android.os.Binder;
import android.util.Log;

import com.voltron.route.interfaces.ModDemoInterface;

/**
 * @author wenzhihao
 * @description 自定义binder,实现共用模块的IBinder
 * @date 2019/2/12
 */
public class DemoBinder extends Binder implements ModDemoInterface {

    @Override
    public void sayHello() {
        Log.e("DemoBinder", "sayHello");
    }
}
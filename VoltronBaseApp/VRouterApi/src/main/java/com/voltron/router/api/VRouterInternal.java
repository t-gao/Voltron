package com.voltron.router.api;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class VRouterInternal {
    private static final String TAG = "VRouterInternal";

    static void init(Activity activity) {
        Class clazz = activity.getClass();
        boolean result = false;
        StringBuilder errBuilder = new StringBuilder();
        try {
            String generatedClassName = "com.voltron.router.routes." + clazz.getSimpleName() + "$$Generated";
            Class generatedClazz = Class.forName(generatedClassName);
            Method method = generatedClazz.getMethod("getAnnoVal");
            String ret = (String) method.invoke(null);
            Log.d(TAG, "method getAnnoVal() of class " + generatedClassName + " returned value: " + ret);
            Toast.makeText(activity, "path: " + ret, Toast.LENGTH_LONG).show();
            result = true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            errBuilder.append("ClassNotFoundException").append(e.getMessage());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            errBuilder.append("NoSuchMethodException").append(e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            errBuilder.append("IllegalAccessException").append(e.getMessage());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            errBuilder.append("InvocationTargetException").append(e.getMessage());
        }

        if (!result) {
            Toast.makeText(activity, "ERROR : " + errBuilder.toString(), Toast.LENGTH_LONG).show();
        }
    }
}

package me.tangni.librouter;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class VRouterInternal {
    private static final String TAG = "VRouterInternal";

    static void init(Activity activity) {
        Class clazz = activity.getClass();
        try {
            String generatedClassName = clazz.getName() + "$$Generated";
            Class generatedClazz = Class.forName(generatedClassName);
            Method method = generatedClazz.getMethod("getAnnoVal");
            String ret = (String) method.invoke(null);
            Log.d(TAG, "method getAnnoVal() of class " + generatedClassName + " returned value: " + ret);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

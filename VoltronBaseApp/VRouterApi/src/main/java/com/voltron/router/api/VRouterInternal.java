package com.voltron.router.api;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.voltron.router.base.EndPointMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class VRouterInternal {
    private static final String TAG = "VRouterInternal";

    private static HashMap<String, HashMap<String, EndPointMeta>> groups = new HashMap<>();
    private static HashMap<String, EndPointMeta> noNameGroup = new HashMap<>();

    static void init() {
        Log.d(TAG, "VROUTER GROUP >>>> init");
        try {
            Class entryClazz = Class.forName("com.voltron.router.routes.VRouterEntry");
            Method method = entryClazz.getMethod("init", ArrayList.class);
            ArrayList<String> groupClassNames = new ArrayList<>();
            method.invoke(null, groupClassNames);
            for (String groupClassName : groupClassNames) {
//                Log.d(TAG, "VROUTER GROUP >>>> : " + groupClassName);
                if (groupClassName.endsWith("G__")) {
                    loadGroup(groupClassName, null);
                } else {
                    int mid = groupClassName.lastIndexOf("G__");
                    String groupName = groupClassName.substring(mid + "G__".length());
                    loadGroup(groupClassName, groupName);
                }
            }
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

    // 加载分组内的路由信息
    private static void loadGroup(String groupClassName, String groupName) {
        Log.d(TAG, "VROUTER GROUP >>>> loadGroup, groupClassName: " + groupClassName + ", groupName: " + groupName);
        try {
            Class groupClazz = Class.forName(groupClassName);
            Method method = groupClazz.getMethod("load", Map.class);
            HashMap<String, EndPointMeta> routes = new HashMap<>();
            method.invoke(null, routes);
            if (TextUtils.isEmpty(groupName)) {
                noNameGroup.putAll(routes);
            } else {
                HashMap<String, EndPointMeta> group = groups.get(groupName);
                if (group == null) {
                    group = new HashMap<>();
                    groups.put(groupName, group);
                }
                group.putAll(routes);
            }
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

    static boolean go(@NonNull Activity activity, String groupName, @NonNull String path) {
        Log.d(TAG, "go, path: " + path);
        HashMap<String, EndPointMeta> group;
        if (TextUtils.isEmpty(groupName)) {
            group = noNameGroup;
        } else {
            group = groups.get(groupName);
            if (group == null) {
                Log.e(TAG, "GROUP NOT FOUND with group name " + groupName + "!!!");
                return false;
            }
        }

        EndPointMeta endPointMeta = group.get(path);
        if (endPointMeta == null) {
            Log.e(TAG, "END POINT NOT FOUND with path " + path + "!!!");
            return false;
        }

        Class<?> endpointClass = endPointMeta.getEndPointClass();
        if (endpointClass == null) {
            Log.e(TAG, "CLASS NOT FOUND with path " + path + "!!!");
            return false;
        }

        try {
            activity.startActivity(new Intent(activity, endpointClass));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "START ACTIVITY ERR ", e);
            return false;
        }

    }

}

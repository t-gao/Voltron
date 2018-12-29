package com.voltron.router.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import com.voltron.router.base.AnnotationUtil;
import com.voltron.router.base.EndPointMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class VRouterInternal {
    private static final String TAG = "VRouterInternal";

    private static String noNameGroupClassName;
    private static HashMap<String, String> groupClassNameMap = new HashMap<>();
    private static HashMap<String, HashMap<String, EndPointMeta>> groups = new HashMap<>();
    private static HashMap<String, EndPointMeta> noNameGroup = new HashMap<>();

    static void init() {
        Log.d(TAG, "VROUTER GROUP >>>> init");
        try {
            // first, find the entry class
            Class entryClazz = Class.forName("com.voltron.router.routes.VRouterEntry");
            Method method = entryClazz.getMethod("init", ArrayList.class);
            ArrayList<String> groupClassNames = new ArrayList<>();

            // then invoke init method of the entry class to get all the group class names
            method.invoke(null, groupClassNames);

            // now put the class names to the map for later retrieve.
            // don't load the routes now, load a group when first used
            for (String groupClassName : groupClassNames) {
                if (groupClassName.endsWith("G__")) {
                    noNameGroupClassName = groupClassName;
                } else {
                    int mid = groupClassName.lastIndexOf("G__");
                    String groupName = groupClassName.substring(mid + "G__".length());
                    groupClassNameMap.put(groupName, groupClassName);
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

    static boolean go(@Nullable Postcard postcard) {
        if (postcard == null || postcard.getContext() == null || TextUtils.isEmpty(postcard.getPath())) {
            return false;
        }

        String path = postcard.getPath();
        String groupName = postcard.getGroup();
        if (groupName == null || groupName.isEmpty()) {
            groupName = AnnotationUtil.extractGroupNameFromPath(path);
        }

        EndPointMeta endPointMeta = getEndPointMetaByGroupNameAndPath(groupName, path);
        if (endPointMeta == null) {
            Log.e(TAG, "END POINT NOT FOUND with path " + path + "!!!");
            return false;
        }

        return go(postcard.getContext(), endPointMeta, postcard);
    }

    // 加载分组内的路由信息
    private static HashMap<String, EndPointMeta> loadGroup(String groupClassName, String groupName) {
        Log.d(TAG, "VROUTER GROUP >>>> loadGroup, groupClassName: " + groupClassName + ", groupName: " + groupName);
        try {
            Class groupClazz = Class.forName(groupClassName);
            Method method = groupClazz.getMethod("load", Map.class);
            HashMap<String, EndPointMeta> routes = new HashMap<>();
            method.invoke(null, routes);
            if (TextUtils.isEmpty(groupName)) {
                if (noNameGroup == null) {
                    noNameGroup = new HashMap<>();
                }
                noNameGroup.putAll(routes);
                return noNameGroup;
            } else {
                HashMap<String, EndPointMeta> group = groups.get(groupName);
                if (group == null) {
                    group = new HashMap<>();
                    groups.put(groupName, group);
                }
                group.putAll(routes);
                return group;
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
        return null;
    }

    private static EndPointMeta getEndPointMetaByGroupNameAndPath(String groupName, String path) {
        boolean noName = TextUtils.isEmpty(groupName);
        String groupClassName = noName ? noNameGroupClassName : groupClassNameMap.get(groupName);
        if (TextUtils.isEmpty(groupClassName)) {
            return null;
        }

        HashMap<String, EndPointMeta> group = noName ? noNameGroup : groups.get(groupName);
        if (group == null) {// 若该分组尚未加载，则调用 loadGroup() 加载
            group = loadGroup(groupClassName, groupName);
        }

        if (group == null) {
            Log.e(TAG, "GROUP NOT FOUND with group name " + (groupName == null ? "" : groupName) + "!!!");
            return null;
        }

        return group.get(path);
    }

    private static boolean go(@NonNull Context context, EndPointMeta endPointMeta,
                              @NonNull Postcard postcard) {

        String path = postcard.getPath();
        Log.d(TAG, "go, path: " + path);

        Class<?> endpointClass = endPointMeta.getEndPointClass();
        if (endpointClass == null) {
            Log.e(TAG, "CLASS NOT FOUND with path " + path + "!!!");
            return false;
        }

        try {
            Intent intent = new Intent(context, endpointClass);
            Bundle extras = postcard.getExtras();
            if (extras != null) {
                intent.putExtras(extras);
            }
            intent.setFlags(postcard.getIntentFlags());

            Fragment fragment = postcard.getFragment();
            if (fragment != null) {
                if (postcard.isForResult()) {
                    fragment.startActivityForResult(intent, postcard.getRequestCode());
                } else {
                    fragment.startActivity(intent);
                }
            } else {
                if (context instanceof Activity) {
                    if (postcard.isForResult()) {
                        ((Activity)context).startActivityForResult(intent, postcard.getRequestCode());
                    } else {
                        ((Activity)context).startActivity(intent);
                    }
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "START ACTIVITY ERR ", e);
            return false;
        }

    }

}

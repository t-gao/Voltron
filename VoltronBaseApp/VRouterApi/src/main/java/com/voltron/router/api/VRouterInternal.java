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
import android.util.Pair;

import com.voltron.router.EndPointType;
import com.voltron.router.base.AnnotationConsts;
import com.voltron.router.base.AnnotationUtil;
import com.voltron.router.base.EndPointMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class VRouterInternal {
    private static final String TAG = "VRouterInternal";

    // 分组名groupName 和 各个模块的以该名为分组的类名的映射关系
    private static ArrayList<String> anonymousGroupClassNames;
    private static HashMap<String, ArrayList<String>> groupClassNamesMap = new HashMap<>();

    //  分组名groupName 和 该分组内的路由信息的映射关系
    private static HashMap<String, HashMap<String, EndPointMeta>> groups = new HashMap<>();
    private static HashMap<String, EndPointMeta> anonymousGroup;
    // 缓存用户自定义的Scheme的处理
    private static HashMap<String, IRouteSchemeHandler> schemeHandlerHashMap = new HashMap<>();

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
                    if (anonymousGroupClassNames == null) {
                        anonymousGroupClassNames = new ArrayList<>();
                    }
                    anonymousGroupClassNames.add(groupClassName);
                } else {
                    int mid = groupClassName.lastIndexOf("G__");
                    String groupName = groupClassName.substring(mid + "G__".length());
                    ArrayList<String> groupClassNameList = groupClassNamesMap.get(groupName);
                    if (groupClassNameList == null) {
                        groupClassNameList = new ArrayList<>();
                        groupClassNamesMap.put(groupName, groupClassNameList);
                    }
                    groupClassNameList.add(groupClassName);
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

    static void inject(Object obj) {
        try {
            if (obj != null) {
                String className = obj.getClass().getName();
                Class classAutowired = Class.forName(className + "__Autowired");
                if (classAutowired != null) {
                    Method method = classAutowired.getMethod("inject" , Object.class);
                    if (method != null) {
                        method.invoke(null, obj);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Pair<EndPointType, Class> resolveEndPoint(String route) {
        if (TextUtils.isEmpty(route)) {
            return null;
        }

        String groupName = AnnotationUtil.extractGroupNameFromRoute(route);
        EndPointMeta endPointMeta = getEndPointMetaByGroupNameAndRoute(groupName, route);
        if (endPointMeta == null) {
            Log.e(TAG, "END POINT NOT FOUND with route " + route + "!!!");
            return null;
        }

        return new Pair<>(endPointMeta.getEndPointType(), endPointMeta.getEndPointClass());
    }

    static boolean go(@Nullable Postcard postcard) {
        if (postcard == null || postcard.getContext() == null || TextUtils.isEmpty(postcard.getRoute())) {
            return false;
        }

        String route = postcard.getRoute();
        // handle deeplink scheme
        String scheme = getDeepLinkScheme(route);
        if (!TextUtils.isEmpty(scheme)) {
            IRouteSchemeHandler schemeHandler = getSchemeHandler(scheme);
            if (schemeHandler != null) {
                schemeHandler.handle(route);
            }
        }
        String groupName = postcard.getGroup();

        EndPointMeta endPointMeta = getEndPointMetaByGroupNameAndRoute(groupName, route);
        if (endPointMeta == null) {
            Log.e(TAG, "END POINT NOT FOUND with route " + route + "!!!");
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
                if (anonymousGroup == null) {
                    anonymousGroup = new HashMap<>();
                }
                anonymousGroup.putAll(routes);
                return anonymousGroup;
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

    private static EndPointMeta getEndPointMetaByGroupNameAndRoute(String groupName, String route) {
        boolean anonymous = TextUtils.isEmpty(groupName);
        ArrayList<String> groupClassNameList = anonymous ? anonymousGroupClassNames : groupClassNamesMap.get(groupName);
        if (groupClassNameList == null || groupClassNameList.isEmpty()) {
            return null;
        }

        HashMap<String, EndPointMeta> group = anonymous ? anonymousGroup : groups.get(groupName);
        if (group == null) {// 若该分组尚未加载，则调用 loadGroup() 加载
            for (String groupClassName : groupClassNameList) {
                group = loadGroup(groupClassName, groupName);
            }
        }

        if (group == null) {
            Log.e(TAG, "GROUP NOT FOUND with group name " + (groupName == null ? "" : groupName) + "!!!");
            return null;
        }

        return group.get(route);
    }

    private static boolean go(@NonNull Context context, EndPointMeta endPointMeta,
                              @NonNull Postcard postcard) {

        String route = postcard.getRoute();
        Log.d(TAG, "go, route: " + route);

        Class<?> endpointClass = endPointMeta.getEndPointClass();
        if (endpointClass == null) {
            Log.e(TAG, "CLASS NOT FOUND with route " + route + "!!!");
            return false;
        }

        EndPointType endPointType = endPointMeta.getEndPointType();
        Log.d(TAG, "endPointType: " + endPointType.toString());
        switch (endPointType) {
            case ACTIVITY:
                return startActivity(context, endPointMeta, postcard, endpointClass);
            default:
                return false;
        }

    }

    private static boolean startActivity(@NonNull Context context, EndPointMeta endPointMeta,
                                         @NonNull Postcard postcard, @NonNull Class<?> endpointClass) {
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
                        ((Activity) context).startActivityForResult(intent, postcard.getRequestCode());
                    } else {
                        ((Activity) context).startActivity(intent);
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

    static void registerSchemeHandler(String scheme, IRouteSchemeHandler handler) {
        if (schemeHandlerHashMap == null) {
            schemeHandlerHashMap = new HashMap<>();
        }
        schemeHandlerHashMap.put(scheme, handler);
    }

    private static IRouteSchemeHandler getSchemeHandler(String scheme) {
        if (schemeHandlerHashMap == null) {
            return null;
        }
        return schemeHandlerHashMap.get(scheme);
    }

    // 只截取://（testscheme://）之前的Scheme
    private static String getDeepLinkScheme(String route) {
        int endIndex = -1;
        if (TextUtils.isEmpty(route) || (endIndex = route.indexOf(AnnotationConsts.SCHEME_SUFFIX)) == -1) {
            return "";
        }
        return route.substring(0, endIndex);
    }
}

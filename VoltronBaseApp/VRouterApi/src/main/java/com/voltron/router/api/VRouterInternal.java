package com.voltron.router.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.voltron.router.EndPointType;
import com.voltron.router.base.AnnotationUtil;
import com.voltron.router.base.EndPointMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    private static Collection<Interceptor> commonRouteInterceptors = new ArrayList<>();
    static NavHandler defaultNavHandler;

    static void init(@Nullable NavHandler navHandler, @Nullable Interceptor... commonRouteInterceptors) {
        Log.d(TAG, "VROUTER GROUP >>>> init");
        addCommonRouteInterceptors(commonRouteInterceptors);
        VRouterInternal.defaultNavHandler = navHandler;

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
        AnnotationUtil.injectAutowired(obj);
    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Nullable
    static Pair<EndPointType, Class> resolveEndPoint(String route) {
        if (TextUtils.isEmpty(route)) {
            return null;
        }

        String groupName = AnnotationUtil.extractGroupNameFromRoute(route);

        int navType = VRouter.NavigationTypes.NATIVE_NOT_HANDLED;
        NavHandler navHandler = VRouterInternal.defaultNavHandler;
        if (navHandler != null) {
            navType = navHandler.getNavigationType(route);
        }

        if (navType == VRouter.NavigationTypes.NOT_SUPPORTED) {
            return null;
        }

        if (navType == VRouter.NavigationTypes.NATIVE_NOT_HANDLED) {
            EndPointMeta endPointMeta = getEndPointMetaByGroupNameAndRoute(groupName, route);
            if (endPointMeta == null) {
                Log.e(TAG, "END POINT NOT FOUND with route " + route + "!!!");
                return null;
            }

            return new Pair<>(endPointMeta.getEndPointType(), endPointMeta.getEndPointClass());
        } else {
            return new Pair<>(getEndPointTypeByNavType(navType), null);
        }
    }

    private static EndPointType getEndPointTypeByNavType(int navType) {
        switch (navType) {
            case VRouter.NavigationTypes.RN_NOT_HANDLED:
            case VRouter.NavigationTypes.RN_HANDLED:
                return EndPointType.RN;
            case VRouter.NavigationTypes.WEB_NOT_HANDLED:
            case VRouter.NavigationTypes.WEB_HANDLED:
                return EndPointType.WEB;
        }
        return EndPointType.OTHER;
    }

    /**
     * Encapsulation of {@link Activity#startActivities(Intent[])}.
     * If there are more than one postcards, their interceptors will be ignored.
     * //TODO: callback and interceptors
     *
     * @param activity
     * @param postcards
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public static boolean startActivities(Activity activity, Postcard... postcards) {
        if (postcards != null) {
            if (postcards.length == 1) {
                if (postcards[0] == null) {
                    return false;
                }
                return go(postcards[0]);
            } else if (activity != null) {
                ArrayList<Intent> intents = new ArrayList<>();
                for (Postcard postcard : postcards) {
                    if (postcard != null) {
                        Intent i = buildActivityIntent(postcard.getPostcardInternal());
                        if (i != null) {
                            intents.add(i);
                        }
                    }
                }
                Intent[] intentArray = intents.toArray(new Intent[0]);
                if (intentArray != null && intentArray.length > 0) {
                    activity.startActivities(intentArray);
                    return true;
                }
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    static boolean go(@Nullable Postcard pc) {
        if (pc == null) {
            return false;
        }

        PostcardInternal postcard = pc.getPostcardInternal();
        if (postcard == null) {
            return false;
        }

        if (postcard.isCanceled()) {
            onCancelled(postcard);
            return false;
        }


        if (postcard.isPending()) {
            onPending(postcard);
            return false;
        }

        if (!checkInterceptors(postcard)) {
            onIntercepted(postcard);
            return false;
        }

        // 如果拦截器已完成跳转，回调并return true
        if (postcard.isNavigated()) {
            onNavigated(postcard);
            return true;
        }

        // 如果设置了不使用 "路由拦截器"，则不检查"路由拦截器"
        if (!postcard.dontInterceptRoute && !checkRouteInterceptors(postcard)) {
            onIntercepted(postcard);
            return false;
        }

        // 如果拦截器已完成跳转，回调并return true
        if (postcard.isNavigated()) {
            onNavigated(postcard);
            return true;
        }

        if (postcard.getContext() == null) {
            onError(postcard, null);
            return false;
        }

        String route = postcard.getRoute();

        if (TextUtils.isEmpty(route)) {
            onNotFound(postcard);
            return false;
        }

        String groupName = postcard.getGroup();

        // 根据 scheme 分发
        EndPointMeta endPointMeta = getEndPointMetaByGroupNameAndRoute(groupName, route);

        // 如果设置了不拦截跳转逻辑，直接走默认原生实现
        if (postcard.dontInterceptNavigation) {
            return goNative(postcard, endPointMeta);
        }

        NavHandler navHandler = postcard.getNavHandler();
        if (navHandler == null) {
            // 如果上册未注册跳转处理器，走默认原生处理
            return goNative(postcard, endPointMeta);
        } else {

            if (postcard.isPending()) {
                navHandler.onPending(pc);
                return false;
            }

            int navType = navHandler.onNavigation(pc);
            if (navType == VRouter.NavigationTypes.NOT_SUPPORTED) {
                onNotFound(postcard);
                return false;
            } else if (VRouter.NavigationTypes.isHandled(navType)) {
                onNavigated(postcard);
                return true;//已处理
            } else {
                switch (navType) {
                    case VRouter.NavigationTypes.NATIVE_NOT_HANDLED:
//                    case VRouter.NavigationTypes.UNKNOWN_NOT_HANDLED:
                        return goNative(postcard, endPointMeta);
                    case VRouter.NavigationTypes.RN_NOT_HANDLED:
                        return goRn(postcard);
                    case VRouter.NavigationTypes.WEB_NOT_HANDLED:
                        return goWeb(postcard);
                    default:
                        onNotFound(postcard);
                        return false;
                }
            }
        }
    }

    // 已完成跳转，回调
    private static void onNavigated(@NonNull PostcardInternal postcard) {
        NavCallback cb = postcard.getCallback();
        if (cb != null) {
            if (!postcard.isNavigatedCallbackCalled()) {
                postcard.setNavigatedCallbackCalled(true);
                cb.onNavigated();
            }
        }
    }

    // 未找到目标或不支持，回调
    private static void onNotFound(@NonNull PostcardInternal postcard) {
        NavCallback cb = postcard.getCallback();
        if (cb != null) {
            cb.onNotFound();
        }
    }

    // 被暂停时回调
    private static void onPending(@NonNull PostcardInternal postcard) {
        NavCallback cb = postcard.getCallback();
        if (cb != null) {
            cb.onPending();
        }
    }

    // 已被取消，回调
    private static void onCancelled(@NonNull PostcardInternal postcard) {
        NavCallback cb = postcard.getCallback();
        if (cb != null) {
            cb.onCancelled(postcard.getCancelCode(), postcard.getCancelMessage());
        }
    }

    // 被拦截，回调
    private static void onIntercepted(@NonNull PostcardInternal postcard) {
        NavCallback cb = postcard.getCallback();
        if (cb != null) {
            cb.onIntercepted();
        }
    }

    // 出错，回调
    private static void onError(@NonNull PostcardInternal postcard, @Nullable Throwable e) {
        NavCallback cb = postcard.getCallback();
        if (cb != null) {
            cb.onError(e);
        }
    }

    // RN处理默认实现
    private static boolean goRn(@NonNull PostcardInternal postcard) {
        onNotFound(postcard);//FIXME
        return false;
    }

    // Web处理默认实
    private static boolean goWeb(@NonNull PostcardInternal postcard) {
        onNotFound(postcard);//FIXME
        return false;
    }

        /**
         * check if all the interceptors have been handled.
         * 检查是否所有的拦截器都已经处理完了。
         *
         * @param postcard postcard
         * @return true if all the interceptors have been handled; false else.
         */
    private static boolean checkInterceptors(@NonNull PostcardInternal postcard) {
        VRouterInterceptorChain chain = (VRouterInterceptorChain) postcard.interceptorChain;
        if (chain == null || !chain.hasNextInterceptor()) {
            return true;
        }

        Interceptor next = chain.nextInterceptor();
        next.intercept(chain);

        return false;
    }

    /**
     * check if all the route interceptors have been handled.
     * 检查是否所有的 "路由拦截器" 都已经处理完了。
     *
     * "路由拦截器"的主要工作是在页面跳转发生前，修改路由。
     *
     * @param postcard postcard
     * @return true if all the route interceptors have been handled; false else.
     */
    private static boolean checkRouteInterceptors(@NonNull PostcardInternal postcard) {
        VRouterInterceptorChain chain = (VRouterInterceptorChain) postcard.routeInterceptorChain;
        if (chain == null || !chain.hasNextInterceptor()) {
            return true;
        }

        Interceptor next = chain.nextInterceptor();
        next.intercept(chain);

        return false;
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

        return group.get(AnnotationUtil.getEndPointKeyFromRoute(route));
    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    private static boolean goNative(@NonNull PostcardInternal postcard,
                                    @Nullable EndPointMeta endPointMeta) {

        String route = postcard.getRoute();
        Log.d(TAG, "go, route: " + route);

        if (endPointMeta == null) {
            Log.e(TAG, "END POINT NOT FOUND with route " + route + "!!!");
            onNotFound(postcard);
            return false;
        }

        Context context = postcard.getContext();

        Class<?> endpointClass = endPointMeta.getEndPointClass();
        if (endpointClass == null) {
            Log.e(TAG, "CLASS NOT FOUND with route " + route + "!!!");
            onNotFound(postcard);
            return false;
        }

        EndPointType endPointType = endPointMeta.getEndPointType();
        Log.d(TAG, "endPointType: " + endPointType.toString());
        switch (endPointType) {
            case ACTIVITY:
                return startActivity(context, postcard, endpointClass);
            case SERVICE:
                return startService(context, postcard, endpointClass);
            default:
                onNotFound(postcard);
                return false;
        }

    }

    @Nullable
    private static Intent buildActivityIntent(PostcardInternal postcard) {
        if (postcard == null || postcard.getContext() == null) {
            return null;
        }
        String groupName = postcard.getGroup();
        String route = postcard.getRoute();
        EndPointMeta endPointMeta = getEndPointMetaByGroupNameAndRoute(groupName, route);
        if (endPointMeta == null) {
            return null;
        }
        Class<?> endpointClass = endPointMeta.getEndPointClass();
        if (endpointClass == null) {
            Log.e(TAG, "CLASS NOT FOUND with route " + route + "!!!");
            return null;
        }
        if (endPointMeta.getEndPointType() == EndPointType.ACTIVITY) {
            Intent intent = new Intent(postcard.getContext(), endpointClass);
            Bundle extras = postcard.getExtras();
            if (extras != null) {
                intent.putExtras(extras);
            }
            intent.setFlags(postcard.getIntentFlags());
            return intent;
        }
        return null;
    }

    /**
     * 启动服务
     * @param context
     * @param postcard
     * @param endpointClass
     * @return
     */
    private static boolean startService(Context context, PostcardInternal postcard, Class<?> endpointClass) {
        try {
            Intent intent = new Intent(context, endpointClass);
            Bundle extras = postcard.getExtras();
            Bundle urlQuery = postcard.getRouteUrlQueryAsBundle();
            if (extras != null) {
                if (urlQuery != null) {
                    extras.putAll(urlQuery);
                }
                intent.putExtras(extras);
            } else if (urlQuery != null) {
                intent.putExtras(urlQuery);
            }
            intent.setFlags(postcard.getIntentFlags());

            if (postcard.bindService){
                context.bindService(intent, postcard.conn, postcard.bindServiceFlags);
            } else {
                context.startService(intent);
            }

            onNavigated(postcard);

            return true;
        } catch (Exception e) {
            Log.e(TAG, "START SERVICE ERROR ", e);
            onError(postcard, e);
            return false;
        }
    }

    /**
     * 启动activity or fragment
     * @param context
     * @param postcard
     * @param endpointClass
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    private static boolean startActivity(@NonNull Context context, @NonNull PostcardInternal postcard,
                                         @NonNull Class<?> endpointClass) {
        try {
            Intent intent = new Intent(context, endpointClass);
            Bundle extras = postcard.getExtras();
            Bundle urlQuery = postcard.getRouteUrlQueryAsBundle();
            if (extras != null) {
                if (urlQuery != null) {
                    extras.putAll(urlQuery);
                }
                intent.putExtras(extras);
            } else if (urlQuery != null) {
                intent.putExtras(urlQuery);
            }
            intent.setFlags(postcard.getIntentFlags());

            Fragment fragment = postcard.getFragment();
            if (fragment != null) {
                if (postcard.isForResult()) {
                    fragment.startActivityForResult(intent, postcard.getRequestCode(), postcard.options);
                } else {
                    fragment.startActivity(intent, postcard.options);
                }
            } else {
                if (context instanceof Activity) {
                    if (postcard.isForResult()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            ((Activity) context).startActivityForResult(intent, postcard.getRequestCode(), postcard.options);
                        } else {
                            ((Activity) context).startActivityForResult(intent, postcard.getRequestCode());
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            ((Activity) context).startActivity(intent, postcard.options);
                        } else {
                            ((Activity) context).startActivity(intent);
                        }
                    }
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        context.startActivity(intent, postcard.options);
                    } else {
                        context.startActivity(intent);
                    }
                }
            }

            if (context instanceof Activity && postcard.overrideEnterAnim != null && postcard.overrideExitAnim != null) {
                ((Activity) context).overridePendingTransition(postcard.overrideEnterAnim, postcard.overrideExitAnim);
            }

            onNavigated(postcard);

            return true;
        } catch (Exception e) {
            Log.e(TAG, "START ACTIVITY ERR ", e);
            onError(postcard, e);
            return false;
        }
    }

    @NonNull
    static Collection<Interceptor> commonRouteInterceptors() {
        return commonRouteInterceptors;
    }

    static void addCommonRouteInterceptors(@Nullable Interceptor... interceptors) {
        if (interceptors != null && interceptors.length > 0) {
            commonRouteInterceptors.addAll(Arrays.asList(interceptors));
        }
    }
}

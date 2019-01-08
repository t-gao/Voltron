package com.voltron.router.base;

import com.voltron.router.EndPointType;
import com.voltron.router.annotation.EndPoint;

import java.lang.reflect.Method;

import javax.lang.model.element.Element;

public class AnnotationUtil {

    public static EndPointMeta buildEndPointMetaFromAnnotation(EndPoint endPointAnno, Element element,
                                                               EndPointType endPointType) {
        if (endPointAnno == null) {
            return null;
        }

        String scheme = endPointAnno.scheme();
        String host = endPointAnno.host();
        String path = endPointAnno.path();

        String value = endPointAnno.value();

        String groupName = extractGroupNameFromRoute(value);
        if (StringUtils.isEmpty(groupName)) {
            groupName = extractGroupNameFromSchemeHost(scheme, host);
        }

        String route = value;
        if (route.isEmpty()) {
            route = buildRouteFromSchemeHostPath(scheme, host, path);
        }

        if (StringUtils.isEmpty(route)) {
            return null;
        }

        return new EndPointMeta(groupName, scheme, host, path, value, route, element, endPointType);
    }

    public static String extractGroupNameFromSchemeHost(String scheme, String host) {
        return StringUtils.isEmpty(scheme) ? host : scheme;
    }

    /**
     * 按如下顺序确定分组名 group name：
     * 1) 若有 scheme，则以 scheme 为组名；
     * 2) 若有 host，则以 host 为组名；
     * 3) 组名为 null；
     *
     * @param route 路由路径
     * @return 分组名
     */
    public static String extractGroupNameFromRoute(String route) {
        if (StringUtils.isEmpty(route)) {
            return null;
        }

        String rest;
        if (route.contains(AnnotationConsts.SCHEME_SUFFIX)) {
            if (!route.startsWith(AnnotationConsts.SCHEME_SUFFIX)) {
                String[] parts = route.split(AnnotationConsts.SCHEME_SUFFIX);
                return parts[0];
            } else {
                rest = route.substring(AnnotationConsts.SCHEME_SUFFIX.length());
            }
        } else {
            rest = route;
        }

        if (!StringUtils.isEmpty(rest)) {
            return extractGroupNameFromRouteWithoutScheme(rest);
        }
        return null;
    }

    private static String extractGroupNameFromRouteWithoutScheme(String route) {
        if (route.contains(AnnotationConsts.PATH_SEPARATOR)) {
            int firstIdx = route.indexOf(AnnotationConsts.PATH_SEPARATOR);
            if (firstIdx == 0) {
                return extractGroupNameFromRouteWithoutScheme(route.substring(AnnotationConsts.PATH_SEPARATOR.length()));
            } else {
                return route.substring(0, firstIdx);
            }
        } else {
            return null;
        }
    }

    public static String buildRouteFromSchemeHostPath(String scheme, String host, String path) {
        if (host == null) {
            host = "";
        }
        if (path == null) {
            path = "";
        }

        return StringUtils.isEmpty(scheme) ? (host + path) : (scheme + AnnotationConsts.SCHEME_SUFFIX + host + path);
    }

    public static void injectAutowired(Object obj) {
        try {
            if (obj != null) {
                String className = obj.getClass().getName();
                Class classAutowired = Class.forName(className + AnnotationConsts.AUTOWIRED_CLASS_SUFFIX);
                if (classAutowired != null) {
                    Method method = classAutowired.getMethod(AnnotationConsts.AUTOWIRED_METHOD_INJECT , Object.class);
                    if (method != null) {
                        method.invoke(null, obj);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.voltron.router.base;

import java.lang.reflect.Method;
import java.net.URI;

public class AnnotationUtil {

    public static String getSchemeFromRoute(String route) {
        if (StringUtils.isEmpty(route)) {
            return null;
        }

        int schemeSuffixStart = route.indexOf(AnnotationConsts.SCHEME_SUFFIX);
        if (schemeSuffixStart >= 0) {
            return route.substring(0, schemeSuffixStart);
        } else {
            return null;
        }
    }

    public static String getHostFromRoute(String route) {
        if (StringUtils.isEmpty(route)) {
            return null;
        }

        String host = null;

        final String schemeSuffix = AnnotationConsts.SCHEME_SUFFIX;

        int schemeSuffixStart = route.indexOf(schemeSuffix);
        int schemeSuffixLen = schemeSuffix.length();
        if (schemeSuffixStart >= 0) { // 有 scheme
            int dotIdx = route.indexOf(".");
            if (dotIdx > 0) { // 有 "."，即有 host
                host = route.substring(schemeSuffixStart + schemeSuffixLen, dotIdx);
            }
        }

        return host;
    }

    public static String getEndPointKeyFromRoute(String route) {
        if (StringUtils.isEmpty(route)) {
            return null;
        }

        String key;

        final String schemeSuffix = AnnotationConsts.SCHEME_SUFFIX;
        final String pathSeparator = AnnotationConsts.PATH_SEPARATOR;

        int schemeSuffixStart = route.indexOf(schemeSuffix);
        int schemeSuffixLen = schemeSuffix.length();
        if (schemeSuffixStart >= 0) { // 有 scheme
            String hostAndPath = route.substring(schemeSuffixStart + schemeSuffixLen);
            int firstDotIndex = hostAndPath.indexOf(".");
            if (firstDotIndex >= 0) { // 有 "."，即有 host
                int firstSeparatorIndex = hostAndPath.indexOf(pathSeparator);

                if (firstSeparatorIndex >= firstDotIndex && firstSeparatorIndex < hostAndPath.length() - 1) {
                    // "/" 在 "." 之后，且不是最后一个字符（即有path），取 "/" 之后部分（即path部分）作为key
                    key = hostAndPath.substring(firstSeparatorIndex);
                } else {
                    // 否则（没有path），取 "." 之前部分（即host）作为key
                    key = hostAndPath.substring(0, firstDotIndex);
                }
            } else { // 没有 "."
                key = hostAndPath;
            }
        } else { // 没有 scheme，将整个 route 视为 path
            key = route;
        }

        if (key.startsWith(pathSeparator)) {
            // 去除开头的 "/"
            key = key.substring(pathSeparator.length());
        }

        int queryStart = key.indexOf(AnnotationConsts.URL_QUERY_CHAR);
        if (queryStart >= 0) {
            key = key.substring(0, queryStart);
        }

        return key;
    }

    /**
     * FIXME: 该方法不支持 url 里带下划线 "_"
     * 从 route url 中取出 目标点的 key（开头不含 '/'）
     * @param route
     * @return endPointKey
     */
    public static String getEndPointKeyFromRoute_Old(String route) {
        if (!StringUtils.isEmpty(route)) {
            URI uri = URI.create(route);
            String path = uri.getPath();
            String host = uri.getHost();
            if (StringUtils.isEmpty(path)) {
                if (host != null && host.contains(".")) {
                    // 如route为 "hfqdl://hello.com"，返回 "hello"; (去除结尾的 ".com")
                    return host.substring(0, host.lastIndexOf("."));
                } else {
                    // 如route为 "hfqdl://hello"，返回 "hello"
                    return host;
                }
            } else {
                if (StringUtils.isEmpty(host)) {
                    // 如route为 "hello/world?a=0&b=bval"，返回 "hello/world"
                    return path;
                } else {
                    // 如route为 "hfqdl://haofenqi.com/hello/world"，返回 "hello/world"
                    if (path != null && path.startsWith(AnnotationConsts.PATH_SEPARATOR)) {
                        // 去除 "/hello/world" 开头的 "/"
                        return path.substring(AnnotationConsts.PATH_SEPARATOR.length());
                    } else {
                        return path;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 从 route 提取分组名。
     *
     * 规则：
     * 先从 route 里提取 path 部分；
     * 若 path 有分段符 "/"：
     *      group 取 path 的最后一段之前的部分。
     *          例如，"hello/world/detail" 的 group 为 "hello/world"；而 "hello/world"的 group 为 "hello"
     * 若 path 没有分段：
     *      group 为空字符串。
     *          例如，"helloworld" 的 group 为 ""
     *
     * @param route 路由路径
     * @return 分组名
     */
    public static String extractGroupNameFromRoute(String route) {
        String path = getEndPointKeyFromRoute(route);
        if (!StringUtils.isEmpty(path)) {
            int lastIndex = path.lastIndexOf(AnnotationConsts.PATH_SEPARATOR);
            if (lastIndex > 0) {
                return path.substring(0, lastIndex);
            } else {
                return "";
            }
        }
        return "";
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

package com.voltron.router.base;

import com.voltron.router.EndPointType;

/**
 * 封装了路由目标点的信息，如目标点类型、目标点Class等。
 * 该信息通过用由发起方传递来的路由 route（其中包含目标点的唯一key）查找而来。
 *
 * NOTE: 由路由框架内部使用，业务层不应该直接使用该类。
 */
public class EndPointMeta {

    /**
     * 目标唯一key
     */
    private String endPointKey;

    private String group;

    private Class endPointClass;

    private String scheme;
//    private String host;
//    private String path;

    /**
     * 完整路由路径，[scheme]://[host][path]，或 [host][path]
     * 如果注解指定了[value]，则取 [value] 为 route
     */
    private String route;

    /**
     * 完整路由路径，方便一次性指定 scheme+host+path
     */
    private String value;

    /**
     * 目标类类型
     */
    private EndPointType endPointType;

    private EndPointMeta(String endPointKey, String group, String scheme, String value, String route,
                         Class endPointClass, EndPointType endPointType) {
        this.endPointKey = endPointKey;
        this.group = group;
        this.scheme = scheme;
//        this.host = host;
//        this.path = path;
        this.value = value;
        this.route = route;
        this.endPointClass = endPointClass;
        this.endPointType = endPointType;
    }

    public String getEndPointKey() {
        return endPointKey;
    }

    public String getGroup() {
        return group;
    }

//    public String getPath() {
//        return path;
//    }

    public String getScheme() {
        return scheme;
    }

//    public String getHost() {
//        return host;
//    }

    public String getRoute() {
        return route;
    }

    public String getValue() {
        return value;
    }

    public Class getEndPointClass() {
        return endPointClass;
    }

    public EndPointType getEndPointType() {
        return endPointType;
    }

    public static EndPointMeta build(String endPointKey, String group, String scheme, String value,
                                     String route, Class endPointClass, EndPointType endPointType) {
        return new EndPointMeta(endPointKey, group, scheme, value, route, endPointClass, endPointType);
    }
}

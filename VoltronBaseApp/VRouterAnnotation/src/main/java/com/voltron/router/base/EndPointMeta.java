package com.voltron.router.base;

import com.voltron.router.EndPointType;

import javax.lang.model.element.Element;

public class EndPointMeta {
    private String group;

    private Class endPointClass;

    private Element element;


    private String scheme;
    private String host;
    private String path;

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

    private EndPointMeta(String group, String scheme, String host, String path, String value,
                         String route, Class endPointClass, EndPointType endPointType) {
        this.group = group;
        this.scheme = scheme;
        this.host = host;
        this.path = path;
        this.value = value;
        this.route = route;
        this.endPointClass = endPointClass;
        this.endPointType = endPointType;
    }

    public EndPointMeta(String group, String scheme, String host, String path, String value,
                        String route, Element element, EndPointType endPointType) {
        this.group = group;
        this.scheme = scheme;
        this.host = host;
        this.path = path;
        this.value = value;
        this.route = route;
        this.element = element;
        this.endPointType = endPointType;
    }

    public Element getElement() {
        return element;
    }

    public String getGroup() {
        return group;
    }

    public String getPath() {
        return path;
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

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

    public static EndPointMeta build(String group, String scheme, String host, String path, String value,
                                     String route, Class endPointClass, EndPointType endPointType) {
        return new EndPointMeta(group, scheme, host, path, value, route, endPointClass, endPointType);
    }
}

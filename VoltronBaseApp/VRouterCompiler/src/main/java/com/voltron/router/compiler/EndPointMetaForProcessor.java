package com.voltron.router.compiler;

import com.voltron.router.EndPointType;

import javax.lang.model.element.Element;

/**
 * 该类仅限于编译过程中的注解处理器使用
 */
class EndPointMetaForProcessor {
    private String group;

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

    EndPointMetaForProcessor(String group, String scheme, String host, String path, String value,
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

    Element getElement() {
        return element;
    }

    String getGroup() {
        return group;
    }

    String getPath() {
        return path;
    }

    String getScheme() {
        return scheme;
    }

    String getHost() {
        return host;
    }

    String getRoute() {
        return route;
    }

    String getValue() {
        return value;
    }

    EndPointType getEndPointType() {
        return endPointType;
    }

}

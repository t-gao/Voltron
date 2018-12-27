package com.voltron.router.base;

import javax.lang.model.element.Element;

public class EndPointMeta {
    private String group;

    /**
     * 完整路由路径，包含group
     */
    private String path;

    private Class endPointClass;

    private Element element;

    EndPointMeta(String group, String path, Class endPointClass) {
        this.group = group;
        this.path = path;
        this.endPointClass = endPointClass;
    }

    public EndPointMeta(String group, String path, Element element) {
        this.group = group;
        this.path = path;
        this.element = element;
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

    public Class getEndPointClass() {
        return endPointClass;
    }

    public static EndPointMeta build(String group, String path, Class endPointClass) {
        return new EndPointMeta(group, path, endPointClass);
    }
}

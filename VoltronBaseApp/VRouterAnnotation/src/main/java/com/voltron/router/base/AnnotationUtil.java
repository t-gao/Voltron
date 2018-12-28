package com.voltron.router.base;

import com.voltron.router.annotation.EndPoint;

import javax.lang.model.element.Element;

public class AnnotationUtil {

    /**
     * 从注解 {@link EndPoint EndPoint} 的 value 中截取出分组 group 的值
     * @param path 路由path，即 EndPoint 注解的 value
     * @return 分组名
     */
    public static String extractGroupNameFromPath(String path) {
        String group = null;
        if (StringUtils.isNotEmpty(path)) {
            int idx = path.lastIndexOf("/");
            if (idx > 0 && idx < path.length() - 1) {
                group = path.substring(0, idx).replace("/", "");
            }
        }

        return group;
    }

    public static EndPointMeta buildEndPointMetaFromAnnotation(EndPoint endPointAnno, Element element) {
        if (endPointAnno == null) {
            return null;
        }

        String path = endPointAnno.value();
        if (StringUtils.isEmpty(path)) {
            return null;
        }

        String groupName = endPointAnno.group();
        if (StringUtils.isEmpty(groupName)) {
            groupName = AnnotationUtil.extractGroupNameFromPath(path);
        }

        return new EndPointMeta(groupName, path, element);
    }

}

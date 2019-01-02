package com.voltron.router.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 路由端点注解
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface EndPoint {

    /**
     * 如 http 或 https，或自定义 scheme
     */
    String scheme() default "";

    /**
     * 如 voltron.com
     */
    String host() default "";

    /**
     * 必须以 "/" 开头，如 "/demopage"
     */
    String path() default "";

    /**
     * 完整的路由路径，包括 scheme、host 和 path。优先级高于单独指定的scheme、host 和 path。
     */
    String value() default "";
}

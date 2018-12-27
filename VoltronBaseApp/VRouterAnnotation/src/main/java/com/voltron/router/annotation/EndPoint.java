package com.voltron.router.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface EndPoint {

    // 显式指定分组名
    String group() default "";

    /**
     * 完整的路由路径，包括 group 和 path。group 部分可不配置，若配置，group 和 path 需要由 "/" 分隔。
     * 合法的 value 取值举例如下：
     *  0. "/ab/cd"   --  group 为 "ab", path 为 "/ab/cd"
     *  1. "ab/cd"   --  group 为 "ab", path 为 "ab/cd"
     *  2. "/ab/cd/"   --  group 为 "ab", path 为 "/ab/cd/"
     *  3. "efg"   --  group 为 空, path 为 "efg"
     *  4. "/xyz"   --  group 为 空, path 为 "/xyz"
     *  5. "op/"   --  group 为 空, path 为 "op/"
     */
    String value() default "";
}

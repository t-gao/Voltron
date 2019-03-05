package com.voltron.router.gradle.autowired.autoinject;

import com.voltron.router.gradle.autowired.BaseAutowiredTransform;

/**
 * Inserts VRouter.inject() call to the beginning of an Activity's onCreate() method.
 *
 * 在 Activity 的 onCreate() 方法开头插入一句 VRouter.inject() 调用。
 */
public class AutoInjectAutowiredTransform extends BaseAutowiredTransform {
    public AutoInjectAutowiredTransform() {
        super(false);
    }
}

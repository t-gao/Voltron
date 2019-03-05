package com.voltron.router.gradle.autowired.preprocess;

import com.voltron.router.gradle.autowired.BaseAutowiredTransform;

/**
 * Pre-processes classes with @Autowired annotated fields, if an Activity does not have an
 * overridden onCreate() method, create on for it，as:
 * ```
 *     @Override
 *     public void onCreate(@Nullable Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *     }
 * ```
 *
 * 预处理包含 @Autowired 注解的字段的 Activity 类，如果没有覆盖 onCreate() 方法，自动创建一个。
 *
 */
public class PreProcessAutowiredTransform extends BaseAutowiredTransform {
    public PreProcessAutowiredTransform() {
        super(true);
    }
}

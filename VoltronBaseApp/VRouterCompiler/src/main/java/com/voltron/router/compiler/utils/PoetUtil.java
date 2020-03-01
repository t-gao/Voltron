package com.voltron.router.compiler.utils;

public class PoetUtil {
    public static String getGroupJavaFileName(String moduleName, String groupName) {
        if (groupName == null) {
            groupName = "";
        } else {
            groupName = groupName.replace("/", "_");
        }
        return "VRouter__M__" + moduleName + "__G__" + groupName;
    }
}

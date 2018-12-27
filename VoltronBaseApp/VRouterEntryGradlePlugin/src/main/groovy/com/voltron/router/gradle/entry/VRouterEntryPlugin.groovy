//package com.voltron.router.gradle.entry
//
//import com.voltron.router.gradle.utils.Logger
//import org.gradle.api.Plugin
//import org.gradle.api.Project
//
//import com.android.build.gradle.AppPlugin
//
//class VRouterEntryPlugin implements Plugin<Project> {
//
//    static int moduleCount = 0
//
//    @Override
//    void apply(Project project) {
//        Logger.init(project)
//        def isApp = project.plugins.hasPlugin(AppPlugin)
//        Logger.lifecycle("Entry plugin apply, isApp: " + isApp + ", name: " + project.name)
//        moduleCount++
//        // generate router entry code only in application module
//        if (isApp) {
//
//        }
//    }
//}
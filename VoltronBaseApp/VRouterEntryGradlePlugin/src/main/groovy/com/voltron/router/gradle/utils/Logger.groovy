//package com.voltron.router.gradle.utils
//
//import org.gradle.api.Project
//
///**
// * Format log
// *
// * @author zhilong <a href="mailto:zhilong.lzl@alibaba-inc.com">Contact me.</a>
// * @version 1.0
// * @since 2017/12/18 下午2:43
// */
//class Logger {
//    static org.gradle.api.logging.Logger logger
//
//    static void init(Project project) {
//        logger = project.getLogger()
//    }
//
//    static void i(String info) {
//        if (null != info && null != logger) {
//            logger.info("VRouter::Gradle Plugin >>> " + info)
//        }
//    }
//
//    static void d(String debug) {
//        if (null != debug && null != logger) {
//            logger.debug("VRouter::Gradle Plugin >>> " + debug)
//        }
//    }
//
//    static void e(String error) {
//        if (null != error && null != logger) {
//            logger.error("VRouter::Gradle Plugin >>> " + error)
//        }
//    }
//
//    static void w(String warning) {
//        if (null != warning && null != logger) {
//            logger.warn("VRouter::Gradle Plugin >>> " + warning)
//        }
//    }
//
//    static void lifecycle(String lifecycleMsg) {
//        if (null != lifecycleMsg && null != logger) {
//            logger.lifecycle("VRouter::Gradle Plugin >>> " + lifecycleMsg)
//        }
//    }
//}

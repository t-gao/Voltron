package com.voltron.router.compiler.utils;

import com.voltron.router.compiler.Constants;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class Logger {
    private Messager msgr;

    public Logger(Messager messager) {
        msgr = messager;
    }

    public void i(CharSequence msg) {
        msgr.printMessage(Diagnostic.Kind.NOTE, Constants.LOG_PREFIX + msg);
    }

    public void i(CharSequence msg, Element element) {
        msgr.printMessage(Diagnostic.Kind.NOTE, Constants.LOG_PREFIX + msg, element);
    }

    public void e(CharSequence msg) {
        msgr.printMessage(Diagnostic.Kind.ERROR, Constants.LOG_PREFIX + msg);
    }

    public void e(CharSequence msg, Element element) {
        msgr.printMessage(Diagnostic.Kind.ERROR, Constants.LOG_PREFIX + msg, element);
    }

    public void e(Throwable tr, Element element) {
        msgr.printMessage(Diagnostic.Kind.ERROR, Constants.LOG_PREFIX + tr.getMessage(), element);
    }
}

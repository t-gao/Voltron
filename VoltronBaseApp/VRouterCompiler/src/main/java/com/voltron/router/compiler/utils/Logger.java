package com.voltron.router.compiler.utils;

import com.voltron.router.compiler.Constants;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class Logger {
    private Messager msgr;
    private CharSequence tag;

    public Logger(CharSequence tag, Messager messager) {
        msgr = messager;
        this.tag = tag;
        if (this.tag == null) {
            this.tag = "";
        }
    }

    public void i(CharSequence msg) {
        msgr.printMessage(Diagnostic.Kind.NOTE, Constants.LOG_PREFIX + tag  + " " + msg);
    }

    public void i(CharSequence msg, Element element) {
        msgr.printMessage(Diagnostic.Kind.NOTE, Constants.LOG_PREFIX + tag  + " " + msg, element);
    }

    public void w(CharSequence msg) {
        msgr.printMessage(Diagnostic.Kind.WARNING, Constants.LOG_PREFIX + tag  + " " + msg);
    }

    public void e(CharSequence msg) {
        msgr.printMessage(Diagnostic.Kind.ERROR, Constants.LOG_PREFIX + tag  + " " + msg);
    }

    public void e(CharSequence msg, Element element) {
        msgr.printMessage(Diagnostic.Kind.ERROR, Constants.LOG_PREFIX + tag  + " " + msg, element);
    }

    public void e(Throwable tr, Element element) {
        msgr.printMessage(Diagnostic.Kind.ERROR, Constants.LOG_PREFIX + tag  + " " + tr.getMessage(), element);
    }
}

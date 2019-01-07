package com.voltron.router.api;

// 针对://之前的Scheme - eg：[testscheme://]，则scheme为testscheme
public interface IRouteSchemeHandler {
    void handle(String route);
}

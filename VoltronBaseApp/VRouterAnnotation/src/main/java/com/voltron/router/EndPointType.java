package com.voltron.router;

public enum EndPointType {

    ACTIVITY(0, "android.app.Activity"),
    SERVICE(1, "android.app.Service"),
    FRAGMENT(2, "android.support.v4.app.Fragment"),
    OTHER(-1, "");

    int id;
    String className;

    EndPointType(int id, String className) {
        this.id = id;
        this.className = className;
    }
}

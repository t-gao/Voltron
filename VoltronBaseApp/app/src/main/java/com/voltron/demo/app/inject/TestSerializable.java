package com.voltron.demo.app.inject;

import java.io.Serializable;

/**
 * JUST FOR TEST
 */
public class TestSerializable implements Serializable {
    public String name;
    public int id;

    public TestSerializable() {
    }

    public TestSerializable(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String toString() {
        return "TestSerializable{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}';
    }
}

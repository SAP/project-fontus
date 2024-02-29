package com.sap.fontus.gdpr.servlet;

class Widget {
    private String name;
    private static String className = "Widget";
    Widget(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
    public static void setClassName(String s) {
        className = s;
    }

    public static String getClassName() {
        return className;
    }
}

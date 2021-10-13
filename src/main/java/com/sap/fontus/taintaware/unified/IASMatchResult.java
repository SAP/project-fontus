package com.sap.fontus.taintaware.unified;

public interface IASMatchResult {
    int end();
    int end(int group);
    IASString group();
    IASString group(int group);
    int groupCount();
    int start();
    int start(int group);
}

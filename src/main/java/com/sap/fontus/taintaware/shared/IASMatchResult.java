package com.sap.fontus.taintaware.shared;

public interface IASMatchResult {
    int end();
    int end(int group);
    IASStringable group();
    IASStringable group(int group);
    int groupCount();
    int start();
    int start(int group);
}

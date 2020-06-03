package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;


public interface IASMatchResult {
    int end();
    int end(int group);
    IASString group();
    IASString group(int group);
    int groupCount();
    int start();
    int start(int group);
}

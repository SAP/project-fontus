package de.tubs.cs.ias.asm_test.taintaware.shared;

public interface IASLazyAware extends IASTaintRangeAware {
    int length();
    IASStringable toIASString();
}

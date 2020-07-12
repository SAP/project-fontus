package de.tubs.cs.ias.asm_test.taintaware.shared;

public interface IASFactory {
    IASStringable createString(String s);
    IASStringable valueOf(Object o);
    IASStringBuilderable createStringBuilder();
    IASStringBuilderable createStringBuilder(IASStringable string);
}

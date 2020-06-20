package de.tubs.cs.ias.asm_test.taintaware.shared;

public interface IASFactory {
    IASStringBuilderable createStringBuilder();
    IASStringable createString(String s);
    IASStringBuilderable createStringBuilder(IASStringable string);
}

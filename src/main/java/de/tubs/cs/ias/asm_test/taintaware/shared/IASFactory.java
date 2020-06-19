package de.tubs.cs.ias.asm_test.taintaware.shared;

import java.nio.charset.Charset;

public interface IASFactory {
    IASStringBuilderable createStringBuilder();
    IASStringable createString(String s);
}

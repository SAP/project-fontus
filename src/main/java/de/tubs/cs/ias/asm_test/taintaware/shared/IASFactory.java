package de.tubs.cs.ias.asm_test.taintaware.shared;

import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface IASFactory {
    IASStringable createString(String s);
    IASStringable valueOf(Object o);
    IASStringBuilderable createStringBuilder();
    IASStringBuilderable createStringBuilder(IASStringable string);
    IASStringBuilderable createStringBuilder(StringBuilder param);
    Class<? extends IASStringable> getStringClass();
    Class<? extends IASStringable[]> getStringArrayClass();

    IASStringBuilderable createStringBuffer(StringBuffer param);
    IASFormatterable createFormatter(Formatter param);
    IASPatternable createPattern(Pattern param);
    IASMatcherable createMatcher(Matcher param);
}

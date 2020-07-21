package de.tubs.cs.ias.asm_test.taintaware.shared;

import java.util.Formatter;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface IASFactory {
    Class<? extends IASStringable> getStringClass();

    Class<? extends IASStringable[]> getStringArrayClass();

    IASStringable createString(String s);

    IASStringable valueOf(Object o);

    IASAbstractStringBuilderable createStringBuilder();

    IASStringBuilderable createStringBuilder(IASStringable param);

    IASStringBuilderable createStringBuilder(StringBuilder param);

    IASStringBufferable createStringBuffer(StringBuffer param);

    IASFormatterable createFormatter(Formatter param);

    IASPatternable createPattern(Pattern param);

    IASMatcherable createMatcher(Matcher param);

    IASProperties createProperties(Properties param);
}

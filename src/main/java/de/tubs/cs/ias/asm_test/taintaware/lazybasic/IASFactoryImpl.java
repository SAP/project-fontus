package de.tubs.cs.ias.asm_test.taintaware.lazybasic;

import de.tubs.cs.ias.asm_test.taintaware.shared.*;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASProperties;

import java.util.Formatter;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IASFactoryImpl implements IASFactory {
    @Override
    public IASStringBuilder createStringBuilder() {
        return new IASStringBuilder();
    }

    @Override
    public IASString createString(String s) {
        return IASString.fromString(s);
    }

    @Override
    public IASString valueOf(Object o) {
        return IASString.valueOf(o);
    }

    @Override
    public IASStringBuilder createStringBuilder(IASStringable param) {
        return new IASStringBuilder(param);
    }

    @Override
    public Class<? extends IASStringable> getStringClass() {
        return IASString.class;
    }

    @Override
    public Class<? extends IASStringable[]> getStringArrayClass() {
        return IASString[].class;
    }

    @Override
    public IASStringBuilder createStringBuilder(StringBuilder string) {
        return IASStringBuilder.fromStringBuilder(string);
    }

    @Override
    public IASStringBuffer createStringBuffer(StringBuffer param) {
        return IASStringBuffer.fromStringBuffer(param);
    }

    @Override
    public IASFormatter createFormatter(Formatter param) {
        return IASFormatter.fromFormatter(param);
    }

    @Override
    public IASPattern createPattern(Pattern param) {
        return IASPattern.fromPattern(param);
    }

    @Override
    public IASMatcher createMatcher(Matcher param) {
        return IASMatcher.fromMatcher(param);
    }

    @Override
    public IASProperties createProperties(Properties param) {
        return de.tubs.cs.ias.asm_test.taintaware.lazybasic.IASProperties.fromProperties(param);
    }
}

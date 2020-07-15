package de.tubs.cs.ias.asm_test.taintaware.bool;

import de.tubs.cs.ias.asm_test.taintaware.shared.*;

import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IASFactoryImpl implements IASFactory {
    @Override
    public IASStringBuilder createStringBuilder() {
        return new IASStringBuilder();
    }

    @Override
    public IASString createString(String s) {
        return new IASString(s);
    }

    @Override
    public IASString valueOf(Object o) {
        return IASString.valueOf(o);
    }

    @Override
    public IASStringBuilder createStringBuilder(IASStringable string) {
        return new IASStringBuilder(string);
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
}

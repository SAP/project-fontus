package de.tubs.cs.ias.asm_test.taintaware.bool;

import de.tubs.cs.ias.asm_test.taintaware.shared.*;

import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IASFactoryImpl implements IASFactory {
    @Override
    public IASStringBuilderable createStringBuilder() {
        return new IASStringBuilder();
    }

    @Override
    public IASStringable createString(String s) {
        return new IASString(s);
    }

    @Override
    public IASStringable valueOf(Object o) {
        return IASString.valueOf(o);
    }

    @Override
    public IASStringBuilderable createStringBuilder(IASStringable string) {
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
    public IASStringBuilderable createStringBuilder(StringBuilder string) {
        return IASStringBuilder.fromStringBuilder(string);
    }

    @Override
    public IASStringBuilderable createStringBuffer(StringBuffer param) {
        return IASStringBuffer.fromStringBuffer(param);
    }

    @Override
    public IASFormatterable createFormatter(Formatter param) {
        return IASFormatter.fromFormatter(param);
    }

    @Override
    public IASPatternable createPattern(Pattern param) {
        return IASPattern.fromPattern(param);
    }

    @Override
    public IASMatcherable createMatcher(Matcher param) {
        return IASMatcher.fromMatcher(param);
    }
}

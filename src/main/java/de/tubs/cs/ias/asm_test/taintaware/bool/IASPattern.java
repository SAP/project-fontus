package de.tubs.cs.ias.asm_test.taintaware.bool;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

import java.util.regex.Pattern;

public class IASPattern {
    private final Pattern pattern;
    private final IASString patternString;

    public IASPattern(Pattern pattern) {
        this(pattern, IASString.valueOf(pattern.pattern()));
    }

    public IASPattern(Pattern pattern, IASString patternString) {
        this.pattern = pattern;
        this.patternString = patternString;
    }

    public static IASPattern compile(IASString regex) {
        return new IASPattern(Pattern.compile(regex.getString()), regex);
    }

    public static IASPattern compile(IASString regex, int flags) {
        return new IASPattern(Pattern.compile(regex.getString(), flags), regex);
    }

    public int flags() {
        return pattern.flags();
    }

    public IASMatcher matcher(CharSequence input) {
        return new IASMatcher(this, input);
    }

    public static boolean matches(IASString regex, CharSequence input) {
        return compile(regex).matcher(input).matches();
    }

    public IASString pattern() {
        return this.patternString;
    }

    public static IASString quote(IASString s) {
        return new IASString(Pattern.quote(s.getString()), s.isTainted());
    }

    public IASString[] split(CharSequence input) {
        boolean tainted = (input instanceof IASTaintAware) && ((IASTaintAware) input).isTainted();
        IASString[] result = IASStringUtils.convertStringArray(this.pattern.split(input));
        if (result != null && tainted) {
            for (IASString s : result) {
                s.setTaint(true);
            }
        }
        return result;
    }

    public IASString[] split(CharSequence input, int limit) {
        boolean tainted = (input instanceof IASTaintAware) && ((IASTaintAware) input).isTainted();
        IASString[] result = IASStringUtils.convertStringArray(this.pattern.split(input, limit));
        if (result != null && tainted) {
            for (IASString s : result) {
                s.setTaint(true);
            }
        }
        return result;
    }

    public String toString() {
        return pattern.toString();
    }

    public IASString toIASString() {
        return patternString;
    }

    public Pattern getPattern() {
        return this.pattern;
    }
}

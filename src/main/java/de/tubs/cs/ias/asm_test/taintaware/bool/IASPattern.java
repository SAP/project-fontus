package de.tubs.cs.ias.asm_test.taintaware.bool;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASPatternable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringUtils;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

import java.util.regex.Pattern;

public final class IASPattern implements IASPatternable {
    private final Pattern pattern;
    private final IASString patternString;

    public IASPattern(Pattern pattern) {
        this(pattern, IASString.valueOf(pattern.pattern()));
    }

    public IASPattern(Pattern pattern, IASStringable patternString) {
        this.pattern = pattern;
        this.patternString = (IASString) patternString;
    }

    public static IASPattern compile(IASStringable regex) {
        return new IASPattern(Pattern.compile(regex.getString()), regex);
    }

    public static IASPattern compile(IASStringable regex, int flags) {
        return new IASPattern(Pattern.compile(regex.getString(), flags), regex);
    }

    public static IASPatternable fromPattern(Pattern param) {
        if (param == null) {
            return null;
        }
        return new IASPattern(param);
    }

    public int flags() {
        return pattern.flags();
    }

    public IASMatcher matcher(CharSequence input) {
        return new IASMatcher(this, input);
    }

    public static boolean matches(IASStringable regex, CharSequence input) {
        return compile(regex).matcher(input).matches();
    }

    public IASString pattern() {
        return this.patternString;
    }

    public static IASString quote(IASStringable s) {
        return new IASString(Pattern.quote(s.getString()), s.isTainted());
    }

    public IASString[] split(CharSequence input) {
        boolean tainted = (input instanceof IASTaintAware) && ((IASTaintAware) input).isTainted();
        IASString[] result = (IASString[]) IASStringUtils.convertStringArray(this.pattern.split(input));
        if (result != null && tainted) {
            for (IASString s : result) {
                s.setTaint(true);
            }
        }
        return result;
    }

    public IASString[] split(CharSequence input, int limit) {
        boolean tainted = (input instanceof IASTaintAware) && ((IASTaintAware) input).isTainted();
        IASString[] result = (IASString[]) IASStringUtils.convertStringArray(this.pattern.split(input, limit));
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

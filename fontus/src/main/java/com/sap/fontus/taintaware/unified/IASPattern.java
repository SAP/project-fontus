package com.sap.fontus.taintaware.unified;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class IASPattern {
    private final Pattern pattern;
    private final IASString patternString;

    public IASPattern(Pattern pattern) {
        this(IASString.valueOfInternal(pattern.pattern()), pattern.flags());
    }

    private IASPattern(IASString p, int f) {
        this.pattern = Pattern.compile(p.toString(), f);
        this.patternString = p;
    }

    public static IASPattern fromPattern(Pattern param) {
        if (param == null) {
            return null;
        }
        return new IASPattern(param);
    }

    public Predicate<IASString> asPredicate() {
        return string -> this.pattern.asPredicate().test(string.getString());
    }

    public static IASPattern compile(IASString string) {
        return compile(string, 0);
    }

    public static IASPattern compile(IASString string, int flags) {
        return new IASPattern(string, flags);
    }

    public int flags() {
        return this.pattern.flags();
    }

    public IASMatcher matcher(CharSequence input) {
        return new IASMatcher(this, input);
    }

    public static boolean matches(IASString regex, CharSequence input) {
        return Pattern.matches(regex.getString(), input);
    }

    public IASString pattern() {
        return this.patternString;
    }

    public static IASString quote(IASString s) {
        // From Apache Harmony
        IASStringBuilder sb = new IASStringBuilder().append("\\Q"); //$NON-NLS-1$
        int apos = 0;
        int k;
        while ((k = s.indexOf(new IASString("\\E"), apos)) >= 0) { //$NON-NLS-1$
            sb.append(s.substring(apos, k + 2)).append("\\\\E\\Q"); //$NON-NLS-1$
            apos = k + 2;
        }

        return sb.append(s.substring(apos)).append("\\E").toIASString(); //$NON-NLS-1$
    }

    public IASString[] split(CharSequence input) {
        return this.split(input, 0);
    }

    public IASString[] split(CharSequence input, int limit) {
        IASString string = IASString.valueOfInternal(input);
        IASMatcher matcher = this.matcher(string);

        ArrayList<IASString> result = new ArrayList<>();
        boolean isLimited = limit > 0;
        int start = 0;
        while (matcher.find()) {
            if (isLimited && result.size() >= limit - 1) {
                break;
            }

            int matchSize = matcher.end() - matcher.start();
            if (!result.isEmpty() || matchSize >= 0) {
                int end = matcher.start();

                IASString part = string.substring(start, end);
                result.add(part);

            }
            start = matcher.end();
        }

        if (start < string.length() || limit < 0) {
            IASString endPart = string.substring(start);
            result.add(endPart);
        } else if (start == 0 && string.isEmpty()) {
            result.add(string);
        }

            return result.toArray(new IASString[0]);
    }

    public Stream<IASString> splitAsStream(CharSequence input) {
        return Arrays.stream(this.split(input));
    }

    public IASString toIASString() {
        return this.pattern();
    }

    @Override
    public int hashCode() {
        return this.pattern.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.pattern.equals(obj);
    }

    @Override
    public String toString() {
        return this.pattern.toString();
    }

    public Pattern getPattern() {
        return this.pattern;
    }
}

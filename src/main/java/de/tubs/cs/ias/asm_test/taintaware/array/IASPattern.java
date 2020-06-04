package de.tubs.cs.ias.asm_test.taintaware.array;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class IASPattern {
    private final Pattern pattern;
    private final IASString patternString;

    public IASPattern(Pattern pattern) {
        this(IASString.valueOf(pattern.pattern()), pattern.flags());
    }

    private IASPattern(IASStringable p, int f) {
        this.pattern = Pattern.compile(p.getString(), f);
        this.patternString = (IASString) p;
    }

    public Predicate<IASString> asPredicate() {
        return string -> this.pattern.asPredicate().test(string.getString());
    }

    public static IASPattern compile(IASStringable string) {
        return compile(string, 0);
    }

    public static IASPattern compile(IASStringable string, int flags) {
        return new IASPattern(string, flags);
    }

    public int flags() {
        return this.pattern.flags();
    }

    public IASMatcher matcher(CharSequence input) {
        return new IASMatcher(this, input);
    }

    public static boolean matches(IASStringable regex, CharSequence input) {
        return Pattern.matches(regex.getString(), input);
    }

    public IASString pattern() {
        return this.patternString;
    }

    public static IASString quote(IASStringable s) throws IOException {
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
        return split(input, 0);
    }

    public IASString[] split(CharSequence input, int limit) {
        IASString string = IASString.valueOf(input);
        IASMatcher matcher = matcher(string);

        ArrayList<IASString> result = new ArrayList<>();
        boolean isLimited = limit > 0;
        int start = 0;
        while (matcher.find()) {
            if (isLimited && result.size() >= limit - 1) {
                break;
            }

            int matchSize = matcher.end() - matcher.start();
            if (result.size() != 0 || matchSize > 0) {
                int end = matcher.start();

                IASString part = string.substring(start, end);
                result.add(part);

            }
            start = matcher.end();
        }

        if (start < string.length() || limit < 0) {
            IASString endPart = string.substring(start);
            result.add(endPart);
        } else if (start == 0 && string.length() == 0) {
            result.add(string);
        }

        return result.toArray(new IASString[0]);
    }

    public Stream<IASString> splitAsStream(CharSequence input) {
        return Arrays.stream(split(input));
    }

    public IASString toIASString() {
        return pattern();
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
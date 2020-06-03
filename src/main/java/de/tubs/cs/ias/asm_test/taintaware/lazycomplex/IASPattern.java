package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations.SplitOperation;

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

    private IASPattern(IASString p, int f) {
        this.pattern = Pattern.compile(p.toString(), f);
        this.patternString = p;
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

    public static IASString quote(IASString s) throws IOException {
        // From Apache Harmony
        IASStringBuilder sb = (IASStringBuilder) new IASStringBuilder().append("\\Q"); //$NON-NLS-1$
        int apos = 0;
        int k;
        while ((k = s.indexOf(new IASString("\\E"), apos)) >= 0) { //$NON-NLS-1$
            sb.append(s.substring(apos, k + 2)).append("\\\\E\\Q"); //$NON-NLS-1$
            apos = k + 2;
        }

        return (IASString) ((IASStringBuilder) sb.append(s.substring(apos)).append("\\E")).toIASString(); //$NON-NLS-1$
    }

    public IASString[] split(CharSequence input) {
        return split(input, 0);
    }

    public IASString[] split(CharSequence input, int limit) {
        IASString inputString = IASString.valueOf(input);
        String[] splitted = this.pattern.split(input, limit);
        IASString[] strings = new IASString[splitted.length];
        for (int i = 0; i < splitted.length; i++) {
            strings[i] = inputString.derive(splitted[i], new SplitOperation(this.patternString.getString(), i));
        }
        return strings;
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

package de.tubs.cs.ias.asm_test.taintaware.lazybasic;

import de.tubs.cs.ias.asm_test.taintaware.lazybasic.operation.DeleteLayer;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASPatternable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class IASPattern implements IASPatternable {
    private final Pattern pattern;
    private final IASString patternString;

    public IASPattern(Pattern pattern) {
        this(IASString.valueOf(pattern.pattern()), pattern.flags());
    }

    private IASPattern(IASStringable p, int f) {
        this.pattern = Pattern.compile(p.toString(), f);
        this.patternString = (IASString) p;
    }

    public static IASPatternable fromPattern(Pattern param) {
        if (param == null) {
            return null;
        }
        return new IASPattern(param);
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

    public static IASString quote(IASStringable s) {
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

        String[] strings = string.getString().split(this.patternString.getString(), limit);
        IASString[] result = new IASString[strings.length];
        int start = 0;
        for (int i = 0; i < strings.length; i++) {
            boolean found = matcher.find();

            int end = Integer.MAX_VALUE;
            if (found && i != strings.length - 1) {
                end = matcher.start();
            }

            List<IASLayer> layers = Arrays.asList(
                    new DeleteLayer(end),
                    new DeleteLayer(0, start)
            );

            if (found) {
                start = matcher.end();
            }

            result[i] = string.derive(strings[i], layers);
        }

        return result;
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

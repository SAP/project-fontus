package de.tubs.cs.ias.asm_test.taintaware.bool;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASMatchResult;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASMatcherable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.regex.Matcher;

@SuppressWarnings("Since15")
public final class IASMatcher implements IASMatcherable {
    private IASString input;
    private IASPattern pattern;
    private final Matcher matcher;

    public IASMatcher(Matcher matcher) {
        // TODO Very hacky way, but original text of a matcher is only accessible though reflection
        this(new IASPattern(matcher.pattern()), ((Function<Matcher, String>) origMatcher -> {
            try {
                Field textField = origMatcher.getClass().getDeclaredField("text");
                textField.setAccessible(true);
                return (String) textField.get(origMatcher);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }).apply(matcher));
    }

    IASMatcher(IASPattern pattern, CharSequence input) {
        this.input = IASString.valueOf(input);
        this.pattern = pattern;
        this.matcher = pattern.getPattern().matcher(input);
    }

    public static IASMatcherable fromMatcher(Matcher param) {
        if (param == null) {
            return null;
        }
        return new IASMatcher(param);
    }

    public IASMatcher appendReplacement(IASStringBuffer sb, IASStringable replacement) {
        matcher.appendReplacement(sb.getStringBuilder(), replacement.getString());
        boolean tainted = sb.isTainted() || replacement.isTainted() || input.isTainted();
        sb.setTaint(tainted);
        return this;
    }

    public IASStringBuffer appendTail(IASStringBuffer sb) {
        matcher.appendTail(sb.getStringBuilder());
        boolean tainted = sb.isTainted() || input.isTainted();
        sb.setTaint(tainted);
        return sb;
    }

    public int end() {
        return this.matcher.end();
    }

    public int end(int group) {
        return this.matcher.end(group);
    }

    public int end(IASStringable name) {
        return this.matcher.end(name.toString());
    }

    public boolean find() {
        return this.matcher.find();
    }

    public boolean find(int start) {
        return this.matcher.find(start);
    }

    public IASString group() {
        return this.input.substring(this.start(), this.end());
    }

    public IASString group(int group) {
        int start = this.start(group);
        int end = this.end(group);
        if (start == -1 || end == -1) {
            return null;
        }
        return this.input.substring(start, end);
    }

    public IASString group(IASStringable name) {
        return this.input.substring(this.start(name), this.end(name));
    }

    public int groupCount() {
        return this.matcher.groupCount();
    }

    public boolean hasAnchoringBounds() {
        return this.matcher.hasAnchoringBounds();
    }

    public boolean hasTransparentBounds() {
        return this.matcher.hasTransparentBounds();
    }

    public boolean hitEnd() {
        return this.matcher.hitEnd();
    }

    public boolean lookingAt() {
        return this.matcher.lookingAt();
    }

    public boolean matches() {
        return this.matcher.matches();
    }

    public IASPattern pattern() {
        return this.pattern;
    }

    public static IASString quoteReplacement(IASStringable s) {
        return new IASString(Matcher.quoteReplacement(s.getString()), s.isTainted());
    }

    public IASMatcher region(int start, int end) {
        this.matcher.region(start, end);
        return this;
    }

    public int regionEnd() {
        return this.matcher.regionEnd();
    }

    public int regionStart() {
        return this.matcher.regionStart();
    }

    public IASString replaceAll(IASStringable replacement) {
        this.reset();
        boolean tainted = this.input.isTainted() || (this.matcher.find() && replacement.isTainted());
        return new IASString(this.matcher.replaceAll(replacement.getString()), tainted);
    }

    public IASString replaceFirst(IASStringable replacement) {
        this.reset();
        boolean tainted = this.input.isTainted() || (this.matcher.find() && replacement.isTainted());
        return new IASString(this.matcher.replaceFirst(replacement.getString()), tainted);
    }

    public boolean requireEnd() {
        return this.matcher.requireEnd();
    }

    public IASMatcher reset() {
        this.matcher.reset();
        return this;
    }

    public IASMatcher reset(CharSequence input) {
        this.matcher.reset(input);
        this.input = IASString.valueOf(input);
        return this;
    }

    public int start() {
        return this.matcher.start();
    }

    public int start(int group) {
        return this.matcher.start(group);
    }

    public int start(IASStringable name) {
        return this.matcher.start(name.toString());
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public IASMatchResult toMatchResult() {
        return new IASMatchResultImpl(this.input, this.matcher.toMatchResult());
    }

    @Override
    public int hashCode() {
        return this.matcher.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.matcher.equals(obj);
    }

    @Override
    public String toString() {
        return this.matcher.toString();
    }

    public IASString toIASString() {
        return IASString.fromString(this.toString());
    }

    public IASMatcher useAnchoringBounds(boolean b) {
        this.matcher.useAnchoringBounds(b);
        return this;
    }

    public IASMatcher usePattern(IASPattern newPattern) {
        this.matcher.usePattern(newPattern.getPattern());
        this.pattern = newPattern;
        return this;
    }

    public IASMatcher useTransparentBounds(boolean b) {
        this.matcher.useTransparentBounds(b);
        return this;
    }
}

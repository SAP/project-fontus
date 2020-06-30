package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASMatchResult;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASMatcherReplacement;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.regex.Matcher;

@SuppressWarnings("unused")
public class IASMatcher {
    private IASString input;
    private IASPattern pattern;
    private final Matcher matcher;
    private int appendPos = 0;

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

    public IASMatcher appendReplacement(IASStringBuffer sb, IASStringable replacement) {
        IASMatcherReplacement replacer = IASMatcherReplacement.createReplacement(replacement, new IASStringBuilder());
        int end = this.start();

        IASString first = this.input.substring(appendPos, end);
        sb.append(first);
        IASString currRepl = (IASString) replacer.doReplacement(this.matcher, this.input, new IASStringBuilder());
        sb.append(currRepl);
        appendPos = this.end();

        return this;
    }

    public IASStringBuffer appendTail(IASStringBuffer sb) {
        if (appendPos < this.input.length()) {
            IASString last = this.input.substring(appendPos);
            sb.append(last);
        }
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
        if(start == -1 || end == -1) { return null; }
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
        // From Apache Harmony
        // first check whether we have smth to quote
        if (s.indexOf('\\') < 0 && s.indexOf('$') < 0)
            return (IASString) s;
        IASStringBuilder res = new IASStringBuilder(s.length() * 2);
        IASString charString;
        int len = s.length();

        for (int i = 0; i < len; i++) {

            switch (s.charAt(i)) {
                case '$':
                    res.append('\\');
                    res.append('$');
                    break;
                case '\\':
                    res.append('\\');
                    res.append('\\');
                    break;
                default:
                    charString = (IASString) s.substring(i, i + 1);
                    res.append(charString);
            }
        }

        return res.toIASString();
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
        return this.input.replaceAll(this.pattern.pattern(), replacement);
    }

    public IASString replaceFirst(IASStringable replacement) {
        this.reset();
        return this.input.replaceFirst(this.pattern.pattern(), replacement);
    }

    public boolean requireEnd() {
        return this.matcher.requireEnd();
    }

    public IASMatcher reset() {
        this.matcher.reset();
        this.appendPos = 0;
        return this;
    }

    public IASMatcher reset(CharSequence input) {
        this.reset();
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

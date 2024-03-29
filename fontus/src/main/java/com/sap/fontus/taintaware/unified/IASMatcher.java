package com.sap.fontus.taintaware.unified;

import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.regex.Matcher;

@SuppressWarnings("unused")
public final class IASMatcher {
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
        this.input = IASString.valueOfInternal(input);
        this.pattern = pattern;
        this.matcher = pattern.getPattern().matcher(input);
    }

    public static IASMatcher fromMatcher(Matcher param) {
        if (param == null) {
            return null;
        }
        return new IASMatcher(param);
    }

    public IASMatcher appendReplacement(IASStringBuffer sb, IASString replacement) {
        IASMatcherReplacement replacer = IASMatcherReplacement.createReplacement(replacement, new IASStringBuilder());
        int end = this.start();
        IASString first = this.input.substring(this.appendPos, end);
        sb.append(first);
        IASString currRepl = replacer.doReplacement(this.matcher, this.input, new IASStringBuilder());
        sb.append(currRepl);
        this.appendPos = this.end();

        return this;
    }

    public IASStringBuffer appendTail(IASStringBuffer sb) {
        if (this.appendPos < this.input.length()) {
            IASString last = this.input.substring(this.appendPos);
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

    public int end(IASString name) {
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

    public IASString group(IASString name) {
        int start = this.start(name);
        int end = this.end(name);
        if(start == -1 || end == -1) {
            return null;
        }
        return this.input.substring(start, end);
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

    public static IASString quoteReplacement(IASString s) {
        // From Apache Harmony
        // first check whether we have smth to quote
        if (s.indexOf('\\') < 0 && s.indexOf('$') < 0) {
            return s;
        }
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
                    charString = s.substring(i, i + 1);
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

    public IASString replaceAll(IASString replacement) {
        IASStringBuffer sb = new IASStringBuffer();
        this.reset();
        while (this.find()) {
            this.appendReplacement(sb, replacement);
        }
        return this.appendTail(sb).toIASString();
    }

    public IASString replaceFirst(IASString replacement) {
        String replacedStr = this.input.getString().replaceFirst(this.pattern.pattern().getString(), replacement.getString());

        // Is one of both Strings tainted? If not, it's irrelevant if one happened for the tainting
        IASTaintInformationable taintInformation = this.input.getTaintInformationInitialized().copy();

        if (this.input.isTainted() || replacement.isTainted()) {
            if (this.find()) {
                final int start = this.start();
                final int end = this.end();

                taintInformation = taintInformation.replaceTaint(start, end, replacement.getTaintInformationInitialized().copy());
            }
        } else {
            taintInformation.resize(replacedStr.length());
        }

        return new IASString(replacedStr, taintInformation);
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
        this.matcher.reset(input);
        this.appendPos = 0;
        this.input = IASString.valueOfInternal(input);
        return this;
    }

    public int start() {
        return this.matcher.start();
    }

    public int start(int group) {
        return this.matcher.start(group);
    }

    public int start(IASString name) {
        return this.matcher.start(name.toString());
    }

    public Matcher getMatcher() {
        return this.matcher;
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
        if (obj instanceof IASMatcher) {
            return this.matcher.equals(((IASMatcher) obj).matcher);
        } else if (obj instanceof Matcher) {
            return this.matcher.equals(obj);
        }
        return false;
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

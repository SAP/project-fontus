package de.tubs.cs.ias.asm_test.taintaware.array;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASMatchResult;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASMatcherable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;

public final class IASMatcher implements IASMatcherable {
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

    public static IASMatcher fromMatcher(Matcher param) {
        if (param == null) {
            return null;
        }
        return new IASMatcher(param);
    }

    public IASMatcher appendReplacement(IASStringBuffer sb, IASStringable replacement) {
        IASMatcher.Replacement replacer = IASMatcher.Replacement.createReplacement(replacement);
        int end = this.start();

        IASString first = this.input.substring(appendPos, end);
        sb.append(first);
        IASString currRepl = replacer.doReplacement(this.matcher, this.input);
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
        if (start == -1 || end == -1) {
            return null;
        }
        return this.input.substring(start, end);
    }

    public IASString group(IASStringable name) {
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
        IASStringBuffer sb = new IASStringBuffer();
        this.reset();
        while (this.find()) {
            this.appendReplacement(sb, replacement);
        }
        return this.appendTail(sb).toIASString();
    }

    public IASString replaceFirst(IASStringable replacement) {
        String replacedStr = this.input.getString().replaceFirst(this.pattern.pattern().getString(), replacement.getString());
        IASTaintInformation ti = new IASTaintInformation(this.input.getTaints());

        // Is one of both Strings tainted? If not, it's irrelevant if one happened for the tainting
        if (this.input.isTainted() || replacement.isTainted()) {
            if (this.find()) {
                final int start = this.start();
                final int end = this.end();

                ti.removeTaintFor(start, end, true);
                ti.insertTaint(start, ((IASString) replacement).getTaints());
            }
        }
        IASString newStr;
        if (ti.isTainted()) {
            newStr = new IASString(replacedStr, ti);
        } else {
            newStr = new IASString(replacedStr);
        }
        return newStr;
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

    private static final class Replacement {
        /**
         * Mapping von group name or group index to index in string
         */
        private final Map<Object, Integer> groups;

        /**
         * Replacement string without the group insertions
         */
        private final IASString clearedReplacementString;

        private Replacement(IASStringable clearedReplacementString, HashMap<Object, Integer> groups) {
            this.clearedReplacementString = (IASString) clearedReplacementString;
            this.groups = groups;
        }

        public IASString doReplacement(Matcher m, IASStringable orig) {
            int lastIndex = -1;
            int shift = 0;
            IASStringBuffer stringBuffer = new IASStringBuffer(this.clearedReplacementString);

            for (Object key : this.groups.keySet()) {
                int start;
                int end;
                if (key instanceof String) {
                    start = m.start((String) key);
                    end = m.end((String) key);
                } else if (key instanceof Integer) {
                    start = m.start((Integer) key);
                    end = m.end((Integer) key);
                } else {
                    throw new IllegalStateException("Group map must not contain something else as strinngs and ints");
                }

                IASString insert = (IASString) orig.substring(start, end);

                int index = groups.get(key);
                if (index < lastIndex) {
                    throw new IllegalStateException("Map not sorted ascending");
                }
                lastIndex = index;

                stringBuffer.insert(index + shift, insert);
                shift += insert.length();
            }
            return stringBuffer.toIASString();
        }

        public static IASMatcher.Replacement createReplacement(IASStringable repl) {
            LinkedHashMap<Object, Integer> groups = new LinkedHashMap<>();

            boolean escaped = false;
            boolean groupParsing = false;
            boolean indexedParsing = false;
            boolean namedParsing = false;

            int groupIndex = -1;
            String groupName = "";

            IASStringBuilder clearedStringBuilder = new IASStringBuilder();

            for (int i = 0; i < repl.length(); i++) {
                char c = repl.charAt(i);

                if (!escaped) {
                    if (groupParsing) {
                        if (indexedParsing) {
                            if (Character.isDigit(c)) {
                                groupIndex = groupIndex * 10 + Character.getNumericValue(c);
                            } else {
                                groupParsing = false;
                                indexedParsing = false;

                                groups.put(groupIndex, clearedStringBuilder.length());

                                // Analyse character again
                                i--;
                                continue;
                            }
                        } else if (namedParsing) {
                            if (isAlphanum(c)) {
                                groupName += c;
                            } else if (c == '}') {
                                if (groupName.isEmpty()) {
                                    throw new IllegalStateException("Groupname cannot be empty!");
                                }
                                groups.put(groupName, clearedStringBuilder.length());

                                groupName = "";
                                namedParsing = false;
                                groupParsing = false;
                            }
                        } else {
                            if (Character.isDigit(c)) {
                                indexedParsing = true;
                                groupIndex = Character.getNumericValue(c);
                            } else if (c == '{') {
                                namedParsing = true;
                            } else {
                                throw new IllegalStateException("After $ there mus be a group index or a named capture group name");
                            }
                        }
                    } else {
                        if (c == '\\') {
                            escaped = true;
                        } else if (c == '$') {
                            groupParsing = true;
                        } else {
                            IASString charStr = (IASString) repl.substring(i, i + 1);
                            clearedStringBuilder.append(charStr);
                        }
                    }
                } else {
                    IASString charStr = (IASString) repl.substring(i, i + 1);
                    clearedStringBuilder.append(charStr);
                    escaped = false;
                }
            }

            return new IASMatcher.Replacement(clearedStringBuilder.toIASString(), groups);
        }

        private static boolean isAlphanum(char c) {
            return Character.isDigit(c) || Character.isLetter(c);
        }
    }
}


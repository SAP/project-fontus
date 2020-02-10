package de.tubs.cs.ias.asm_test.taintaware.range;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;

@SuppressWarnings("unused")
public class IASMatcher {
    private IASString input;
    private IASPattern pattern;
    private Matcher matcher;

    private int appendPos = 0;

    IASMatcher(IASPattern pattern, CharSequence input) {
        this.input = IASString.valueOf(input);
        this.pattern = pattern;
        this.matcher = pattern.getPattern().matcher(input);
    }

    public IASMatcher appendReplacement(IASStringBuffer sb, IASString replacement) {
        Replacement replacer = Replacement.createReplacement(replacement);
        int end = this.start();

        IASString first = this.input.substring(appendPos, end);
        sb.append(first, true);
        IASString currRepl = replacer.doReplacement(this.matcher, this.input);
        sb.append(currRepl, true);
        appendPos = this.end();

        return this;
    }

    public IASStringBuffer appendTail(IASStringBuffer sb) {
        if (appendPos < this.input.length()) {
            IASString last = this.input.substring(appendPos);
            sb.append(last, true);
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
        return this.input.substring(this.start(group), this.end(group));
    }

    public IASString group(IASString name) {
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

    public static IASString quoteReplacement(IASString s) {
        // From Apache Harmony
        // first check whether we have smth to quote
        if (s.indexOf('\\') < 0 && s.indexOf('$') < 0)
            return s;
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
        IASString newStr = new IASString(replacedStr, this.input.getAllRangesAdjusted());

        // Is one of both Strings tainted? If not, it's irrelevant if one happened for the tainting
        if (this.input.isTainted() || replacement.isTainted()) {
            if (this.find()) {
                final int start = this.start();
                final int end = this.end();

                newStr.initialize();
                newStr.getTaintInformation().replaceTaintInformation(start, end, replacement.getAllRangesAdjusted(), replacement.length(), true);
            }
        }
        if (!newStr.isTainted()) {
            newStr = IASString.fromString(newStr.getString());
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

    public int start(IASString name) {
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

    private static final class Replacement {
        /**
         * Mapping von group name or group index to index in string
         */
        private final Map<Object, Integer> groups;

        /**
         * Replacement string without the group insertions
         */
        private final IASString clearedReplacementString;

        private Replacement(IASString clearedReplacementString, HashMap<Object, Integer> groups) {
            this.clearedReplacementString = clearedReplacementString;
            this.groups = groups;
        }

        public IASString doReplacement(Matcher m, IASString orig) {
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

                IASString insert = orig.substring(start, end);

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

        public static Replacement createReplacement(IASString repl) {
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
                            IASString charStr = repl.substring(i, i + 1);
                            clearedStringBuilder.append(charStr);
                        }
                    }
                } else {
                    IASString charStr = repl.substring(i, i + 1);
                    clearedStringBuilder.append(charStr);
                    escaped = false;
                }
            }

            return new Replacement(clearedStringBuilder.toIASString(), groups);
        }

        private static boolean isAlphanum(char c) {
            return Character.isDigit(c) || Character.isLetter(c);
        }
    }
}

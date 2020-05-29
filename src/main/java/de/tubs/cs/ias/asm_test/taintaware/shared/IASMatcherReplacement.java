package de.tubs.cs.ias.asm_test.taintaware.shared;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;

public final class IASMatcherReplacement {
    /**
     * Mapping von group name or group index to index in string
     */
    private final Map<Object, Integer> groups;

    /**
     * Replacement string without the group insertions
     */
    private final IASStringable clearedReplacementString;

    private IASMatcherReplacement(IASStringable clearedReplacementString, HashMap<Object, Integer> groups) {
        this.clearedReplacementString = clearedReplacementString;
        this.groups = groups;
    }

    public IASStringable doReplacement(Matcher m, IASStringable orig, IASStringBuilderable emptyBuilder) {
        int lastIndex = -1;
        int shift = 0;
        emptyBuilder.append(this.clearedReplacementString);

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

            IASStringable insert = orig.substring(start, end);

            int index = groups.get(key);
            if (index < lastIndex) {
                throw new IllegalStateException("Map not sorted ascending");
            }
            lastIndex = index;

            emptyBuilder.insert(index + shift, insert);
            shift += insert.length();
        }
        return emptyBuilder.toIASString();
    }

    public static IASMatcherReplacement createReplacement(IASStringable repl, IASStringBuilderable emptyBuilder) {
        LinkedHashMap<Object, Integer> groups = new LinkedHashMap<>();

        boolean escaped = false;
        boolean groupParsing = false;
        boolean indexedParsing = false;
        boolean namedParsing = false;

        int groupIndex = -1;
        String groupName = "";

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

                            groups.put(groupIndex, emptyBuilder.length());

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
                            groups.put(groupName, emptyBuilder.length());

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
                        IASStringable charStr = repl.substring(i, i + 1);
                        emptyBuilder.append(charStr);
                    }
                }
            } else {
                IASStringable charStr = repl.substring(i, i + 1);
                emptyBuilder.append(charStr);
                escaped = false;
            }
        }

        return new IASMatcherReplacement(emptyBuilder.toIASString(), groups);
    }

    private static boolean isAlphanum(char c) {
        return Character.isDigit(c) || Character.isLetter(c);
    }
}

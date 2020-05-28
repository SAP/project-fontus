package de.tubs.cs.ias.asm_test.taintaware.shared;

import java.util.*;

/**
 * Created by d059349 on 15.07.17.
 */
public class IASTaintSource {
    private final String name;
    private final short id;
    private final IASTaintSourceSeverityLevel level;

    private IASTaintSource(String name, short id, IASTaintSourceSeverityLevel level) {
        this.name = name;
        this.id = id;
        this.level = level;
    }

    private static Map<String, IASTaintSource> map = new HashMap<>();
    private static List<IASTaintSource> arr = new ArrayList<>();
    public static final IASTaintSource TS_STRING_CREATED_FROM_CHAR_ARRAY = getOrCreateInstance("StringCreatedFromCharArray", IASTaintSourceSeverityLevel.POTENTIAL_LAUNDRY);
    public static final IASTaintSource TS_CHAR_UNKNOWN_ORIGIN = getOrCreateInstance("CharUnknownOrigin", IASTaintSourceSeverityLevel.POTENTIAL_LAUNDRY);
    public static final IASTaintSource TS_CS_UNKNOWN_ORIGIN = getOrCreateInstance("CharSequenceUnknownOrigin", IASTaintSourceSeverityLevel.POTENTIAL_LAUNDRY);

    private static IASTaintSource getOrCreateInstance(String name, IASTaintSourceSeverityLevel level) {
        Objects.requireNonNull(name);
        if (name.equals("*")) {
            throw new IllegalArgumentException("\"*\" is not a valid name for a taint-source since this identifier is already reserved as a wildcard for defining forbidden sources in sink checking!");
        } else if (name.startsWith("!")) {
            throw new IllegalArgumentException("The name (called with \"$name\") of a taint-source is not allowed to start with \"!\", as this marks an explicitly allowed source in sink checking!");
        }

        IASTaintSource instance = map.get(name);
        if (instance == null) {
            if (arr.size() + Short.MIN_VALUE > Short.MAX_VALUE) { // MIN_VALUE itself is already negative so substraction would be double negation
                throw new IllegalStateException("It is not possibly to declare any further TaintSources - already ${arr.size} instances created!");
            }

            if (level == null) {
                throw new IllegalArgumentException("For creation of a taint-source a TaintSourceSeverityLevel has to be stated!");
            }

            final IASTaintSource source = new IASTaintSource(name, (short) (arr.size() + Short.MIN_VALUE), level);
            arr.add(source);

            instance = source;
        }


        if (instance.level != level && level != null) {
            throw new IllegalStateException("Given taint-source name is already in use - but with a different severity level!");
        }

        return instance;
    }

    public IASTaintSource getInstanceByName(String name) {
        return map.get(name);
    }

    public static IASTaintSource getInstanceById(short id) {
        if (id < Short.MIN_VALUE || id - Short.MIN_VALUE > arr.size()) {
            throw new IllegalArgumentException("Given TaintSource id ($id) seems to be invalid!");
        }

        return arr.get(id - Short.MIN_VALUE);
    }

    public int size() {
        return arr.size();
    }

    public void reset() {
        map.clear();
        arr.clear();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other.getClass() != this.getClass()) return false;

        IASTaintSource source = (IASTaintSource) other;

        if (!name.equals(source.name)) {
            return false;
        }
        if (id != source.id) {
            return false;
        }
        return level == source.level;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + id;
        result = 31 * result + level.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TaintSource(name='$name', id=$id, level=$level)";
    }

    public int getId() {
        return this.id;
    }
}

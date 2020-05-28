package de.tubs.cs.ias.asm_test.taintaware.shared;

public enum IASTaintSourceSeverityLevel {
    ACTUAL_SOURCE("A source that might be used by an attacker to inject possibly harmful strings into the application"),
    POTENTIAL_LAUNDRY("A source that could possibly be used to launder String, i.e. code we are not able to propagate taint-information along. E.g. serialization, replace(char old, char new) etc."),
    SANITIZATION_FUNCTION("A virtual source that marks a string as sanitized.");
    private final String desc;

    IASTaintSourceSeverityLevel(String desc) {
        this.desc = desc;
    }

    public static boolean isValid(String severtiy) {
        try {
            IASTaintSourceSeverityLevel.valueOf(severtiy);

            return true;
        } catch (Throwable err) {
            return false;
        }
    }
}
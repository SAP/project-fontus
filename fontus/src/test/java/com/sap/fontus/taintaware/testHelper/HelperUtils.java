package com.sap.fontus.taintaware.testHelper;

@SuppressWarnings("ALL")
// David: As I didn't write this Code and don't want to mess with it I suppressed the warnings.
// TODO: Work out whether we can adapt it to the style of the remaining project?
public class HelperUtils {
    public static CharSequence createCharSequence(String retValue) {
        return new CharSequence() {
            @Override
            public int length() {
                return retValue.length();
            }

            @Override
            public char charAt(int index) {
                return retValue.charAt(index);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return retValue.subSequence(start, end);
            }

            @Override
            public String toString() {
                return retValue;
            }
        };
    }
}

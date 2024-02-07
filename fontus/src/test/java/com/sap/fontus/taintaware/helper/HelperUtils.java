package com.sap.fontus.taintaware.helper;

public final class HelperUtils {
    private HelperUtils() {
    }

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

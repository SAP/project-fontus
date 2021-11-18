package com.sap.fontus.sql.tainter;

public class AssignmentValue {

    private Object value;
    private SupportedTypes type;

    public AssignmentValue(char c) {
        value = c;
        type = SupportedTypes.CHAR;
    }

    public AssignmentValue(byte b) {
        value = b;
        type = SupportedTypes.BYTE;
    }

    public AssignmentValue(short s) {
        value = s;
        type = SupportedTypes.SHORT;
    }

    public AssignmentValue(int i) {
        value = i;
        type = SupportedTypes.INT;
    }

    public AssignmentValue(long l) {
        value = l;
        type = SupportedTypes.LONG;
    }

    public AssignmentValue(float f) {
        value = f;
        type = SupportedTypes.FLOAT;
    }

    public AssignmentValue(double d) {
        value = d;
        type = SupportedTypes.DOUBLE;
    }

    public AssignmentValue(boolean b) {
        value = b;
        type = SupportedTypes.BOOLEAN;
    }

    public AssignmentValue(String s) {
        value = s;
        type = SupportedTypes.STRING;
    }

    public Object getValue() {
        return value;
    }

    public String getValueAsString() {
        return String.valueOf(value);
    }
}

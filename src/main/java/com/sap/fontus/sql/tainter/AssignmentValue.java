package com.sap.fontus.sql.tainter;

public class AssignmentValue {

    private final Object value;
    private final SupportedTypes type;

    public AssignmentValue(char c) {
        this.value = c;
        this.type = SupportedTypes.CHAR;
    }

    public AssignmentValue(byte b) {
        this.value = b;
        this.type = SupportedTypes.BYTE;
    }

    public AssignmentValue(short s) {
        this.value = s;
        this.type = SupportedTypes.SHORT;
    }

    public AssignmentValue(int i) {
        this.value = i;
        this.type = SupportedTypes.INT;
    }

    public AssignmentValue(long l) {
        this.value = l;
        this.type = SupportedTypes.LONG;
    }

    public AssignmentValue(float f) {
        this.value = f;
        this.type = SupportedTypes.FLOAT;
    }

    public AssignmentValue(double d) {
        this.value = d;
        this.type = SupportedTypes.DOUBLE;
    }

    public AssignmentValue(boolean b) {
        this.value = b;
        this.type = SupportedTypes.BOOLEAN;
    }

    public AssignmentValue(String s) {
        this.value = s;
        this.type = SupportedTypes.STRING;
    }

    public Object getValue() {
        return this.value;
    }

    public String getValueAsString() {
        return String.valueOf(this.value);
    }
}

package com.sap.fontus.sql.tainter;

import java.util.Objects;

public class TaintAssignment {

    private final int oldIndex;
    private final int newIndex;
    private final int taintIndex;
    private final boolean hasTaint;
    private final ParameterType parameterType;

    public TaintAssignment(int oldIndex, int newIndex, ParameterType parameterType) {
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
        this.taintIndex = -1;
        this.parameterType = parameterType;
        this.hasTaint = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        TaintAssignment that = (TaintAssignment) o;
        return this.oldIndex == that.oldIndex && this.newIndex == that.newIndex && this.taintIndex == that.taintIndex && this.hasTaint == that.hasTaint && this.parameterType == that.parameterType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.oldIndex, this.newIndex, this.taintIndex, this.hasTaint, this.parameterType);
    }

    public TaintAssignment(int oldIndex, int newIndex, int taintIndex, ParameterType parameterType) {
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
        this.taintIndex = taintIndex;
        this.parameterType = parameterType;
        this.hasTaint = taintIndex != -1;
    }

    public int getOldIndex() {
        return this.oldIndex;
    }

    public int getNewIndex() {
        return this.newIndex;
    }

    public int getTaintIndex() {
        return this.taintIndex;
    }

    public boolean isHasTaint() {
        return this.hasTaint;
    }

    public ParameterType getParameterType() {
        return this.parameterType;
    }
}

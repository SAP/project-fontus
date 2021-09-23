package com.sap.fontus.taintaware.unified.reflect;

import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.utils.ConversionUtils;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

public class IASField extends IASAccessibleObject<Field> implements IASMember {
    private static final CombinedExcludedLookup lookup = new CombinedExcludedLookup();

    public IASField(Field field) {
        super(field);
    }

    @Override
    public void setAccessible(boolean flag) {
        super.setAccessible(flag);
    }

    public Class<?> getDeclaringClass() {
        return this.original.getDeclaringClass();
    }

    public IASString getName() {
        return IASString.valueOf(this.original.getName());
    }

    public int getModifiers() {
        return this.original.getModifiers();
    }

    public boolean isEnumConstant() {
        return this.original.isEnumConstant();
    }

    public boolean isSynthetic() {
        return this.original.isSynthetic();
    }

    public Class<?> getType() {
        return this.original.getType();
    }

    public Type getGenericType() {
        return ConversionUtils.convertTypeToInstrumented(this.original.getGenericType());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IASField) {
            return this.original.equals(((IASField) obj).original);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.original.hashCode();
    }

    @Override
    public String toString() {
        return this.original.toString();
    }

    public String toGenericString() {
        return this.original.toGenericString();
    }

    public Object get(Object obj) throws IllegalArgumentException, IllegalAccessException {
        return ConversionUtils.convertToConcrete(this.original.get(obj));
    }

    public boolean getBoolean(Object obj) throws IllegalArgumentException, IllegalAccessException {
        return this.original.getBoolean(obj);
    }

    public byte getByte(Object obj) throws IllegalArgumentException, IllegalAccessException {
        return this.original.getByte(obj);
    }

    public char getChar(Object obj) throws IllegalArgumentException, IllegalAccessException {
        return this.original.getChar(obj);
    }

    public short getShort(Object obj) throws IllegalArgumentException, IllegalAccessException {
        return this.original.getShort(obj);
    }

    public int getInt(Object obj) throws IllegalArgumentException, IllegalAccessException {
        return this.original.getInt(obj);
    }

    public long getLong(Object obj) throws IllegalArgumentException, IllegalAccessException {
        return this.original.getLong(obj);
    }

    public float getFloat(Object obj) throws IllegalArgumentException, IllegalAccessException {
        return this.original.getFloat(obj);
    }

    public double getDouble(Object obj) throws IllegalArgumentException, IllegalAccessException {
        return this.original.getDouble(obj);
    }

    public void set(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException {
        if (lookup.isPackageExcludedOrJdk(this.getDeclaringClass())) {
            this.original.set(obj, ConversionUtils.convertToOrig(value));
        } else {
            this.original.set(obj, value);
        }
    }

    public void setBoolean(Object obj, boolean z) throws IllegalArgumentException, IllegalAccessException {
        this.original.set(obj, z);
    }

    public void setByte(Object obj, byte b) throws IllegalArgumentException, IllegalAccessException {
        this.original.setByte(obj, b);
    }

    public void setChar(Object obj, char c) throws IllegalArgumentException, IllegalAccessException {
        this.original.setChar(obj, c);
    }

    public void setShort(Object obj, short s) throws IllegalArgumentException, IllegalAccessException {
        this.original.setShort(obj, s);
    }

    public void setInt(Object obj, int i) throws IllegalArgumentException, IllegalAccessException {
        this.original.setInt(obj, i);
    }

    public void setLong(Object obj, long l) throws IllegalArgumentException, IllegalAccessException {
        this.original.setLong(obj, l);
    }

    public void setFloat(Object obj, float f) throws IllegalArgumentException, IllegalAccessException {
        this.original.setFloat(obj, f);
    }

    public void setDouble(Object obj, double d) throws IllegalArgumentException, IllegalAccessException {
        this.original.setDouble(obj, d);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return super.getAnnotation(annotationClass);
    }

    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return super.getAnnotationsByType(annotationClass);
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return super.getDeclaredAnnotations();
    }

    public AnnotatedType getAnnotatedType() {
        return this.original.getAnnotatedType();
    }

    public Field getField() {
        return this.original;
    }

    public Member getMember() {
        return this.original;
    }
}

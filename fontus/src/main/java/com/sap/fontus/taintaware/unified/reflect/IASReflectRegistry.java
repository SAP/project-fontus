package com.sap.fontus.taintaware.unified.reflect;

import java.lang.reflect.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IASReflectRegistry {
    private static final IASReflectRegistry INSTANCE = new IASReflectRegistry();
    // TODO: Replace with ConcurrentHashMap and get rid of synchronized
    private final Map<Field, IASField> fields = new ConcurrentHashMap<>();
    private final Map<Method, IASMethod> methods = new ConcurrentHashMap<>();
    private final Map<Constructor<?>, IASConstructor<?>> constructors = new ConcurrentHashMap<>();

    public static IASReflectRegistry getInstance() {
        return INSTANCE;
    }

    public IASField map(Field field) {
        return this.fields.computeIfAbsent(field, IASField::new);
    }

    public IASMethod map(Method method) {
        return this.methods.computeIfAbsent(method, IASMethod::new);
    }

    public <T> IASConstructor<T> map(Constructor<T> constructor) {
        return new IASConstructor<>(constructor);
        /* TODO: Figure out whether this was "just" an optimization or is required for something
        this.constructors.computeIfAbsent(constructor, IASConstructor<T>::new);
        return this.constructors.get(constructor);
        */
    }

    public IASExecutable<?> mapExecutable(Executable executable) {
        if (executable instanceof Method m) {
            return this.map(m);
        } else if (executable instanceof Constructor<?> c) {
            return this.map(c);
        }
        throw new IllegalArgumentException("Executable is not a method or constructor");
    }

    public IASAccessibleObject<?> mapAccessibleObject(AccessibleObject accessibleObject) {
        if (accessibleObject instanceof Method m) {
            return this.map(m);
        } else if (accessibleObject instanceof Constructor<?> c) {
            return this.map(c);
        } else if (accessibleObject instanceof Field f) {
            return this.map(f);
        }
        throw new IllegalArgumentException("Executable is not a method or constructor");
    }

    public IASMember mapMember(Member m) {
        return (IASMember) this.mapAccessibleObject((AccessibleObject) m);
    }
}

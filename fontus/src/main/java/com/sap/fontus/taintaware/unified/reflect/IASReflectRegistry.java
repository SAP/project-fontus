package com.sap.fontus.taintaware.unified.reflect;

import java.lang.reflect.*;
import java.util.HashMap;
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
        if (executable instanceof Method) {
            return this.map((Method) executable);
        } else if (executable instanceof Constructor) {
            return this.map((Constructor<?>) executable);
        }
        throw new IllegalArgumentException("Executable is not a method or constructor");
    }

    public IASAccessibleObject<?> mapAccessibleObject(AccessibleObject accessibleObject) {
        if (accessibleObject instanceof Method) {
            return this.map((Method) accessibleObject);
        } else if (accessibleObject instanceof Constructor) {
            return this.map((Constructor<?>) accessibleObject);
        } else if (accessibleObject instanceof Field) {
            return this.map((Field) accessibleObject);
        }
        throw new IllegalArgumentException("Executable is not a method or constructor");
    }

    public IASMember mapMember(Member m) {
        return (IASMember) this.mapAccessibleObject((AccessibleObject) m);
    }
}

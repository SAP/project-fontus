package com.sap.fontus.taintaware.unified.reflect;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

public class IASReflectRegistry {
    private static final IASReflectRegistry INSTANCE = new IASReflectRegistry();
    // TODO: Replace with ConcurrentHashMap and get rid of synchronized
    private final Map<Field, IASField> fields = new HashMap<>();
    private final Map<Method, IASMethod> methods = new HashMap<>();
    private final Map<Constructor<?>, IASConstructor<?>> constructors = new HashMap<>();

    public static IASReflectRegistry getInstance() {
        return INSTANCE;
    }

    public synchronized IASField map(Field field) {
        this.fields.computeIfAbsent(field, IASField::new);
        return this.fields.get(field);
    }

    public synchronized IASMethod map(Method method) {
        this.methods.computeIfAbsent(method, IASMethod::new);
        return this.methods.get(method);
    }

    public synchronized <T> IASConstructor<T> map(Constructor<T> constructor) {
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

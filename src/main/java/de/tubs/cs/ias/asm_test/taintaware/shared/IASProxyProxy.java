package de.tubs.cs.ias.asm_test.taintaware.shared;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class IASProxyProxy {
    protected final InvocationHandler h;
    private static final List<Class<?>> proxyCache = new ArrayList<>();

    protected IASProxyProxy(InvocationHandler h) {
        this.h = h;
    }

    public static boolean isProxyClass(Class<?> cls) {
        return proxyCache.contains(cls);
    }

    public static InvocationHandler getInvocationHandler(Object proxy) {
        if (!isProxyClass(proxy.getClass())) {
            throw new IllegalArgumentException("not a proxy instance");
        } else {
//            InvocationHandler invocationHandler = ((IASProxyProxy) proxy).h;
//            Class<?> caller = ReflectionUtils.getCallerClass();
//            if (needsPackageAccessCheck(caller.getClassLoader(), invocationHandler.getClass().getClassLoader())) {
//                ReflectUtil.checkPackageAccess(invocationHandler.getClass());
//            }
//            return invocationHandler;

            try {
                Field f = proxy.getClass().getDeclaredField("h");
                f.setAccessible(true);
                return (InvocationHandler) f.get(proxy);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static boolean needsPackageAccessCheck(ClassLoader from, ClassLoader to) {
        if (from != null && from != to) {
            if (to == null) {
                return true;
            } else {
                do {
                    to = to.getParent();
                    if (from == to) {
                        return true;
                    }
                } while (to != null);

                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean isAncestor(ClassLoader p, ClassLoader cl) {
        ClassLoader acl = cl;

        do {
            acl = acl.getParent();
            if (p == acl) {
                return true;
            }
        } while (acl != null);

        return false;
    }

    @SuppressWarnings("unchecked")
    public static Object newProxyInstance(ClassLoader classLoader, Class<?>[] interfaces, InvocationHandler h) throws NoSuchMethodException {
        try {
            IASProxyProxyBuilder builder = IASProxyProxyBuilder.newBuilder(interfaces, classLoader);
            Class<?> cls = builder.build();

            Constructor<?> constructor = cls.getConstructor(InvocationHandler.class);

            proxyCache.add(cls);

            try {
                return constructor.newInstance(h);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new InternalError(e.toString(), e);
            } catch (InvocationTargetException e) {
                Throwable t = e.getCause();
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else {
                    throw new InternalError(t.toString(), t);
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }
}

package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.utils.Utils;
import de.tubs.cs.ias.asm_test.utils.VerboseLogger;
import jdk.internal.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class IASProxyProxy {
    private static final Unsafe UNSAFE;
    protected final InvocationHandler h;
    private static final Map<byte[], Class<?>> proxyCache = new HashMap<>();

    static {
        try {
            UNSAFE = Unsafe.getUnsafe();
        } catch (Throwable ex) {
            System.err.println("Couldn't load unsafe!");
            throw ex;
        }
    }

    protected IASProxyProxy(InvocationHandler h) {
        this.h = h;
    }

    public static boolean isProxyClass(Class<?> cls) {
        return proxyCache.containsValue(cls);
    }

    public static boolean isProxyClass(String name, byte[] bytes) {
        for (Map.Entry<byte[], Class<?>> entry : proxyCache.entrySet()) {
            Class<?> cls = entry.getValue();
            byte[] cachedBytes = entry.getKey();
            if (cls == null || cls.getName().equals(Utils.slashToDot(name))) {
                if (Arrays.equals(cachedBytes, bytes)) {
                    return true;
                }
            }
        }
        return false;
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


    private static Class<?> loadClass(String name, byte[] bytes, ClassLoader classLoader) {
        return UNSAFE.defineClass(Utils.slashToDot(name), bytes, 0, bytes.length, classLoader, null);
    }

    public static Object newProxyInstance(ClassLoader classLoader, Class<?>[] interfaces, InvocationHandler h) throws NoSuchMethodException {
        try {
            IASProxyProxyBuilder builder = IASProxyProxyBuilder.newBuilder(interfaces, classLoader);

            byte[] bytes = builder.build();
            proxyCache.put(bytes, null);

            VerboseLogger.saveIfVerbose(builder.getName(), bytes);

            Class<?> cls = loadClass(builder.getName(), bytes, classLoader);


            Constructor<?> constructor = cls.getConstructor(InvocationHandler.class);

            proxyCache.put(bytes, cls);

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
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}

package com.sap.fontus.taintaware.unified;

import com.sap.fontus.utils.UnsafeUtils;
import com.sap.fontus.utils.Utils;
import com.sap.fontus.utils.VerboseLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IASProxyProxy {
    protected final InvocationHandler h;
    private static final Map<byte[], Class<?>> proxyCache = new HashMap<>();


    protected IASProxyProxy(InvocationHandler h) {
        Objects.requireNonNull(h);
        this.h = h;
    }

    // Prevent instantiation
    private IASProxyProxy() { h = null; }

    public static boolean isProxyClass(Class<?> cls) {
        return proxyCache.containsValue(cls);
    }

    public static boolean isProxyClass(String internalName, byte[] bytes) {
        for (Map.Entry<byte[], Class<?>> entry : proxyCache.entrySet()) {
            Class<?> cls = entry.getValue();
            byte[] cachedBytes = entry.getKey();
            if (cls == null || cls.getName().equals(Utils.slashToDot(internalName))) {
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
                Field f = IASProxyProxy.class.getDeclaredField("h");
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
        ClassLoader loader = classLoader;
        if ((loader == null) || (loader == ClassLoader.getPlatformClassLoader())) {
            loader = IASProxyProxy.class.getClassLoader();
        }
        return UnsafeUtils.defineClass(Utils.slashToDot(name), bytes, loader);
    }

    private static Class<?> findProxyClassInternal(Class<?>[] interfaces) {
        Class<?>[] paramInterfaces = interfaces.clone();
        Arrays.sort(paramInterfaces, (o1, o2) -> o2.hashCode() - o1.hashCode());
        for (Class<?> proxy : proxyCache.values()) {
            if (proxy != null) {
                Class<?>[] proxyInterfaces = proxy.getInterfaces();
                if (proxyInterfaces.length == paramInterfaces.length) {
                    Arrays.sort(proxyInterfaces, (o1, o2) -> o2.hashCode() - o1.hashCode());
                    if (Arrays.equals(proxyInterfaces, paramInterfaces)) {
                        return proxy;
                    }
                }
            }
        }
        return null;
    }

    private static Class<?> createProxyClassInternal(ClassLoader classLoader, Class<?>[] interfaces) {
        IASProxyProxyBuilder builder = IASProxyProxyBuilder.newBuilder(interfaces, classLoader);

        byte[] bytes = builder.build();
        proxyCache.put(bytes, null);

        VerboseLogger.saveIfVerbose(builder.getName(), bytes);

        Class<?> cls = loadClass(builder.getName(), bytes, classLoader);

        proxyCache.put(bytes, cls);

        return cls;
    }

    public static synchronized Class<?> getProxyClass(ClassLoader classLoader, Class<?>[] interfaces) {
        Class<?> cached = findProxyClassInternal(interfaces);

        if (cached == null) {
            return createProxyClassInternal(classLoader, interfaces);
        } else {
            return cached;
        }
    }

    public static Object newProxyInstance(ClassLoader classLoader, Class<?>[] interfaces, InvocationHandler h) throws NoSuchMethodException {
        try {
            Class<?> proxy = getProxyClass(classLoader, interfaces);
            Constructor<?> constructor = proxy.getConstructor(InvocationHandler.class);

            try {
                return constructor.newInstance(new Object[]{h});
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

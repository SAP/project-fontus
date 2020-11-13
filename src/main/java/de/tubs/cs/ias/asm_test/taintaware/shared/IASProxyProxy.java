package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.utils.ReflectionUtils;
import de.tubs.cs.ias.asm_test.utils.VerboseLogger;
import jdk.internal.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class IASProxyProxy {
    private static final Unsafe UNSAFE = Unsafe.getUnsafe();
    private static volatile int counter = 0;
    private static final String PROXY_CLASS_BASE_NAME = Constants.PACKAGE + ".IASProxyImpl";
    protected final InvocationHandler h;
    private static final Map<Class<? extends IASProxyProxy>, byte[]> proxyCache = new HashMap<>();

    protected IASProxyProxy(InvocationHandler h) {
        this.h = h;
    }

    public static boolean isProxyClass(Class<?> cls) {
        if (!cls.isAssignableFrom(IASProxyProxy.class)) {
            return false;
        }

        return proxyCache.containsKey(cls);
    }

    public static InvocationHandler getInvocationHandler(Object proxy) {
        if (!isProxyClass(proxy.getClass())) {
            throw new IllegalArgumentException("not a proxy instance");
        } else {
            InvocationHandler invocationHandler = ((IASProxyProxy) proxy).h;
            Class<?> caller = ReflectionUtils.getCallerClass();
            if (needsPackageAccessCheck(caller.getClassLoader(), invocationHandler.getClass().getClassLoader())) {
//                ReflectUtil.checkPackageAccess(invocationHandler.getClass());
            }

            return invocationHandler;
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
        String name = newProxyName();

        IASProxyProxyBuilder builder = IASProxyProxyBuilder.newBuilder(name, interfaces);
        byte[] bytes = builder.build();

        VerboseLogger.saveIfVerbose(name, bytes);

        Class<? extends IASProxyProxy> cls = (Class<? extends IASProxyProxy>) UNSAFE.defineClass(name, bytes, 0, bytes.length, classLoader, null);
        Constructor<?> constructor = cls.getConstructor(InvocationHandler.class);

        proxyCache.put(cls, bytes);

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
    }

    private static synchronized String newProxyName() {
        String name = PROXY_CLASS_BASE_NAME + "$" + counter;
        counter++;
        return name;
    }
}

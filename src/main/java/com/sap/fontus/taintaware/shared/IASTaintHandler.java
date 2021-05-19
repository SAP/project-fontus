package com.sap.fontus.taintaware.shared;

import com.sap.fontus.config.abort.Abort;
import com.sap.fontus.utils.stats.Statistics;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

import static com.sap.fontus.utils.ClassTraverser.getAllFields;

public class IASTaintHandler {
    public static CombinedExcludedLookup combinedExcludedLookup = new CombinedExcludedLookup(ClassLoader.getSystemClassLoader());

    public static Void handleTaint(IASTaintAware taintAware, String sinkFunction, String sinkName) {
        boolean isTainted = taintAware.isTainted();

        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.recordTaintCheck(isTainted);
        }

        if (isTainted) {
            Abort abort = Configuration.getConfiguration().getAbort();

            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            List<StackTraceElement> cleanedStackTrace = new ArrayList<>();
            for (int i = 1; i < stackTrace.length; i++) {
                StackTraceElement ste = stackTrace[i];
                if (!ste.getClassName().startsWith(IASTaintHandler.class.getName())) {
                    cleanedStackTrace.add(ste);
                }
            }

            abort.abort(taintAware, sinkFunction, sinkName, cleanedStackTrace);
        }
        return null;
    }

    private static Void setTaint(Object taintAware, int sourceId) {
        IASTaintSource source = IASTaintSourceRegistry.getInstance().get(sourceId);
        ((IASTaintAware) taintAware).setTaint(source);
        return null;
    }

    public static Object traverseObject(Object object, Function<IASTaintAware, Void> atomicHandler) {
        List<Object> visited = new ArrayList<>();
        return traverseObject(object, new Function<Object, Void>() {
            @Override
            public Void apply(Object o) {
                traverseObject(o, this, visited, atomicHandler);
                return null;
            }
        }, visited, atomicHandler);
    }

    public static Object traverseObject(Object object, Function<Object, Void> traverser, List<Object> visited, Function<IASTaintAware, Void> atomicHandler) {
        if (object == null) {
            return null;
        } else if (visited.contains(object)) {
            return object;
        } else if (object.getClass().isEnum()) {
            return object;
        }
        visited.add(object);

        boolean isArray = object.getClass().isArray();
        boolean isPrimitive = isArray ? object.getClass().getComponentType().isPrimitive() : object.getClass().isPrimitive();
        boolean isIterable = Iterable.class.isAssignableFrom(object.getClass());
        boolean isEnumerate = Enumeration.class.isAssignableFrom(object.getClass());
        boolean isMap = Map.class.isAssignableFrom(object.getClass());
        Class<?> cls = object.getClass();

        if (isArray && isPrimitive) {
            return object;
        }

        if (IASTaintAware.class.isAssignableFrom(cls)) {
            atomicHandler.apply((IASTaintAware) object);
        } else if (isArray) {
            Object[] array = (Object[]) object;
            for (Object o : array) {
                traverser.apply(o);
            }
        } else if (isIterable) {
            Iterable<Object> iterable = (Iterable<Object>) object;
            for (Object o : iterable) {
                traverser.apply(o);
            }
        } else if (isMap) {
            Map<Object, Object> map = (Map) object;
            map.forEach((o, o2) -> {
                traverser.apply(o);
                traverser.apply(o2);
            });
        } else if (isEnumerate) {
            Enumeration<Object> enumeration = (Enumeration<Object>) object;
            List<Object> list = Collections.list(enumeration);
            for (Object o : list) {
                traverser.apply(o);
            }
            object = Collections.enumeration(list);
        } else if (combinedExcludedLookup.isJdkClass(cls) || combinedExcludedLookup.isAnnotation(cls) || combinedExcludedLookup.isPackageExcluded(cls)) {
            return object;
        } else if (Configuration.getConfiguration().isRecursiveTainting()) {
            List<Field> fields = getAllFields(cls);
            for (Field f : fields) {
                f.setAccessible(true);
                Object attr;
                try {
                    attr = f.get(object);
                    traverseObject(attr, traverser, visited, atomicHandler);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return object;
    }

    public static Object checkTaint(Object object, String sinkFunction, String sinkName) {
        if (object instanceof IASTaintAware) {
            handleTaint((IASTaintAware) object, sinkFunction, sinkName);
            return object;
        }

        return traverseObject(object, taintAware -> handleTaint(taintAware, sinkFunction, sinkName));
    }

    public static Object taint(Object object, int sourceId) {
        if (object instanceof IASTaintAware) {
            IASTaintSource source = IASTaintSourceRegistry.getInstance().get(sourceId);
            ((IASTaintAware) object).setTaint(source);
            return object;
        }
        return traverseObject(object, taintAware -> setTaint(taintAware, sourceId));
    }
}

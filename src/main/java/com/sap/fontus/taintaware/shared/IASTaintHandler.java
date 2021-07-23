package com.sap.fontus.taintaware.shared;

import com.sap.fontus.config.abort.Abort;
import com.sap.fontus.utils.stats.Statistics;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sap.fontus.utils.ClassTraverser.getAllFields;

public class IASTaintHandler {
    public static CombinedExcludedLookup combinedExcludedLookup = new CombinedExcludedLookup(ClassLoader.getSystemClassLoader());

    public static IASTaintAware handleTaint(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName) {
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

            abort.abort(taintAware, instance, sinkFunction, sinkName, cleanedStackTrace);
        }
        return null;
    }

    private static IASTaintAware setTaint(IASTaintAware taintAware, int sourceId) {
        IASTaintSource source = IASTaintSourceRegistry.getInstance().get(sourceId);
        taintAware.setTaint(source);
        return taintAware;
    }

    public static Object traverseObject(Object object, Function<IASTaintAware, IASTaintAware> atomicHandler) {
        List<Object> visited = new ArrayList<>();
        return traverseObject(object, new Function<Object, Object>() {
            @Override
            public Object apply(Object o) {
                traverseObject(o, this, visited, atomicHandler);
                return null;
            }
        }, visited, atomicHandler);
    }

    public static Object traverseObject(Object object, Function<Object, Object> traverser, List<Object> visited, Function<IASTaintAware, IASTaintAware> atomicHandler) {
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
//        boolean isIterable = Iterable.class.isAssignableFrom(object.getClass());
//        boolean isEnumerate = Enumeration.class.isAssignableFrom(object.getClass());
        boolean isList = List.class.isAssignableFrom(object.getClass());
        boolean isMap = Map.class.isAssignableFrom(object.getClass());
        Class<?> cls = object.getClass();

        if (isArray && isPrimitive) {
            return object;
        }

        if (IASTaintAware.class.isAssignableFrom(cls)) {
            atomicHandler.apply((IASTaintAware) object);
        } else if (isArray) {
            Object[] array = (Object[]) object;
            for (int i = 0; i < array.length; i++) {
                array[i] = traverser.apply(array[i]);
            }
        } else if (isList) {
            List list = (List) object;
            for (int i = 0; i < list.size(); i++) {
                list.set(i, traverser.apply(list.get(i)));
            }
        }
//        else if (isIterable) {
//            Iterable<Object> iterable = (Iterable<Object>) object;
//            for (Object o : iterable) {
//                traverser.apply(o);
//            }
//        }
        else if (isMap) {
            Map<Object, Object> map = (Map) object;
            object = map.entrySet().stream().collect(Collectors.toMap(e -> traverser.apply(e.getKey()), e -> traverser.apply(e.getValue())));
        }
//        else if (isEnumerate) {
//            Enumeration<Object> enumeration = (Enumeration<Object>) object;
//            List<Object> list = Collections.list(enumeration);
//            for (Object o : list) {
//                traverser.apply(o);
//            }
//            object = Collections.enumeration(list);
//        }
        else if (combinedExcludedLookup.isJdkClass(cls) || combinedExcludedLookup.isAnnotation(cls) || combinedExcludedLookup.isPackageExcluded(cls)) {
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

    public static Object checkTaint(Object object, Object instance, String sinkFunction, String sinkName) {
        if (object instanceof IASTaintAware) {
            return handleTaint((IASTaintAware) object, instance, sinkFunction, sinkName);
        }

        return traverseObject(object, taintAware -> handleTaint(taintAware, instance, sinkFunction, sinkName));
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

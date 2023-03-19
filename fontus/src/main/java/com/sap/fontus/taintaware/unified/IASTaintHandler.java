package com.sap.fontus.taintaware.unified;

import com.sap.fontus.Constants;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.abort.Abort;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import com.sap.fontus.utils.stats.Statistics;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

import static com.sap.fontus.utils.ClassTraverser.getAllFields;

/**
 * This class provides the interface between the instrumented bytecode and taint setters / getters
 */
public class IASTaintHandler {
    public static final CombinedExcludedLookup combinedExcludedLookup = new CombinedExcludedLookup(ClassLoader.getSystemClassLoader());

    protected static void printObjectInfo(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId) {

        IASTaintSource source = IASTaintSourceRegistry.getInstance().get(sourceId);

        System.out.println("FONTUS: Source: " + source);
        System.out.println("        taintAware: " + taintAware);
        System.out.println("        Caller Type:" + parent);
        System.out.println("        Input Parameters: " + Arrays.toString(parameters));

        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                System.out.println("                  " + i + ": " + parameters[i].toString());
            }
        }

    }

    public static List<StackTraceElement> getCleanedStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        List<StackTraceElement> cleanedStackTrace = new ArrayList<>();
        for (int i = 1; i < stackTrace.length; i++) {
            StackTraceElement ste = stackTrace[i];
            if (!ste.getClassName().startsWith(IASTaintHandler.class.getName())) {
                cleanedStackTrace.add(ste);
            }
        }
        return cleanedStackTrace;
    }

    /**
     * Hook function called before a sink function is called
     *
     * @param taintAware   The taint aware object (normally a string)
     * @param instance     The specific instance of the object on which the method is called
     * @param sinkFunction The name of the function
     * @param sinkName     The name of the sink
     * @return
     */
    public static IASTaintAware handleTaint(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName) {
        boolean isTainted = taintAware.isTainted();
//        System.out.println("isTainted : " + isTainted);
//        System.out.println("taintaware : " + taintAware);
//        System.out.println("sink : " + sinkFunction);
//        System.out.println("stackTrace : " + java.util.Arrays.toString(Thread.currentThread().getStackTrace()));

        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.recordTaintCheck(isTainted);
        }

        if (isTainted) {
            Abort abort = Configuration.getConfiguration().getAbort();
            taintAware = abort.abort(taintAware, instance, sinkFunction, sinkName, getCleanedStackTrace());
        }
        return taintAware;
    }

    private static IASTaintAware setTaint(IASTaintAware taintAware, int sourceId) {
        IASTaintSource source = IASTaintSourceRegistry.getInstance().get(sourceId);
        taintAware.setTaint(new IASBasicMetadata(source));
        return taintAware;
    }

    protected static Object traverseObject(Object object, Function<IASTaintAware, IASTaintAware> atomicHandler) {
        List<Object> visited = new ArrayList<>();
        return traverseObject(object, new Function<Object, Object>() {
            @Override
            public Object apply(Object o) {
                return traverseObject(o, this, visited, atomicHandler);
            }
        }, visited, atomicHandler);
    }

    protected static Object traverseObject(Object object, Function<Object, Object> traverser, List<Object> visited, Function<IASTaintAware, IASTaintAware> atomicHandler) {
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
            List<Object> list = (List<Object>) object;

            boolean isUnmodifiable = cls.getName().startsWith("java.util.Collections$Unmodifiable") || cls.getName().startsWith("java.util.Collections$Singleton");

            for (int i = 0; i < list.size(); i++) {
                Object traversed = traverser.apply(list.get(i));
                if (!isUnmodifiable) {
                    list.set(i, traversed);
                }
            }
        } else if (isIterable) {
            Iterable<Object> iterable = (Iterable<Object>) object;
            for (Object o : iterable) {
                traverser.apply(o);
            }
        } else if (isMap) {
            Map<Object, Object> map = (Map<Object, Object>) object;
            if (!map.isEmpty()) {
                Map<Object, Object> newMap = new HashMap<>(map.size());
                for(Map.Entry<Object, Object> e : map.entrySet()) {
                    Object key = traverser.apply(e.getKey());
                    Object value = traverser.apply(e.getValue());
                    newMap.put(key, value);
                }
                object = newMap; // map.entrySet().stream().collect(Collectors.toMap(e -> traverser.apply(e.getKey()), e -> traverser.apply(e.getValue())));
            }
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

    public static Object checkTaint(Object object, Object instance, String sinkFunction, String sinkName) {
        if (object instanceof IASTaintAware) {
            return handleTaint((IASTaintAware) object, instance, sinkFunction, sinkName);
        } else if (instance instanceof IASTaintAware) {
            // Things like String.toCharArray() can be handled here
            return handleTaint((IASTaintAware) instance, object, sinkFunction, sinkName);
        }

        return traverseObject(object, taintAware -> handleTaint(taintAware, instance, sinkFunction, sinkName));
    }

    /**
     * Hook function called at all taint sources added to bytecode
     *
     * @param object   The object to be tainted (can be a string, or something which needs traversing, like a list)
     * @param sourceId The source as an integer
     * @return A tainted version of the input object
     */
    public static Object taint(Object object, Object parentObject, Object[] parameters, int sourceId) {
        if (object instanceof IASTaintAware) {
            setTaint((IASTaintAware) object, sourceId);
            return object;
        }
        return traverseObject(object, taintAware -> setTaint(taintAware, sourceId));
    }

    public static boolean isValidTaintHandler(FunctionCall function) {
        // Check at least the descriptor is right
        return (function.getDescriptor().equals(Constants.TaintHandlerTaintDesc));
    }

    public static boolean isValidTaintChecker(FunctionCall function) {
        // Check at least the descriptor is right
        return (function.getDescriptor().equals(Constants.TaintHandlerCheckTaintDesc));
    }
}

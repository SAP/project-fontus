package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.config.abort.Abort;
import de.tubs.cs.ias.asm_test.utils.stats.Statistics;

import java.util.*;
import java.util.function.Function;

public class IASTaintHandler {
    public static Void handleTaint(IASTaintAware taintAware, String sink, String category) {
        boolean isTainted = taintAware.isTainted();

        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.recordTaintCheck(isTainted);
        }

        if (isTainted) {
            Abort abort = Configuration.getConfiguration().getAbort();
            System.out.println(taintAware.toString());

            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            List<StackTraceElement> cleanedStackTrace = new ArrayList<>();
            for (int i = 1; i < stackTrace.length; i++) {
                StackTraceElement ste = stackTrace[i];
                if (!ste.getClassName().startsWith(IASTaintHandler.class.getName())) {
                    cleanedStackTrace.add(ste);
                }
            }
            abort.abort(taintAware, sink, category, cleanedStackTrace);
        }
        return null;
    }

    private static Void setTaint(Object taintAware, int sourceId) {
        IASTaintSource source = IASTaintSourceRegistry.getInstance().get(sourceId);
        ((IASTaintAware) taintAware).setTaint(source);
        return null;
    }

    public static Object traverseObject(Object object, Function<Object, Object> traverser, Function<IASTaintAware, Void> atomicHandler) {
        if (object == null) {
            return null;
        }
        boolean isArray = object.getClass().isArray();
        boolean isIterable = Iterable.class.isAssignableFrom(object.getClass());
        boolean isEnumerate = Enumeration.class.isAssignableFrom(object.getClass());
        boolean isMap = Map.class.isAssignableFrom(object.getClass());
        Class<?> cls = object.getClass();
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
        }
        return object;
    }

    public static Object checkTaint(Object object, String sink, String category) {
        if (object instanceof IASTaintAware) {
            handleTaint((IASTaintAware) object, sink, category);
            return object;
        }

        return traverseObject(object, o -> checkTaint(o, sink, category), taintAware -> handleTaint(taintAware, sink, category));
    }

    public static Object taint(Object object, int sourceId) {
        if (object instanceof IASTaintAware) {
            IASTaintSource source = IASTaintSourceRegistry.getInstance().get(sourceId);
            ((IASTaintAware) object).setTaint(source);
            return object;
        }
        return traverseObject(object, o -> taint(o, sourceId), taintAware -> setTaint(taintAware, sourceId));
    }
}

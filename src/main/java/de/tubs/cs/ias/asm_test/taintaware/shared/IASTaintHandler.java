package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class IASTaintHandler {
    private static Void handleTaint(Object taintAware) {
        // TODO Different taint handling
        if (((IASTaintAware) taintAware).isTainted()) {
            System.err.printf("String %s is tainted!\nAborting..!\n", taintAware);
            System.exit(1);
        }
        return null;
    }

    private static Void setTaint(Object taintAware) {
        ((IASTaintAware) taintAware).setTaint(true);
        return null;
    }

    public static Object traversObject(Object object, Function<Object, Object> traverser, Function<Object, Void> atomicHandler) {
        if (object == null) {
            return null;
        }
        boolean isArray = object.getClass().isArray();
        boolean isIterable = Iterable.class.isAssignableFrom(object.getClass());
        boolean isEnumerate = Enumeration.class.isAssignableFrom(object.getClass());
        boolean isMap = Map.class.isAssignableFrom(object.getClass());
        Class<?> cls = isArray ? object.getClass().getComponentType() : object.getClass();
        if (IASTaintAware.class.isAssignableFrom(cls)) {
            if (isArray) {
                Object[] array = (Object[]) object;
                for (Object o : array) {
                    traverser.apply(o);
                }
            } else {
                atomicHandler.apply(object);
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

    public static Object checkTaint(Object object) {
        return traversObject(object, IASTaintHandler::checkTaint, IASTaintHandler::handleTaint);
    }

    public static Object taint(Object object) {
        return traversObject(object, IASTaintHandler::taint, IASTaintHandler::setTaint);
    }
}

package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.config.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IASToArrayProxy {
    private static final IASFactory factory = Configuration.getConfiguration().getTaintMethod().getFactory();

    public static Object[] toArray(Collection<?> list, Object[] array) {
        if (array instanceof IASStringable[]) {
            List<IASStringable> converted = new ArrayList<>(list.size());
            boolean notToConvert = false;
            for (Object s : list) {
                if (s instanceof IASStringable) {
                    notToConvert = true;
                    break;
                }
                converted.add(factory.valueOf(s));
            }
            if (notToConvert) {
                return list.toArray(array);
            } else {
                return converted.toArray(array);
            }
        }
        return list.toArray(array);
    }

    public static Object[] toArray(Collection<?> list) {
        boolean isString = true;
        if (list.size() > 0) {
            for (Object e : list) {
                if (!(e instanceof String)) {
                    isString = false;
                    break;
                }
            }
        }
        if (isString) {
            return toArray(list, new IASStringable[0]);
        }
        return list.toArray();
    }
}

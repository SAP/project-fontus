package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.config.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IASToArrayProxy {
    private static final IASFactory factory = Configuration.getConfiguration().getTaintMethod().getFactory();

    public static IASStringable[] toArray(Collection<?> list, IASStringable[] array) {
        List<IASStringable> converted = new ArrayList<>(list.size());
        for(Object s : list) {
            if (s instanceof IASStringable) {
                break;
            }
            converted.add(factory.valueOf(s));
        }
        return converted.toArray(array);
    }

    public static Object[] toArray(Collection<?> list) {
        boolean isString = true;
        if(list.size() > 0) {
            for(Object e : list) {
                if (!(e instanceof String)) {
                    isString = false;
                    break;
                }
            }
        }
        if(isString) {
            return toArray((Collection<String>) list, new IASStringable[0]);
        }
        return list.toArray();
    }
}

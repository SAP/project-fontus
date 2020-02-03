package de.tubs.cs.ias.asm_test.taintaware.range;

import java.util.concurrent.ConcurrentHashMap;

public final class IASStringPool {
    private static final ConcurrentHashMap<String, IASString> stringPool = new ConcurrentHashMap<>();

    public static IASString intern(IASString origIasString) {
        IASString existingIasString = stringPool.get(origIasString.getString());
        if(existingIasString != null) {
            return existingIasString;
        }

        stringPool.put(origIasString.getString(), origIasString);
        return origIasString;
    }
}

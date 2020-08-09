package de.tubs.cs.ias.asm_test.taintaware.shared;


import java.util.concurrent.ConcurrentHashMap;

public final class IASStringPool {
    private static final ConcurrentHashMap<String, IASStringable> stringPool = new ConcurrentHashMap<>();

    public static IASStringable intern(IASStringable origIasString) {
        IASStringable existingIasString = stringPool.get(origIasString.getString());
        if (existingIasString != null) {
            return existingIasString;
        }

        stringPool.put(origIasString.getString(), origIasString);
        return origIasString;
    }
}

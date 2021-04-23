package com.sap.fontus.taintaware.shared;

import com.sap.fontus.utils.lookups.CombinedExcludedLookup;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static com.sap.fontus.utils.ConversionUtils.convertClassToOrig;

public class IASLookupUtils {
    private static final CombinedExcludedLookup lookup = new CombinedExcludedLookup();

    public static boolean isJdkOrExcluded(Class cls) {
        return lookup.isJdkClass(cls) || lookup.isPackageExcluded(cls);
    }

    public static MethodType uninstrumentForJdk(MethodType methodType) {
        for (int i = 0; i < methodType.parameterCount(); i++) {
            Class<?> param = methodType.parameterType(i);
            if (lookup.isFontusClass(param)) {
                methodType.changeParameterType(i, convertClassToOrig(param));
            }
        }
        Class<?> returnType = methodType.returnType();
        if (lookup.isFontusClass(returnType)) {
            methodType.changeReturnType(convertClassToOrig(returnType));
        }
        return methodType;
    }

    public static boolean isInstrumented(MethodHandle methodHandle) {
        for (Class<?> param : methodHandle.type().parameterArray()) {
            if (lookup.isFontusClass(param)) {
                return true;
            }
        }
        return lookup.isFontusClass(methodHandle.type().returnType());
    }
}

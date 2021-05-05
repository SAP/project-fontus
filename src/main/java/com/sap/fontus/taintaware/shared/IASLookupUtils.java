package com.sap.fontus.taintaware.shared;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.instrumentation.strategies.InstrumentationHelper;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static com.sap.fontus.utils.ConversionUtils.*;

public class IASLookupUtils {
    private static final CombinedExcludedLookup lookup = new CombinedExcludedLookup();

    public static boolean isJdkOrExcluded(Class cls) {
        return lookup.isJdkClass(cls) || lookup.isPackageExcluded(cls);
    }

    public static MethodHandle convertForJdk(MethodHandle methodHandle) {
        InstrumentationHelper helper = InstrumentationHelper.getInstance(Configuration.getConfiguration().getTaintStringConfig());
        for (int i = 0; i < methodHandle.type().parameterCount(); i++) {
            Class<?> param = methodHandle.type().parameterType(i);
            if (helper.canHandleType(Type.getDescriptor(param))) {
                methodHandle = MethodHandles.filterArguments(methodHandle, i, getToOriginalConverter(convertClassToOrig(param)));
            }
        }
        Class<?> returnType = methodHandle.type().returnType();
        if (helper.canHandleType(Type.getDescriptor(returnType))) {
            methodHandle = MethodHandles.filterReturnValue(methodHandle, getToInstrumentedConverter(convertClassToOrig(returnType)));
        }
        return methodHandle;
    }

    public static MethodType uninstrumentForJdk(MethodType methodType) {
        for (int i = 0; i < methodType.parameterCount(); i++) {
            Class<?> param = methodType.parameterType(i);
            if (lookup.isFontusClass(param)) {
                methodType = methodType.changeParameterType(i, convertClassToOrig(param));
            }
        }
        Class<?> returnType = methodType.returnType();
        if (lookup.isFontusClass(returnType)) {
            methodType = methodType.changeReturnType(convertClassToOrig(returnType));
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

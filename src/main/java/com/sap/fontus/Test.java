package com.sap.fontus;

import com.sap.fontus.taintaware.bool.IASString;
import com.sap.fontus.taintaware.shared.IASLookupUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class Test {
    public static MethodHandle findStatic(MethodHandles.Lookup lookup, Class cls, IASString name, MethodType methodType) throws NoSuchMethodException, IllegalAccessException {
        boolean isExcluded = IASLookupUtils.isJdkOrExcluded(cls);
        if (isExcluded) {
            methodType = IASLookupUtils.uninstrumentForJdk(methodType);
        }
        MethodHandle mh = lookup.findStatic(cls, name.getString(), methodType);
        if (isExcluded) {
            mh = IASLookupUtils.convertForJdk(mh);
        }
        return mh;
    }
}

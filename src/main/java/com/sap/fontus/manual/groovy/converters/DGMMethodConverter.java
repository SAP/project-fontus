package com.sap.fontus.manual.groovy.converters;

import com.sap.fontus.utils.ClassFinder;
import com.sap.fontus.utils.ConversionUtils;
import com.sap.fontus.utils.InstrumentationFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;

public class DGMMethodConverter {
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final MethodHandle parameterFieldGetter;
    private static final MethodHandle returnTypeGetter;
    private static final MethodHandle returnTypeSetter;

    static {
        try {
            Class<?> dgmMethodClass = InstrumentationFactory.createClassFinder().findClass("org.codehaus.groovy.reflection.GeneratedMetaMethod$DgmMethodRecord");
            if (dgmMethodClass == null) {
                throw new ClassNotFoundException("org.codehaus.groovy.reflection.GeneratedMetaMethod$DgmMethodRecord");
            }
            parameterFieldGetter = lookup.findGetter(dgmMethodClass, "parameters", Class[].class);
            returnTypeGetter = lookup.findGetter(dgmMethodClass, "returnType", Class.class);
            returnTypeSetter = lookup.findSetter(dgmMethodClass, "returnType", Class.class);
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static List<?> convertTypes(List<?> methods) {
        for (Object method : methods) {
            try {
                Class<?>[] parameters = (Class<?>[]) parameterFieldGetter.invoke(method);
                Class<?> returnType = (Class<?>) returnTypeGetter.invoke(method);
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = ConversionUtils.convertClassToConcrete(parameters[i]);
                }
                returnTypeSetter.invoke(method, ConversionUtils.convertClassToConcrete(returnType));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return methods;
    }
}

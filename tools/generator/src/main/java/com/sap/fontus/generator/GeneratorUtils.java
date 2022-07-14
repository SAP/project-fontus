package com.sap.fontus.generator;

import com.sap.fontus.Constants;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

class GeneratorUtils {
    static String generateName(Constructor<?> m) {
        return String.format("%s.%s", m.getDeclaringClass().getSimpleName(), Constants.Init);
    }

    static String generateName(Method m) {
        return String.format("%s.%s", m.getDeclaringClass().getSimpleName(), m.getName());
    }
}

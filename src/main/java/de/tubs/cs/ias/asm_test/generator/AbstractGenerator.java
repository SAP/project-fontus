package de.tubs.cs.ias.asm_test.generator;

import de.tubs.cs.ias.asm_test.Constants;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class AbstractGenerator {
    public String generateName(Constructor m) {
        return String.format("%s.%s", m.getDeclaringClass().getSimpleName(), Constants.Init);
    }

    public String generateName(Method m) {
        return String.format("%s.%s", m.getDeclaringClass().getSimpleName(), m.getName());
    }
}

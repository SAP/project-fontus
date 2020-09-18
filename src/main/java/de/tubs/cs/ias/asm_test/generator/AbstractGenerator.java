package de.tubs.cs.ias.asm_test.generator;

import java.lang.reflect.Method;

public class AbstractGenerator {
    public String generateName(Method m) {
        return String.format("%s.%s", m.getDeclaringClass().getSimpleName(), m.getName());
    }
}

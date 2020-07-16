package de.tubs.cs.ias.asm_test.utils;

import de.tubs.cs.ias.asm_test.Constants;

public class MethodUtils {
    public static boolean isToString(String name, String methodDescriptor) {
        return "()Ljava/lang/String;".equals(methodDescriptor) && Constants.ToString.equals(name);
    }
}

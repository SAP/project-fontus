package com.sap.fontus.instrumentation;

import com.sap.fontus.asm.FunctionCall;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringInstrumentationStrategyTests {
    @Test
    void multiDimensionalArrayHandlingTest() {
        InstrumentationHelper helper = new InstrumentationHelper();
        String owner = "[[Ljava/lang/String;";
        FunctionCall fc = helper.rewriteOwnerMethod(new FunctionCall(Opcodes.INVOKEVIRTUAL, owner, "clone", "()Ljava/lang/Object;", false));
        int orig = Type.getType(owner).getDimensions();
        int res = Type.getType(fc.getOwner()).getDimensions();
        assertEquals(orig, res);
    }
}

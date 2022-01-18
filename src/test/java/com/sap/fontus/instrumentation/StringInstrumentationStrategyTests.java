package com.sap.fontus.instrumentation;

import com.sap.fontus.asm.FunctionCall;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.stream.Stream;

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

    private static Stream<Arguments> provideParameters() {
        return Stream.of(
                Arguments.of("java.lang.String","com.sap.fontus.taintaware.unified.IASString"),
                Arguments.of("[Ljava.lang.String;","[Lcom.sap.fontus.taintaware.unified.IASString;"),
                Arguments.of("java.lang.StringBuilder","com.sap.fontus.taintaware.unified.IASStringBuilder"),
                Arguments.of("[Ljava.lang.StringBuilder;","[Lcom.sap.fontus.taintaware.unified.IASStringBuilder;"),
                Arguments.of("[[[[Ljava.lang.String;","[[[[Lcom.sap.fontus.taintaware.unified.IASString;")
        );
    }

    @ParameterizedTest
    @MethodSource("provideParameters")
    public void testClassNameConversion(String input, String expected) {
        InstrumentationHelper helper = new InstrumentationHelper();
        assertEquals(expected, helper.translateClassName(input).orElse(input));
    }
}

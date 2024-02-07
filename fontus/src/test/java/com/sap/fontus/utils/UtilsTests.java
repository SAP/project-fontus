package com.sap.fontus.utils;

import com.sap.fontus.Constants;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.taintaware.unified.IASString;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"ClassIndependentOfModule", "ClassOnlyUsedInOneModule"})
class UtilsTests {
    private static final TaintMethod TAINT_METHOD = TaintMethod.defaultTaintMethod();
    private static final Pattern StringPattern = Pattern.compile(Constants.StringQN, Pattern.LITERAL);
    private static final String desc = "(Ljava/lang/String;Ljava/lang/String;)I";
    private static final String expected = String.format("(%s%s)I", Type.getType(IASString.class).getDescriptor(), Type.getType(IASString.class).getDescriptor());

    @BeforeAll
    static void init() {
        Configuration.setTestConfig(TAINT_METHOD);
    }

    @Test
    void testTypeReplacer() {
        Type t1 = Type.getType(desc);
        Type t2 = Utils.instrumentType(t1, new InstrumentationHelper());

        assertEquals(expected, t2.getDescriptor(), "Both types shall be equal");
    }

    @Test
    void testHandleReplacer() {
        Handle h = new Handle(Opcodes.H_INVOKESTATIC, "LambdaTest", "lambda$main$0", desc, false);
        Handle h2 = Utils.instrumentHandle(h, new InstrumentationHelper());
        assertEquals(expected, h2.getDesc(), "Descriptors shall be equal");
    }
}

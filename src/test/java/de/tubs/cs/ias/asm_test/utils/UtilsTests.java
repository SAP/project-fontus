package de.tubs.cs.ias.asm_test.utils;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.config.TaintMethod;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"ClassIndependentOfModule", "ClassOnlyUsedInOneModule"})
class UtilsTests {
    private static final TaintMethod TAINT_METHOD = TaintMethod.defaultTaintMethod();
    private static final TaintStringConfig TAINT_STRING_CONFIG = new TaintStringConfig(TAINT_METHOD);
    private static final Pattern StringPattern = Pattern.compile(Constants.StringQN, Pattern.LITERAL);
    private static final String desc = "(Ljava/lang/String;Ljava/lang/String;)I";
    private static final String expected = String.format("(%s%s)I", TAINT_STRING_CONFIG.getTStringDesc(), TAINT_STRING_CONFIG.getTStringDesc());

    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TAINT_METHOD);
    }

    @Test
    void testTypeReplacer() {
        Type t1 = Type.getType(desc);
        Type t2 = Utils.instrumentType(t1, TAINT_STRING_CONFIG);

        assertEquals(expected, t2.getDescriptor(), "Both types shall be equal");
    }

    @Test
    void testHandleReplacer() {
        Handle h = new Handle(Opcodes.H_INVOKESTATIC, "LambdaTest", "lambda$main$0", desc, false);
        Handle h2 = Utils.instrumentHandle(h, TAINT_STRING_CONFIG, new ArrayList<>());
        assertEquals(expected, h2.getDesc(), "Descriptors shall be equal");
    }
}

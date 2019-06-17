package de.tubs.cs.ias.asm_test;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class UtilsTests {
    private static final Pattern StringPattern = Pattern.compile(Constants.StringQN, Pattern.LITERAL);

    @Test
    public void testTypeReplacer() {
        String desc = "(Ljava/lang/String;Ljava/lang/String;)I";
        String expected = "(Lde/tubs/cs/ias/asm_test/IASString;Lde/tubs/cs/ias/asm_test/IASString;)I";
        Type t1 = Type.getType(desc);
        Type t2 = Utils.instrumentType(t1);

        assertEquals(expected, t2.getDescriptor(), "Both types shall be equal");
    }

    public void testHandleReplacer() {
        String desc = "(Ljava/lang/String;Ljava/lang/String;)I";
        String expected = "(Lde/tubs/cs/ias/asm_test/IASString;Lde/tubs/cs/ias/asm_test/IASString;)I";
        Handle h = new Handle(Opcodes.H_INVOKESTATIC, "LambdaTest", "lambda$main$0", desc, false);
        Handle h2 = Utils.instrumentHandle(h);
        assertEquals(expected, h2.getDesc(),"Descriptors shall be equal");
    }
}

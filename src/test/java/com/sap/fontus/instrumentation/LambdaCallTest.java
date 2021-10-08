package com.sap.fontus.instrumentation;

import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.unified.IASString;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LambdaCallTest {

    /* The two methods below show an example of what the LambdaCall wrapper is doing */
    public void test1() {
        int c = (int) (Math.random() * 100);
        /* This is a lambda call with Comparator as argument.
         * In the bytecode, a wrapper method is created which includes:
         *  - the local variables (in this case c)
         *  - the lambda arguments (in this case a and b)
         *
         *    // handle kind 0x6 : INVOKESTATIC
         *    com/sap/fontus/instrumentation/LambdaCallTest.lambda$test1$0(ILjava/lang/Integer;Ljava/lang/Integer;)I,
         *            (Ljava/lang/Integer;Ljava/lang/Integer;)I
         * 
         * The LambdaCall class will wrap these calls depending on the presence of Taintable variables
         */
        test((a, b) -> b - a - c);
    }

    public void test(Comparator<Integer> test) {

    }

    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.defaultTaintMethod());
    }

    @Test
    public void testPatternParameter() {
        InstrumentationHelper instrumentationHelper = new InstrumentationHelper();
        Descriptor implementationDescriptor = new Descriptor(new String[]{Type.getDescriptor(Pattern.class), Type.getDescriptor(File.class), Type.getDescriptor(String.class)}, Type.getDescriptor(boolean.class));
        Descriptor invokeDynamicDescriptor = new Descriptor(new String[]{}, Type.getDescriptor(Comparator.class));
        Handle target = new Handle(Opcodes.H_INVOKESTATIC, "Test", "test", implementationDescriptor.toDescriptor(), false);
        LambdaCall call = new LambdaCall(Type.getType(FilenameFilter.class), target, invokeDynamicDescriptor.toAsmMethodType());

        Descriptor descriptor = call.getProxyDescriptor(Thread.currentThread().getContextClassLoader(), instrumentationHelper);

        assertEquals("(Lcom/sap/fontus/taintaware/unified/IASPattern;Ljava/io/File;Ljava/lang/String;)Z", descriptor.toDescriptor());
    }

    @Test
    public void testObjectParameter() {
        InstrumentationHelper instrumentationHelper = new InstrumentationHelper();
        Descriptor implementationDescriptor = new Descriptor(new String[]{Type.getDescriptor(Integer.class), Type.getDescriptor(Integer.class)}, Type.getDescriptor(Integer.class));
        Descriptor invokeDynamicDescriptor = new Descriptor(new String[]{}, Type.getDescriptor(Comparator.class));
        Handle target = new Handle(Opcodes.H_INVOKESTATIC, "Test", "test", implementationDescriptor.toDescriptor(), false);
        LambdaCall call = new LambdaCall(Type.getType(Comparator.class), target, invokeDynamicDescriptor.toAsmMethodType());

        Descriptor descriptor = call.getProxyDescriptor(Thread.currentThread().getContextClassLoader(), instrumentationHelper);

        assertEquals("(Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/lang/Integer;", descriptor.toDescriptor());
    }

    @Test
    public void testObjectReturnUninstrumented() {
        InstrumentationHelper instrumentationHelper = new InstrumentationHelper();
        Descriptor implementationDescriptor = new Descriptor(new String[]{Type.getDescriptor(Integer.class), Type.getDescriptor(Integer.class)}, Type.getDescriptor(Integer.class));
        Descriptor invokeDynamicDescriptor = new Descriptor(new String[]{}, Type.getDescriptor(BiFunction.class));
        Handle target = new Handle(Opcodes.H_INVOKESTATIC, "Test", "test", implementationDescriptor.toDescriptor(), false);
        LambdaCall call = new LambdaCall(Type.getType(BiFunction.class), target, invokeDynamicDescriptor.toAsmMethodType());

        Descriptor descriptor = call.getProxyDescriptor(Thread.currentThread().getContextClassLoader(), instrumentationHelper);

        assertEquals("(Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/lang/Integer;", descriptor.toDescriptor());
    }

    @Test
    public void testObjectReturnInstrumented() {
        InstrumentationHelper instrumentationHelper = new InstrumentationHelper();
        Descriptor implementationDescriptor = new Descriptor(new String[]{Type.getDescriptor(IASString.class), Type.getDescriptor(IASString.class)}, Type.getDescriptor(IASString.class));
        Descriptor invokeDynamicDescriptor = new Descriptor(new String[]{}, Type.getDescriptor(BiFunction.class));
        Handle target = new Handle(Opcodes.H_INVOKESTATIC, "Test", "test", implementationDescriptor.toDescriptor(), false);
        LambdaCall call = new LambdaCall(Type.getType(BiFunction.class), target, invokeDynamicDescriptor.toAsmMethodType());

        Descriptor descriptor = call.getProxyDescriptor(Thread.currentThread().getContextClassLoader(), instrumentationHelper);

        assertEquals("(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", descriptor.toDescriptor());
    }
}

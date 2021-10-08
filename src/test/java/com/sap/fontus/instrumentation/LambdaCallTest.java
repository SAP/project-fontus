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
//    // access flags 0x1
//    public test1()V
//    L0
//    LINENUMBER 23 L0
//    INVOKESTATIC java/lang/Math.random ()D
//    LDC 100.0
//    DMUL
//            D2I
//    ISTORE 1
//    L1
//    LINENUMBER 24 L1
//    ALOAD 0
//    ILOAD 1
//    INVOKEDYNAMIC compare(I)Ljava/util/Comparator; [
//    // handle kind 0x6 : INVOKESTATIC
//    java/lang/invoke/LambdaMetafactory.metafactory(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
//    // arguments:
//      (Ljava/lang/Object;Ljava/lang/Object;)I,
//    // handle kind 0x6 : INVOKESTATIC
//    com/sap/fontus/instrumentation/LambdaCallTest.lambda$test1$0(ILjava/lang/Integer;Ljava/lang/Integer;)I,
//            (Ljava/lang/Integer;Ljava/lang/Integer;)I
//    ]
//    INVOKEVIRTUAL com/sap/fontus/instrumentation/LambdaCallTest.test (Ljava/util/Comparator;)V
//            L2
//    LINENUMBER 25 L2
//            RETURN
//    L3
//    LOCALVARIABLE this Lcom/sap/fontus/instrumentation/LambdaCallTest; L0 L3 0
//    LOCALVARIABLE c I L1 L3 1
//    MAXSTACK = 4
//    MAXLOCALS = 2

    public void test1() {
        int c = (int) (Math.random() * 100);
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
        Descriptor inputDescriptor = new Descriptor(new String[]{Type.getDescriptor(Pattern.class), Type.getDescriptor(File.class), Type.getDescriptor(String.class)}, Type.getDescriptor(FilenameFilter.class));
        Handle target = new Handle(Opcodes.H_INVOKESTATIC, "Test", "test", inputDescriptor.toDescriptor(), false);
        LambdaCall call = new LambdaCall(Type.getType(FilenameFilter.class), target, inputDescriptor.toAsmMethodType());

        Descriptor descriptor = call.getProxyDescriptor(Thread.currentThread().getContextClassLoader(), instrumentationHelper);

        assertEquals("(Lcom/sap/fontus/taintaware/unified/IASPattern;Ljava/io/File;Ljava/lang/String;)Z", descriptor.toDescriptor());
    }

    @Test
    public void testObjectParameter() {
        InstrumentationHelper instrumentationHelper = new InstrumentationHelper();
        Descriptor implementationDescriptor = new Descriptor(new String[]{Type.getDescriptor(Integer.class), Type.getDescriptor(Integer.class)}, Type.getDescriptor(Comparator.class));
        Descriptor invokeDynamicDescriptor = new Descriptor(new String[]{}, Type.getDescriptor(Comparator.class));
        Handle target = new Handle(Opcodes.H_INVOKESTATIC, "Test", "test", implementationDescriptor.toDescriptor(), false);
        LambdaCall call = new LambdaCall(Type.getType(Comparator.class), target, invokeDynamicDescriptor.toAsmMethodType());

        Descriptor descriptor = call.getProxyDescriptor(Thread.currentThread().getContextClassLoader(), instrumentationHelper);

        assertEquals("(Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/util/Comparator;", descriptor.toDescriptor());
    }

    @Test
    public void testObjectReturnUninstrumented() {
        InstrumentationHelper instrumentationHelper = new InstrumentationHelper();
        Descriptor inputDescriptor = new Descriptor(new String[]{Type.getDescriptor(Integer.class), Type.getDescriptor(Integer.class)}, Type.getDescriptor(BiFunction.class));
        Handle target = new Handle(Opcodes.H_INVOKESTATIC, "Test", "test", inputDescriptor.toDescriptor(), false);
        LambdaCall call = new LambdaCall(Type.getType(BiFunction.class), target, inputDescriptor.toAsmMethodType());

        Descriptor descriptor = call.getProxyDescriptor(Thread.currentThread().getContextClassLoader(), instrumentationHelper);

        assertEquals("(Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/util/function/BiFunction;", descriptor.toDescriptor());
    }

    @Test
    public void testObjectReturnInstrumented() {
        InstrumentationHelper instrumentationHelper = new InstrumentationHelper();
        Descriptor inputDescriptor = new Descriptor(new String[]{Type.getDescriptor(IASString.class), Type.getDescriptor(IASString.class)}, Type.getDescriptor(BiFunction.class));
        Handle target = new Handle(Opcodes.H_INVOKESTATIC, "Test", "test", inputDescriptor.toDescriptor(), false);
        LambdaCall call = new LambdaCall(Type.getType(BiFunction.class), target, inputDescriptor.toAsmMethodType());

        Descriptor descriptor = call.getProxyDescriptor(Thread.currentThread().getContextClassLoader(), instrumentationHelper);

        assertEquals("(Ljava/lang/String;Ljava/lang/String;)Ljava/util/function/BiFunction;", descriptor.toDescriptor());
    }
}

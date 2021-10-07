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

        assertEquals("(Lcom/sap/fontus/taintaware/unified/IASPattern;Ljava/io/File;Ljava/lang/String;)Ljava/io/FilenameFilter;", descriptor.toDescriptor());
    }

    @Test
    public void testObjectParameter() {
        InstrumentationHelper instrumentationHelper = new InstrumentationHelper();
        Descriptor inputDescriptor = new Descriptor(new String[]{Type.getDescriptor(Integer.class), Type.getDescriptor(Integer.class)}, Type.getDescriptor(Comparator.class));
        Handle target = new Handle(Opcodes.H_INVOKESTATIC, "Test", "test", inputDescriptor.toDescriptor(), false);
        LambdaCall call = new LambdaCall(Type.getType(Comparator.class), target, inputDescriptor.toAsmMethodType());

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

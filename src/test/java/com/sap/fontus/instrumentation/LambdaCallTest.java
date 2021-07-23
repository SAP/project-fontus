package com.sap.fontus.instrumentation;

import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.bool.IASString;
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
        InstrumentationHelper instrumentationHelper = new InstrumentationHelper(Configuration.getConfiguration().getTaintStringConfig());
        Handle target = new Handle(Opcodes.H_INVOKESTATIC, "Test", "test", new Descriptor(new String[]{Type.getDescriptor(Pattern.class), Type.getDescriptor(File.class), Type.getDescriptor(String.class)}, Type.getDescriptor(boolean.class)).toDescriptor(), false);
        LambdaCall call = new LambdaCall(Type.getType(FilenameFilter.class), target);

        Descriptor descriptor = call.getProxyDescriptor(Thread.currentThread().getContextClassLoader(), instrumentationHelper);

        assertEquals("(Lcom/sap/fontus/taintaware/bool/IASPattern;Ljava/io/File;Ljava/lang/String;)Z", descriptor.toDescriptor());
    }

    @Test
    public void testObjectParameter() {
        InstrumentationHelper instrumentationHelper = new InstrumentationHelper(Configuration.getConfiguration().getTaintStringConfig());
        Handle target = new Handle(Opcodes.H_INVOKESTATIC, "Test", "test", new Descriptor(new String[]{Type.getDescriptor(Integer.class), Type.getDescriptor(Integer.class)}, Type.getDescriptor(Integer.class)).toDescriptor(), false);
        LambdaCall call = new LambdaCall(Type.getType(Comparator.class), target);

        Descriptor descriptor = call.getProxyDescriptor(Thread.currentThread().getContextClassLoader(), instrumentationHelper);

        assertEquals("(Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/lang/Integer;", descriptor.toDescriptor());
    }

    @Test
    public void testObjectReturnUninstrumented() {
        InstrumentationHelper instrumentationHelper = new InstrumentationHelper(Configuration.getConfiguration().getTaintStringConfig());
        Handle target = new Handle(Opcodes.H_INVOKESTATIC, "Test", "test", new Descriptor(new String[]{Type.getDescriptor(Integer.class), Type.getDescriptor(Integer.class)}, Type.getDescriptor(Integer.class)).toDescriptor(), false);
        LambdaCall call = new LambdaCall(Type.getType(BiFunction.class), target);

        Descriptor descriptor = call.getProxyDescriptor(Thread.currentThread().getContextClassLoader(), instrumentationHelper);

        assertEquals("(Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/lang/Integer;", descriptor.toDescriptor());
    }

    @Test
    public void testObjectReturnInstrumented() {
        InstrumentationHelper instrumentationHelper = new InstrumentationHelper(Configuration.getConfiguration().getTaintStringConfig());
        Handle target = new Handle(Opcodes.H_INVOKESTATIC, "Test", "test", new Descriptor(new String[]{Type.getDescriptor(IASString.class), Type.getDescriptor(IASString.class)}, Type.getDescriptor(IASString.class)).toDescriptor(), false);
        LambdaCall call = new LambdaCall(Type.getType(BiFunction.class), target);

        Descriptor descriptor = call.getProxyDescriptor(Thread.currentThread().getContextClassLoader(), instrumentationHelper);

        assertEquals("(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", descriptor.toDescriptor());
    }
}

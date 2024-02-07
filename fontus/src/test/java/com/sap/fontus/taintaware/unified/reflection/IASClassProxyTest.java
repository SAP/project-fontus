package com.sap.fontus.taintaware.unified.reflection;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.taintaware.unified.IASFormatter;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASStringBuffer;
import com.sap.fontus.taintaware.unified.IASStringBuilder;
import com.sap.fontus.taintaware.unified.reflect.IASClassProxy;
import com.sap.fontus.taintaware.unified.reflect.IASMethod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Formatter;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IASClassProxyTest {

    @BeforeAll
    static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    @Test
    void testReflectionOnJdkInterface() throws NoSuchMethodException {
        @SuppressWarnings("WeakerAccess")
        class FilterImpl implements FilenameFilter {
            @Override
            public boolean accept(File dir, String name) {
                return false;
            }
            public boolean accept(File dir, IASString name) {
                return false;
            }
        }

        IASMethod[] methods = IASClassProxy.getDeclaredMethods(FilterImpl.class);

        assertEquals(1, methods.length);
        assertEquals(FilterImpl.class.getMethod("accept", File.class, IASString.class), methods[0].getMethod());
    }

    @Test
    void testInstrumentationHelper() {
        InstrumentationHelper instrumentationHelper = new InstrumentationHelper();

        Type stringType = Type.getType(String.class);
        Type instrumentedType = Type.getType(IASString.class);

        String stringName = instrumentationHelper.instrumentQN(stringType.getInternalName());
        assertEquals(instrumentedType.getInternalName(), stringName);

        String uninstrumentedName = instrumentationHelper.uninstrumentQN(stringName);
        assertEquals(stringType.getInternalName(), uninstrumentedName);
    }

    private static Stream<Arguments> provideParameters() {
        int[] dimensions = { 1, 2, 3, 4, 5, 6 };
        return Stream.of(
                Arguments.of(IASString.class, String.class),
                Arguments.of(IASStringBuilder.class, StringBuilder.class),
                Arguments.of(IASStringBuffer.class, StringBuffer.class),
                Arguments.of(IASFormatter.class, Formatter.class),
                Arguments.of(IASMethod.class, Method.class),
                Arguments.of(Array.newInstance(IASString.class, 0).getClass(), Array.newInstance(String.class, 0).getClass()),
                Arguments.of(Array.newInstance(IASStringBuilder.class, 0).getClass(), Array.newInstance(StringBuilder.class, 0).getClass()),
                Arguments.of(Array.newInstance(IASStringBuffer.class, 0).getClass(), Array.newInstance(StringBuffer.class, 0).getClass()),
                Arguments.of(Array.newInstance(IASString.class, dimensions).getClass(), Array.newInstance(String.class, dimensions).getClass()),
                Arguments.of(Array.newInstance(IASStringBuilder.class, dimensions).getClass(), Array.newInstance(StringBuilder.class, dimensions).getClass()),
                Arguments.of(Array.newInstance(IASStringBuffer.class, dimensions).getClass(), Array.newInstance(StringBuffer.class, dimensions).getClass()),
                Arguments.of(String.class, String.class),
                Arguments.of(Integer.class, Integer.class),
                Arguments.of(int.class, int.class)
        );
    }

    @ParameterizedTest
    @MethodSource("provideParameters")
    void testClassProxyGetName(Class<?> input, Class<?> instrumented) {
        assertEquals(instrumented.getName(), IASClassProxy.getName(input).getString());
    }

    @ParameterizedTest
    @MethodSource("provideParameters")
    void testClassProxyGetSimpleName(Class<?> input, Class<?> instrumented) {
        assertEquals(instrumented.getSimpleName(), IASClassProxy.getSimpleName(input).getString());
    }

    @ParameterizedTest
    @MethodSource("provideParameters")
    void testClassProxyGetCanonicalName(Class<?> input, Class<?> instrumented) {
        assertEquals(instrumented.getCanonicalName(), IASClassProxy.getCanonicalName(input).getString());
    }

}

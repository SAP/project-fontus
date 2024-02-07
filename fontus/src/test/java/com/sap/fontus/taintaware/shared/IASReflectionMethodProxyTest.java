package com.sap.fontus.taintaware.shared;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.reflect.IASClassProxy;
import com.sap.fontus.taintaware.unified.reflect.IASMethod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import outerpackage.Anno;
import outerpackage.ApplicationClass;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IASReflectionMethodProxyTest {
    @BeforeAll
    static void setup() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    @Test
    void testGetMethodWithParameter() throws NoSuchMethodException {
        Class<?> cls = IASString.class;
        IASString methodName = new IASString("concat");
        Class<?>[] parameters = {IASString.class};
        Method expected = IASString.class.getMethod(methodName.getString(), IASString.class);

        IASMethod actual = IASClassProxy.getMethod(cls, methodName, parameters);

        assertEquals(expected, actual.getMethod());
    }

    @Test
    void testGetMethodWithExternalParameter() throws NoSuchMethodException {
        Class<?> cls = IASString.class;
        IASString methodName = new IASString("charAt");
        Class<?>[] parameters = {int.class};
        Method expected = IASString.class.getMethod(methodName.getString(), int.class);

        IASMethod actual = IASClassProxy.getMethod(cls, methodName, parameters);

        assertEquals(expected, actual.getMethod());
    }

    @Test
    void testGetMethodWithEmptyParameter() throws NoSuchMethodException {
        Class<?> cls = IASString.class;
        IASString methodName = new IASString("trim");
        Class<?>[] parameters = {};
        Method expected = IASString.class.getMethod(methodName.getString());

        IASMethod actual = IASClassProxy.getMethod(cls, methodName, parameters);

        assertEquals(expected, actual.getMethod());
    }

    @Test
    void testGetMethodWithNullParameter() throws NoSuchMethodException {
        Class<?> cls = IASString.class;
        IASString methodName = new IASString("trim");
        Class<?>[] parameters = null;
        Method expected = IASString.class.getMethod(methodName.getString());

        IASMethod actual = IASClassProxy.getMethod(cls, methodName, parameters);

        assertEquals(expected, actual.getMethod());
    }

    // Not reproducible test failure: Test fails for normal execution with ArrayStoreException, test succeeds for debugging execution without any exceptions
    @Test
    void testInvokeAnnotation() throws Throwable {
        Anno anno = ApplicationClass.class.getAnnotation(Anno.class);
        IASMethod valueMethod = IASClassProxy.getMethod(Anno.class, new IASString("value"), new Class[0]);
        IASMethod arrayMethod = IASClassProxy.getMethod(Anno.class, new IASString("array"), new Class[0]);

        IASString value = (IASString) valueMethod.invoke(anno);
        IASString[] array = (IASString[]) arrayMethod.invoke(anno);

        assertEquals("Hallo", value.getString());
        assertArrayEquals(new IASString[]{new IASString("Hallo"), new IASString("Welt")}, array);
    }

    @Test
    void testInvokeJdk() throws Throwable {
        IASMethod method = IASClassProxy.getMethod(MessageFormat.class, new IASString("format"), new Class[]{IASString.class, Object[].class});
        IASString format = new IASString("{0}, {1}");
        IASString insert0 = new IASString("insert0");
        IASString insert1 = new IASString("insert1");

        IASString result = (IASString) method.invoke(null, format, new Object[]{insert0, insert1});

        assertEquals(new IASString("insert0, insert1"), result);
    }

    @Test
    void testInvokeTaintaware() throws Throwable {
        IASMethod method = IASClassProxy.getMethod(IASString.class, new IASString("concat"), new Class[]{IASString.class});
        IASString first = new IASString("first");
        IASString second = new IASString("second");

        IASString concatenated = (IASString) method.invoke(first, second);

        assertEquals(new IASString("firstsecond"), concatenated);
    }

    @Test
    void testInvokeApplicationClass() throws Throwable {
        IASMethod method = IASClassProxy.getMethod(ApplicationClass.class, new IASString("doStuff"), new Class[]{IASString.class});
        ApplicationClass applicationClass = new ApplicationClass();
        IASString input = new IASString("input");

        IASString concatenated = (IASString) method.invoke(applicationClass, input);

        assertEquals(new IASString("inputconcat"), concatenated);

    }

    @Test
    void testGetDeclaredMethodsSorting() throws NoSuchMethodException {
        Method[] expected = {MethodsToSortParameter.class.getMethod("method", IASString.class)};

        IASMethod[] actual = IASClassProxy.getDeclaredMethods(MethodsToSortParameter.class);

        List<Method> actualMethods = new ArrayList<>();
        for (IASMethod m : actual) {
            actualMethods.add(m.getMethod());
        }
        assertArrayEquals(expected, actualMethods.toArray());
    }

    private static class MethodsToSortParameter {
        public void method(String s) {

        }

        public void method(IASString s) {

        }
    }

}

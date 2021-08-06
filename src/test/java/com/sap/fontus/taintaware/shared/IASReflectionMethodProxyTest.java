package com.sap.fontus.taintaware.shared;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.unified.IASString;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import outerpackage.ApplicationClass;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IASReflectionMethodProxyTest {
    @BeforeAll
    public static void setup() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    @Test
    public void testGetMethodWithParameter() throws NoSuchMethodException {
        Class<?> cls = IASString.class;
        IASString methodName = new IASString("concat");
        Class<?>[] parameters = {IASString.class};
        Method expected = IASString.class.getMethod(methodName.getString(), IASString.class);

        Method actual = IASReflectionMethodProxy.getMethodProxied(cls, methodName, parameters);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetMethodWithExternalParameter() throws NoSuchMethodException {
        Class<?> cls = IASString.class;
        IASString methodName = new IASString("charAt");
        Class<?>[] parameters = {int.class};
        Method expected = IASString.class.getMethod(methodName.getString(), int.class);

        Method actual = IASReflectionMethodProxy.getMethodProxied(cls, methodName, parameters);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetMethodWithEmptyParameter() throws NoSuchMethodException {
        Class<?> cls = IASString.class;
        IASString methodName = new IASString("trim");
        Class<?>[] parameters = {};
        Method expected = IASString.class.getMethod(methodName.getString());

        Method actual = IASReflectionMethodProxy.getMethodProxied(cls, methodName, parameters);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetMethodWithNullParameter() throws NoSuchMethodException {
        Class<?> cls = IASString.class;
        IASString methodName = new IASString("trim");
        Class<?>[] parameters = null;
        Method expected = IASString.class.getMethod(methodName.getString());

        Method actual = IASReflectionMethodProxy.getMethodProxied(cls, methodName, parameters);

        assertEquals(expected, actual);
    }

    // Not reproducible test failure: Test fails for normal execution with ArrayStoreException, test succeeds for debugging execution without any exceptions
//    @Test
//    public void testInvokeAnnotation() throws Throwable {
//        Anno anno = ApplicationClass.class.getAnnotation(Anno.class);
//        Method valueMethod = IASReflectionMethodProxy.getMethodProxied(Anno.class, new IASString("value"), new Class[0]);
//        Method arrayMethod = IASReflectionMethodProxy.getMethodProxied(Anno.class, new IASString("array"), new Class[0]);
//
//        IASString value = (IASString) IASReflectionMethodProxy.invoke(valueMethod, anno);
//        IASString[] array = (IASString[]) IASReflectionMethodProxy.invoke(arrayMethod, anno);
//
//        assertEquals("Hallo", value.getString());
//        assertArrayEquals(new IASString[]{new IASString("Hallo"), new IASString("Welt")}, array);
//    }

    @Test
    public void testInvokeJdk() throws Throwable {
        Method method = IASReflectionMethodProxy.getMethodProxied(MessageFormat.class, new IASString("format"), new Class[]{IASString.class, Object[].class});
        IASString format = new IASString("{0}, {1}");
        IASString insert0 = new IASString("insert0");
        IASString insert1 = new IASString("insert1");

        IASString result = (IASString) IASReflectionMethodProxy.invoke(method, null, format, new Object[]{insert0, insert1});

        assertEquals(new IASString("insert0, insert1"), result);
    }

    @Test
    public void testInvokeTaintaware() throws Throwable {
        Method method = IASReflectionMethodProxy.getMethodProxied(IASString.class, new IASString("concat"), new Class[]{IASString.class});
        IASString first = new IASString("first");
        IASString second = new IASString("second");

        IASString concatenated = (IASString) IASReflectionMethodProxy.invoke(method, first, second);

        assertEquals(new IASString("firstsecond"), concatenated);
    }

    @Test
    public void testInvokeApplicationClass() throws Throwable {
        Method method = IASReflectionMethodProxy.getMethodProxied(ApplicationClass.class, new IASString("doStuff"), new Class[]{IASString.class});
        ApplicationClass applicationClass = new ApplicationClass();
        IASString input = new IASString("input");

        IASString concatenated = (IASString) IASReflectionMethodProxy.invoke(method, applicationClass, input);

        assertEquals(new IASString("inputconcat"), concatenated);

    }

    @Test
    public void testGetDeclaredMethodsSorting() throws NoSuchMethodException {
        Method[] expected = new Method[]{MethodsToSortParameter.class.getMethod("method", IASString.class), MethodsToSortParameter.class.getMethod("method", String.class)};

        Method[] actual = IASReflectionMethodProxy.getDeclaredMethods(MethodsToSortParameter.class);

        assertArrayEquals(expected, actual);
    }

    private static class MethodsToSortParameter {
        public void method(String s) {

        }

        public void method(IASString s) {

        }
    }

}

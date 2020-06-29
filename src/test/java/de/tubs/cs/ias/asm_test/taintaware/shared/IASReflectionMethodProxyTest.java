package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.taintaware.range.IASString;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IASReflectionMethodProxyTest {
    @Test
    public void testGetMethodWithParameter() throws NoSuchMethodException {
        Class cls = IASStringable.class;
        IASStringable methodName = new IASString("concat");
        Class[] parameters = {IASString.class};
        Method expected = IASStringable.class.getMethod(methodName.getString(), IASStringable.class);

        Method actual = IASReflectionMethodProxy.getMethodProxied(cls, methodName, parameters);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetMethodWithExternalParameter() throws NoSuchMethodException {
        Class cls = IASStringable.class;
        IASStringable methodName = new IASString("charAt");
        Class[] parameters = {int.class};
        Method expected = IASStringable.class.getMethod(methodName.getString(), int.class);

        Method actual = IASReflectionMethodProxy.getMethodProxied(cls, methodName, parameters);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetMethodWithEmptyParameter() throws NoSuchMethodException {
        Class cls = IASStringable.class;
        IASStringable methodName = new IASString("trim");
        Class[] parameters = {};
        Method expected = IASStringable.class.getMethod(methodName.getString());

        Method actual = IASReflectionMethodProxy.getMethodProxied(cls, methodName, parameters);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetMethodWithNullParameter() throws NoSuchMethodException {
        Class cls = IASStringable.class;
        IASStringable methodName = new IASString("trim");
        Class[] parameters = null;
        Method expected = IASStringable.class.getMethod(methodName.getString());

        Method actual = IASReflectionMethodProxy.getMethodProxied(cls, methodName, parameters);

        assertEquals(expected, actual);
    }
}

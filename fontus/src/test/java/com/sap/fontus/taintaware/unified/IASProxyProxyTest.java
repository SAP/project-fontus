package com.sap.fontus.taintaware.unified;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class IASProxyProxyTest {

    @BeforeAll
    static void setup() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    interface A {
        public int getA();
    }

    interface B {
        public int getB();
    }

    class ABImpl implements A, B {

        public int getA() { return 1; }

        public int getB() { return 2; }
    }

    @Test
    void testIASProxyProxyWithLambda() throws NoSuchMethodException {
        ClassLoader cl = this.getClass().getClassLoader();
        ABImpl ab = new ABImpl();
        Object abProxy = IASProxyProxy.newProxyInstance(cl, new Class[] { A.class, B.class },
                (proxy, method, methodArgs) -> {
                    return method.invoke(ab, methodArgs);
                }
        );
        A a = (A)abProxy;
        B b = (B)abProxy;
        assertEquals(ab.getA(), a.getA());
        assertEquals(ab.getB(), b.getB());
    }

}

package com.sap.fontus.taintaware.shared;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;


public class IASTaintHandlerTest {
    @BeforeAll
    static void before() {
        IASTaintSourceRegistry.getInstance().getOrRegisterTaintSource("mySource");
        Configuration.setTestConfig(TaintMethod.BOOLEAN);
        Configuration.getConfiguration().setRecursiveTainting(true);
    }

    @Test
    public void testRecursiveTaint() {
        IASString string = new IASString("test");
        A a = new A(string);

        IASTaintHandler.taint(a, 1);

        Assertions.assertTrue(string.isTainted());
    }

    static class A {
        IASString string;
        A a;

        public A(IASString string) {
            this.string = string;
            this.a = this;
        }
    }
}

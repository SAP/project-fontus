package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.taintaware.bool.IASString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;


public class IASTaintHandlerTest {
    @BeforeAll
    static void before() {
        IASTaintSourceRegistry.getInstance().getOrRegisterTaintSource("mySource");
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

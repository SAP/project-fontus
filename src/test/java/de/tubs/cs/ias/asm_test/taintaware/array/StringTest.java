package de.tubs.cs.ias.asm_test.taintaware.array;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringTest {
    @Test
    public void constructorTest1() {
        String s = "Hello World";

        IASString ts = new IASString(s);

        assertEquals("Hello World", ts.getString());
        assertFalse(ts.isTainted());
    }

    @Test
    public void constructorTest2() {
        String s = "Hello World";

        IASString ts = IASString.fromString(s);

        assertEquals("Hello World", ts.getString());
        assertFalse(ts.isTainted());
    }
}

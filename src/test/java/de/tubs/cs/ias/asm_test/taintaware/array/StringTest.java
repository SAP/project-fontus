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

    @Test
    public void constructorTest3() {
        IASString s = new IASString("Hello World");
        assertTrue(s.isUninitialized());

        IASString ts = new IASString(s);

        assertEquals("Hello World", ts.getString());
        assertTrue(ts.isUninitialized());
        assertFalse(ts.isTainted());
    }

    @Test
    public void constructorTest4() {
        IASStringBuilder s = new IASStringBuilder("Hello World");
        assertTrue(s.isUninitialized());

        IASString ts = new IASString(s);

        assertEquals("Hello World", ts.getString());
        assertTrue(ts.isUninitialized());
        assertFalse(ts.isTainted());
    }

    @Test
    public void constructorTest5() {
        IASStringBuffer s = new IASStringBuffer("Hello World");
        assertTrue(s.isUninitialized());

        IASString ts = new IASString(s);

        assertEquals("Hello World", ts.getString());
        assertTrue(ts.isUninitialized());
        assertFalse(ts.isTainted());
    }
}

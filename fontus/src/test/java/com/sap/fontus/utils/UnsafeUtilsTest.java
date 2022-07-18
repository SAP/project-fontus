package com.sap.fontus.utils;

import org.junit.jupiter.api.Test;

import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import static org.junit.jupiter.api.Assertions.*;
class UnsafeUtilsTest {

    @Test
    void accessTest() throws Exception {
        URL u = new URL("https:/foo.bar");
        Method protocol = URL.class.getDeclaredMethod("writeObject", ObjectOutputStream.class);
        assertFalse(protocol.canAccess(u));
        UnsafeUtils.setAccessible(protocol);
        assertTrue(protocol.canAccess(u));
    }
}

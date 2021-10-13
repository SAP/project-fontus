package com.sap.fontus.taintaware.unified.reflection;

import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.reflect.IASClassProxy;
import com.sap.fontus.taintaware.unified.reflect.IASMethod;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FilenameFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IASClassProxyTest {
    @Test
    public void testReflectionOnJdkInterface() throws NoSuchMethodException {
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
}

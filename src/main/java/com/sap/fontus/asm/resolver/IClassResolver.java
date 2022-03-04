package com.sap.fontus.asm.resolver;

import java.io.InputStream;

public interface IClassResolver {
    InputStream resolve(String className);
}

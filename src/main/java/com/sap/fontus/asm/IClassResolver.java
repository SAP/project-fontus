package com.sap.fontus.asm;

import java.io.InputStream;

public interface IClassResolver {
    InputStream resolve(String className);
}

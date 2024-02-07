package com.sap.fontus.asm.resolver;

import java.util.Optional;

public interface IClassResolver {
    Optional<byte[]> resolve(String className);
}

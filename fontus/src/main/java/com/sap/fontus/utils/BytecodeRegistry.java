package com.sap.fontus.utils;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BytecodeRegistry {
    private final static BytecodeRegistry INSTANCE = new BytecodeRegistry();
    private final Map<String, byte[]> bytecodeCache;

    private BytecodeRegistry() {
        this.bytecodeCache = new ConcurrentHashMap<>();
    }

    public static BytecodeRegistry getInstance() {
        return INSTANCE;
    }

    public void addClassData(String internalName, byte[] data) {
        Objects.requireNonNull(internalName);
        Objects.requireNonNull(data);

        internalName = Utils.dotToSlash(internalName);

        bytecodeCache.putIfAbsent(internalName, data);
    }

    public Optional<byte[]> getClassData(String internalName) {
        if (this.bytecodeCache.containsKey(internalName)) {
            return Optional.of(this.bytecodeCache.get(internalName));
        }
        return Optional.empty();
    }

    public Map<String, byte[]> getModifiableBytecodeCache() {
        return bytecodeCache;
    }

    public Map<String, byte[]> getUnmodifiableBytecodeCache() {
        return Collections.unmodifiableMap(bytecodeCache);
    }

}

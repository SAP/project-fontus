package com.sap.fontus.taintaware.unified;

import java.util.*;

public class ProcessBuilderEnvironmentProxy implements Map<IASString, IASString> {

    private final Map<String, String> backend;

    ProcessBuilderEnvironmentProxy(Map<String, String> env) {
        this.backend = env;
    }

    @Override
    public int size() {
        return this.backend.size();
    }

    @Override
    public boolean isEmpty() {
        return this.backend.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.backend.containsKey(IASStringUtils.convertTObject(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return this.backend.containsValue(IASStringUtils.convertTObject(value));
    }

    @Override
    public IASString get(Object key) {
        return IASString.fromString(this.backend.get(IASStringUtils.convertTObject(key)));
    }

    @Override
    public IASString put(IASString key, IASString value) {
        return IASString.fromString(this.backend.put(key.getString(), value.getString()));
    }

    @Override
    public IASString remove(Object key) {
        return IASString.fromString(this.backend.remove(IASStringUtils.convertTObject(key)));
    }

    @Override
    public void putAll(Map<? extends IASString, ? extends IASString> m) {
        for(Map.Entry<? extends IASString, ? extends IASString> e : m.entrySet()) {
            this.backend.put(e.getKey().getString(), e.getValue().getString());
        }
    }

    @Override
    public void clear() {
        this.backend.clear();
    }

    @Override
    public Set<IASString> keySet() {
        Set<String> backendKeys = this.backend.keySet();
        Set<IASString> keys = new HashSet<>(backendKeys.size());
        for(String key : backendKeys) {
            keys.add(IASString.fromString(key));
        }
        return keys;
    }

    @Override
    public Collection<IASString> values() {
        Collection<String> backendValues = this.backend.values();
        Collection<IASString> values = new ArrayList<>(backendValues.size());
        for(String value : backendValues) {
            values.add(IASString.fromString(value));
        }
        return values;
    }

    @Override
    public Set<Map.Entry<IASString, IASString>> entrySet() {
        Map<IASString, IASString> temp = new HashMap<>(this.backend.size());
        for(Map.Entry<String, String> e : this.backend.entrySet()) {
            temp.put(IASString.fromString(e.getKey()), IASString.fromString(e.getValue()));
        }
        return temp.entrySet();
    }

    public static Map<IASString, IASString> getProcessBuilderEnv(ProcessBuilder pb) {
        return new ProcessBuilderEnvironmentProxy(pb.environment());
    }
}

package com.sap.fontus.taintaware.bool;

import com.sap.fontus.taintaware.shared.IASStringable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class IASProperties extends com.sap.fontus.taintaware.shared.IASProperties {
    public IASProperties() {
        super();
    }

    public IASProperties(int initialCapacity) {
        super(initialCapacity);
    }

    public IASProperties(Properties defaults) {
        super(defaults);
    }

    public IASProperties(com.sap.fontus.taintaware.shared.IASProperties defaults) {
        super(defaults);
    }

    public IASProperties(IASProperties defaults) {
        super(defaults);
    }

    public static IASProperties fromProperties(Properties properties) {
        return new IASProperties(properties);
    }

    @Override
    public synchronized Object setProperty(IASStringable key, IASStringable value) {
        return super.setProperty(key, value);
    }

    @Override
    public synchronized void load(Reader reader) throws IOException {
        super.load(reader);
    }

    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        super.load(inStream);
    }

    @Override
    public void save(OutputStream out, IASStringable comments) {
        super.save(out, comments);
    }

    @Override
    public void store(Writer writer, IASStringable comments) throws IOException {
        super.store(writer, comments);
    }

    @Override
    public void store(OutputStream out, IASStringable comments) throws IOException {
        super.store(out, comments);
    }

    @Override
    public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
        super.loadFromXML(in);
    }

    @Override
    public void storeToXML(OutputStream os, IASStringable comment) throws IOException {
        super.storeToXML(os, comment);
    }

    @Override
    public void storeToXML(OutputStream os, IASStringable comment, IASStringable encoding) throws IOException {
        super.storeToXML(os, comment, encoding);
    }

    @Override
    public void storeToXML(OutputStream os, IASStringable comment, Charset charset) throws IOException {
        super.storeToXML(os, comment, charset);
    }

    @Override
    public IASString getProperty(IASStringable key) {
        return (IASString) super.getProperty(key);
    }

    @Override
    public IASString getProperty(IASStringable key, IASStringable defaultValue) {
        return (IASString) super.getProperty(key, defaultValue);
    }

    @Override
    public Enumeration<?> propertyNames() {
        return super.propertyNames();
    }

    @Override
    public Set<IASStringable> stringPropertyNames() {
        return super.stringPropertyNames();
    }

    @Override
    public void list(PrintStream out) {
        super.list(out);
    }

    @Override
    public void list(PrintWriter out) {
        super.list(out);
    }

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public Enumeration<Object> keys() {
        return super.keys();
    }

    @Override
    public Enumeration<Object> elements() {
        return super.elements();
    }

    @Override
    public boolean contains(Object value) {
        return super.contains(value);
    }

    @Override
    public boolean containsValue(Object value) {
        return super.containsValue(value);
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    @Override
    public Object get(Object key) {
        return super.get(key);
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        return super.put(key, value);
    }

    @Override
    public synchronized Object remove(Object key) {
        return super.remove(key);
    }

    @Override
    public synchronized void putAll(Map<?, ?> t) {
        super.putAll(t);
    }

    @Override
    public synchronized void clear() {
        super.clear();
    }

    @Override
    public synchronized IASString toIASString() {
        return (IASString) super.toIASString();
    }

    @Override
    public synchronized String toString() {
        return super.toString();
    }

    @Override
    public Set<Object> keySet() {
        return super.keySet();
    }

    @Override
    public Collection<Object> values() {
        return super.values();
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        return super.entrySet();
    }

    @Override
    public synchronized boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public synchronized int hashCode() {
        return super.hashCode();
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        return super.getOrDefault(key, defaultValue);
    }

    @Override
    public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
        super.forEach(action);
    }

    @Override
    public synchronized void replaceAll(BiFunction<? super Object, ? super Object, ?> function) {
        super.replaceAll(function);
    }

    @Override
    public synchronized Object putIfAbsent(Object key, Object value) {
        return super.putIfAbsent(key, value);
    }

    @Override
    public synchronized boolean remove(Object key, Object value) {
        return super.remove(key, value);
    }

    @Override
    public synchronized boolean replace(Object key, Object oldValue, Object newValue) {
        return super.replace(key, oldValue, newValue);
    }

    @Override
    public synchronized Object replace(Object key, Object value) {
        return super.replace(key, value);
    }

    @Override
    public synchronized Object computeIfAbsent(Object key, Function<? super Object, ?> mappingFunction) {
        return super.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public synchronized Object computeIfPresent(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return super.computeIfPresent(key, remappingFunction);
    }

    @Override
    public synchronized Object compute(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return super.compute(key, remappingFunction);
    }

    @Override
    public synchronized Object merge(Object key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return super.merge(key, value, remappingFunction);
    }

    @Override
    protected void rehash() {
        super.rehash();
    }

    @Override
    public synchronized Object clone() {
        return super.clone();
    }
}

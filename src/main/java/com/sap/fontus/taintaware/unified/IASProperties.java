package com.sap.fontus.taintaware.unified;


import com.sap.fontus.utils.ConversionUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IASProperties extends Hashtable<Object, Object> {
    /**
     * This is the single source of truth
     */
    private final Properties properties;

    private final Map<Object, IASString> shadow = new ConcurrentHashMap<>();

    public IASProperties() {
        this.properties = new Properties();
    }

    public IASProperties(int initialCapacity) {
        this.properties = new Properties(initialCapacity);
    }

    public IASProperties(Properties properties) {
        this.properties = properties;
    }

    public IASProperties(IASProperties defaults) {
        this.properties = new Properties(defaults.properties);
    }

    public synchronized Object setProperty(IASString key, IASString value) {
        Object previousString = this.properties.setProperty(key.getString(), value.getString());
        if (previousString instanceof String) {
            return IASString.fromString((String) previousString);
        }
        return previousString;
    }

    public synchronized void load(Reader reader) throws IOException {
        this.properties.load(reader);
    }

    public synchronized void load(InputStream inStream) throws IOException {
        this.properties.load(inStream);
    }

    public void save(OutputStream out, IASString comments) {
        this.properties.save(out, comments.getString());
    }

    public void store(Writer writer, IASString comments) throws IOException {
        this.properties.store(writer, comments.getString());
    }

    public void store(OutputStream out, IASString comments) throws IOException {
        this.properties.store(out, comments.getString());
    }

    public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
        this.properties.loadFromXML(in);
    }

    public void storeToXML(OutputStream os, IASString comment) throws IOException {
        this.properties.storeToXML(os, comment.getString());
    }

    public void storeToXML(OutputStream os, IASString comment, IASString encoding) throws IOException {
        this.properties.storeToXML(os, comment.getString(), encoding.getString());
    }

    @SuppressWarnings("Since15")
    public void storeToXML(OutputStream os, IASString comment, Charset charset) throws IOException {
        this.properties.storeToXML(os, comment.getString(), charset);
    }

    public IASString getProperty(IASString key) {
        Object orig = ConversionUtils.convertToConcrete(this.properties.getProperty(key.getString()));
        IASString taintaware = this.shadow.get(key);
        return (IASString) chooseReturn(orig, taintaware);
    }

    public IASString getProperty(IASString key, IASString defaultValue) {
        String defaultStringValue = defaultValue != null ? defaultValue.getString() : null;
        Object orig = ConversionUtils.convertToConcrete(this.properties.getProperty(key.getString(), defaultStringValue));
        IASString taintaware = this.shadow.get(ConversionUtils.convertToConcrete(key));
        return (IASString) chooseReturn(orig, taintaware);
    }

    public Enumeration<?> propertyNames() {
        return Collections.enumeration(
                Collections
                        .list(this.properties.propertyNames())
                        .stream()
                        .map(ConversionUtils::convertToConcrete)
                        .collect(Collectors.toList())
        );
    }

    public Set<IASString> stringPropertyNames() {
        return this.properties.stringPropertyNames()
                .stream()
                .map(IASString::valueOf)
                .collect(Collectors.toSet());
    }

    public void list(PrintStream out) {
        this.properties.list(out);
    }

    public void list(PrintWriter out) {
        this.properties.list(out);
    }

    public int size() {
        return this.properties.size();
    }

    public boolean isEmpty() {
        return this.properties.isEmpty();
    }

    public Enumeration<Object> keys() {
        return Collections.enumeration(
                Collections
                        .list(this.properties.keys())
                        .stream()
                        .map(ConversionUtils::convertToConcrete)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Enumeration<Object> elements() {
        return Collections.enumeration(
                Collections
                        .list(this.properties.elements())
                        .stream()
                        .map(ConversionUtils::convertToConcrete)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public boolean contains(Object value) {
        return this.properties.contains(value.toString());
    }

    @Override
    public boolean containsValue(Object value) {
        return this.properties.containsValue(value.toString());
    }

    @Override
    public boolean containsKey(Object key) {
        return this.properties.containsKey(key.toString());
    }

    @Override
    public Object get(Object key) {
        Object orig = ConversionUtils.convertToConcrete(this.properties.get(ConversionUtils.convertToOrig(key)));
        IASString taintaware = this.shadow.get(ConversionUtils.convertToConcrete(key));
        return this.chooseReturn(orig, taintaware);
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        Object orig = ConversionUtils.convertToConcrete(this.properties.put(ConversionUtils.convertToOrig(key), ConversionUtils.convertToOrig(value)));
        IASString taintaware = null;
        if (value instanceof IASString) {
            taintaware = this.shadow.put(ConversionUtils.convertToConcrete(key), (IASString) value);
        }
        return chooseReturn(orig, taintaware);
    }

    private Object chooseReturn(Object orig, IASString taintaware) {
        if (Objects.equals(orig, taintaware)) {
            return taintaware;
        }
        return orig;
    }

    @Override
    public synchronized Object remove(Object key) {
        return ConversionUtils.convertToConcrete(this.properties.remove(ConversionUtils.convertToOrig(key)));
    }

    @Override
    public synchronized void putAll(Map<?, ?> t) {
        for (Map.Entry<?, ?> entry : t.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public synchronized void clear() {
        this.properties.clear();
        super.clear();
    }

    public synchronized IASString toIASString() {
        return IASString.fromString(this.toString());
    }

    @Override
    public synchronized String toString() {
        return this.properties.toString();
    }

    @Override
    public Set<Object> keySet() {
        return this.properties
                .keySet()
                .stream()
                .map(ConversionUtils::convertToConcrete)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<Object> values() {
        return this.properties
                .values()
                .stream()
                .map(ConversionUtils::convertToConcrete)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        return this.properties
                .entrySet()
                .stream()
                .map((entry) -> new Map.Entry<Object, Object>() {
                    @Override
                    public Object getKey() {
                        return ConversionUtils.convertToConcrete(entry.getKey());
                    }

                    @Override
                    public Object getValue() {
                        return ConversionUtils.convertToConcrete(entry.getValue());
                    }

                    @Override
                    public Object setValue(Object value) {
                        return ConversionUtils.convertToConcrete(IASProperties.this.put(ConversionUtils.convertToOrig(entry.getKey()), ConversionUtils.convertToOrig(value)));
                    }
                })
                .collect(Collectors.toSet());
    }

    @Override
    public synchronized boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o instanceof Properties) {
            return this.properties.equals(o);
        } else if (o instanceof IASProperties) {
            return this.properties.equals(((IASProperties) o).properties);
        }
        return false;
    }

    @Override
    public synchronized int hashCode() {
        return this.properties.hashCode();
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        if (this.properties.containsKey(ConversionUtils.convertToOrig(key))) {
            return this.get(ConversionUtils.convertToOrig(key));
        }
        return ConversionUtils.convertToConcrete(defaultValue);
    }



    @Override
    public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
        this.properties.forEach((o, o2) -> action.accept(ConversionUtils.convertToOrig(o), ConversionUtils.convertToOrig(o2)));
    }

    @Override
    public synchronized void replaceAll(BiFunction<? super Object, ? super Object, ?> function) {
        this.properties.replaceAll((o, o2) -> ConversionUtils.convertToConcrete(function.apply(ConversionUtils.convertToOrig(o), ConversionUtils.convertToOrig(o2))));
    }

    @Override
    public synchronized Object putIfAbsent(Object key, Object value) {
        return ConversionUtils.convertToConcrete(this.properties.putIfAbsent(ConversionUtils.convertToOrig(key), ConversionUtils.convertToOrig(value)));
    }

    @Override
    public synchronized boolean remove(Object key, Object value) {
        return this.properties.remove(ConversionUtils.convertToOrig(key), ConversionUtils.convertToOrig(value));
    }

    @Override
    public synchronized boolean replace(Object key, Object oldValue, Object newValue) {
        return this.properties.replace(ConversionUtils.convertToOrig(key), ConversionUtils.convertToOrig(oldValue), ConversionUtils.convertToOrig(newValue));
    }

    @Override
    public synchronized Object replace(Object key, Object value) {
        return this.properties.replace(ConversionUtils.convertToOrig(key), ConversionUtils.convertToOrig(value));
    }

    @Override
    public synchronized Object computeIfAbsent(Object key, Function<? super Object, ?> mappingFunction) {
        return ConversionUtils.convertToConcrete(this.properties.computeIfAbsent(ConversionUtils.convertToOrig(key), o -> ConversionUtils.convertToOrig(mappingFunction.apply(ConversionUtils.convertToConcrete(o)))));
    }

    @Override
    public synchronized Object computeIfPresent(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return ConversionUtils.convertToConcrete(
                this.properties.computeIfPresent(ConversionUtils.convertToOrig(key), (o, o2) -> ConversionUtils.convertToOrig(remappingFunction.apply(ConversionUtils.convertToConcrete(o), ConversionUtils.convertToConcrete(o2))))
        );
    }

    @Override
    public synchronized Object compute(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return ConversionUtils.convertToConcrete(
                this.properties.compute(ConversionUtils.convertToOrig(key), (o, o2) -> ConversionUtils.convertToOrig(remappingFunction.apply(ConversionUtils.convertToConcrete(o), ConversionUtils.convertToConcrete(o2))))
        );
    }

    @Override
    public synchronized Object merge(Object key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return ConversionUtils.convertToConcrete(
                this.properties.merge(
                        ConversionUtils.convertToOrig(key),
                        ConversionUtils.convertToOrig(value),
                        (o, o2) ->
                                ConversionUtils.convertToOrig(
                                        remappingFunction.apply(
                                                ConversionUtils.convertToConcrete(o),
                                                ConversionUtils.convertToConcrete(o2)
                                        )
                                )
                )
        );
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public synchronized Object clone() {
        return IASProperties.fromProperties((Properties) this.properties.clone());
    }

    private static IASProperties fromProperties(Properties clone) {
        return new IASProperties(clone);
    }

    public Properties getProperties() {
        return properties;
    }
}

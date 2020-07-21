package de.tubs.cs.ias.asm_test.taintaware.shared;


import de.tubs.cs.ias.asm_test.config.Configuration;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class IASProperties extends Hashtable<Object, Object> {
    private static final IASFactory factory = Configuration.getConfiguration().getTaintMethod().getFactory();
    /**
     * This is the single source of truth
     */
    private Properties properties;

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

    public synchronized Object setProperty(IASStringable key, IASStringable value) {
        String previousString = (String) this.properties.setProperty(key.getString(), value.getString());
        IASStringable previousTaintedString = (IASStringable) super.put(key, value);
        if (previousString == null) {
            return null;
        } else {
            if (previousTaintedString == null || !previousTaintedString.getString().equals(previousString)) {
                return factory.createString(previousString);
            } else {
                return previousTaintedString;
            }
        }
    }

    public synchronized void load(Reader reader) throws IOException {
        this.properties.load(reader);
    }

    public synchronized void load(InputStream inStream) throws IOException {
        this.properties.load(inStream);
    }

    public void save(OutputStream out, String comments) {
        this.properties.save(out, comments);
    }

    public void store(Writer writer, String comments) throws IOException {
        this.properties.store(writer, comments);
    }

    public void store(OutputStream out, String comments) throws IOException {
        this.properties.store(out, comments);
    }

    public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
        this.properties.loadFromXML(in);
    }

    public void storeToXML(OutputStream os, String comment) throws IOException {
        this.properties.storeToXML(os, comment);
    }

    public void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
        this.properties.storeToXML(os, comment, encoding);
    }

    @SuppressWarnings("Since15")
    public void storeToXML(OutputStream os, String comment, Charset charset) throws IOException {
        this.properties.storeToXML(os, comment, charset);
    }

    public IASStringable getProperty(IASStringable key) {
        String property = this.properties.getProperty(key.getString());
        IASStringable propertyTainted = (IASStringable) super.get(key);
        return this.chooseReturn(property, propertyTainted);
    }

    private IASStringable chooseReturn(String string, IASStringable taintedString) {
        if (string == null) {
            return null;
        } else {
            if (taintedString == null || !taintedString.getString().equals(string)) {
                return factory.createString(string);
            } else {
                return taintedString;
            }
        }
    }

    public IASStringable getProperty(IASStringable key, IASStringable defaultValue) {
        String property = this.properties.getProperty(key.getString());
        IASStringable propertyTainted = (IASStringable) super.get(key);
        return this.chooseReturnWithDefault(property, propertyTainted, defaultValue);
    }

    private IASStringable chooseReturnWithDefault(String string, IASStringable taintedString, IASStringable defaultTainted) {
        if (string == null) {
            return defaultTainted;
        } else {
            if (taintedString == null || !taintedString.getString().equals(string)) {
                return factory.createString(string);
            } else {
                return taintedString;
            }
        }
    }

    public Enumeration<?> propertyNames() {
        // TODO Get name from super if possible
        return Collections.enumeration(
                Collections
                        .list(this.properties.propertyNames())
                        .stream()
                        .map(factory::valueOf)
                        .collect(Collectors.toList())
        );
    }

    public Set<IASStringable> stringPropertyNames() {
        return this.properties.stringPropertyNames()
                .stream()
                .map(factory::valueOf)
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
        // TODO Get name from super if possible
        return Collections.enumeration(
                Collections
                        .list(this.properties.keys())
                        .stream()
                        .map((obj) -> factory.createString((String) obj))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Enumeration<Object> elements() {
        // TODO Get name from super if possible
        return Collections.enumeration(
                Collections
                        .list(this.properties.elements())
                        .stream()
                        .map((obj) -> factory.createString((String) obj))
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
        return this.getProperty((IASStringable) key);
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        return this.setProperty((IASStringable) key, (IASStringable) value);
    }

    @Override
    public synchronized Object remove(Object key) {
        String previous = (String) this.properties.remove(key.toString());
        IASStringable previousTainted = (IASStringable) super.remove(key);
        return this.chooseReturn(previous, previousTainted);
    }

    @Override
    public synchronized void putAll(Map<?, ?> t) {
        for (Map.Entry<?, ?> entry : t.entrySet()) {
            IASStringable key = factory.valueOf(entry.getKey());
            IASStringable value = factory.valueOf(entry.getValue());
            this.setProperty(key, value);
        }
    }

    @Override
    public synchronized void clear() {
        this.properties.clear();
        super.clear();
    }

    public synchronized IASStringable toIASString() {
        // TODO taintaware version
        return factory.createString(this.toString());
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
                .map((obj) -> factory.valueOf(obj))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<Object> values() {
        return this.properties
                .values()
                .stream()
                .map((obj) -> factory.valueOf(obj))
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
                        return factory.valueOf(entry.getKey());
                    }

                    @Override
                    public Object getValue() {
                        return factory.valueOf(entry.getValue());
                    }

                    @Override
                    public Object setValue(Object value) {
                        return setProperty((IASStringable) getKey(), factory.valueOf(value));
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
        return this.getProperty((IASStringable) key, (IASStringable) defaultValue);
    }

    @SuppressWarnings("Java8MapForEach")
    @Override
    public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
        this.entrySet().forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    @Override
    public synchronized void replaceAll(BiFunction<? super Object, ? super Object, ?> function) {
        this.properties.replaceAll((o, o2) -> function.apply(factory.valueOf(o), factory.valueOf(o2)).toString());
        super.replaceAll(function);
    }

    @Override
    public synchronized Object putIfAbsent(Object key, Object value) {
        if (!this.properties.contains(key.toString())) {
            String string = (String) this.properties.put(key.toString(), value.toString());
            IASStringable taintedString = (IASStringable) super.put((IASStringable) key, (IASStringable) value);
            return this.chooseReturn(string, taintedString);
        }
        return null;
    }

    @Override
    public synchronized boolean remove(Object key, Object value) {
        boolean success = this.properties.remove(key.toString(), value.toString());
        super.remove((IASStringable) key, (IASStringable) value);
        return success;
    }

    @Override
    public synchronized boolean replace(Object key, Object oldValue, Object newValue) {
        boolean success = this.properties.replace(key.toString(), oldValue.toString(), newValue.toString());
        super.replace((IASStringable) key, (IASStringable) oldValue, (IASStringable) newValue);
        return success;
    }

    @Override
    public synchronized Object replace(Object key, Object value) {
        String string = (String) this.properties.replace(key.toString(), value.toString());
        IASStringable taintedString = (IASStringable) super.replace((IASStringable) key, (IASStringable) value);
        return this.chooseReturn(string, taintedString);
    }

    @Override
    public synchronized Object computeIfAbsent(Object key, Function<? super Object, ?> mappingFunction) {
        if (!this.properties.containsKey(key.toString())) {
            IASStringable result = (IASStringable) mappingFunction.apply(key);
            if (result != null) {
                return this.setProperty((IASStringable) key, result);
            }
            return null;
        }
        return this.getProperty((IASStringable) key);
    }

    @Override
    public synchronized Object computeIfPresent(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        if (this.properties.containsKey(key.toString()) && this.properties.get(key) != null) {
            IASStringable result = (IASStringable) remappingFunction.apply(key, this.get(key));
            if (result != null) {
                this.setProperty((IASStringable) key, result);
            } else {
                this.remove(key);
            }
            return result;
        }
        return null;
    }

    @Override
    public synchronized Object compute(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        IASStringable result = (IASStringable) remappingFunction.apply(key, this.get(key));
        if (result != null) {
            this.setProperty((IASStringable) key, result);
        } else {
            this.remove(key);
        }
        return result;
    }

    @Override
    public synchronized Object merge(Object key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        if (!this.properties.containsKey(key.toString()) || this.get(key) == null) {
            this.setProperty((IASStringable) key, (IASStringable) value);
            return value;
        } else {
            return this.compute(key, remappingFunction);
        }
    }

    @Override
    protected void rehash() {
        super.rehash();
    }

    @Override
    public synchronized Object clone() {
        IASProperties properties = (IASProperties) super.clone();
        properties.properties = (Properties) this.properties.clone();
        return properties;
    }

    public Properties getProperties() {
        return properties;
    }
}

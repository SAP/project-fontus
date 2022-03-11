package com.sap.fontus.utils;

import java.util.ArrayList;
import java.util.List;

public abstract class GenericRegistry<T extends NamedObject> {

    protected final List<T> objects = new ArrayList<>();
    protected int counter = 0;

    protected abstract T getNewObject(String name, int id);

    public synchronized T getOrRegisterObject(String name) {
        T o = this.get(name);
        if (o == null) {
            this.counter++;
            o = getNewObject(name, this.counter);
            this.objects.add(o);
        }
        return o;
    }

    public synchronized T get(String name) {
        for (T o : objects) {
            if (o.getName().equals(name)) {
                return o;
            }
        }
        return null;
    }

    public synchronized T get(int id) {
        if (id <= 0 || id > this.counter) {
            return null;
        }
        return this.objects.get(id - 1);
    }

    public int getIdForObject(T obj) {
        return this.objects.indexOf(obj);
    }

    public synchronized void clear() {
        this.objects.clear();
        this.counter = 0;
    }

}


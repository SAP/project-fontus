package com.sap.fontus.utils;

import java.util.ArrayList;
import java.util.List;

public abstract class GenericRegistry<T extends NamedObject> {

    protected final List<T> objects = new ArrayList<>();
    protected int counter = 0;

    protected abstract T getNewObject(String name, int id);

    public synchronized T getOrRegisterTaintSource(String name) {
        for (T o : objects) {
            if (o.getName().equals(name)) {
                return o;
            }
        }
        this.counter++;
        T o = getNewObject(name, this.counter);
        this.objects.add(o);
        return o;
    }

    public synchronized T get(int id) {
        if (id <= 0 || id > this.counter) {
            return null;
        }
        return this.objects.get(id - 1);
    }

    public synchronized void clear() {
        this.objects.clear();
        this.counter = 0;
    }

}


package com.sap.fontus.gdpr.metadata;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class RequiredPurposeSet implements RequiredPurposes {

    private Set<RequiredPurpose> set;

    public RequiredPurposeSet() {
        this.set = new HashSet<>();
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<RequiredPurpose> iterator() {
        return set.iterator();
    }

    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return toArray(a);
    }

    @Override
    public boolean add(RequiredPurpose requiredPurpose) {
        return set.add(requiredPurpose);
    }

    @Override
    public boolean remove(Object o) {
        return set.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends RequiredPurpose> c) {
        return set.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return set.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return set.retainAll(c);
    }

    @Override
    public void clear() {
        set.clear();
    }
}

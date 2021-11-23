package com.sap.fontus.gdpr.metadata;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class AllowedPurposeSet implements AllowedPurposes {

    private Set<AllowedPurpose> set;

    public AllowedPurposeSet() {
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
    public Iterator<AllowedPurpose> iterator() {
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
    public boolean add(AllowedPurpose allowedPurpose) {
        return set.add(allowedPurpose);
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
    public boolean addAll(Collection<? extends AllowedPurpose> c) {
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

package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.DataSubject;

import java.util.Objects;

public class SimpleDataSubject implements DataSubject {

    private String id;

    public SimpleDataSubject() {
        this.id = "DEFAULT";
    }

    public SimpleDataSubject(String id) {
        this.id = id;
    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleDataSubject that = (SimpleDataSubject) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SimpleDataSubject{" +
                "id='" + id + '\'' +
                '}';
    }
}

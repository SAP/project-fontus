package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.DataSubject;

import java.io.Serializable;
import java.util.Objects;

public class SimpleDataSubject implements DataSubject, Serializable {

    private final String id;

    public SimpleDataSubject() {
        this.id = "DEFAULT";
    }

    public SimpleDataSubject(String id) {
        this.id = id;
    }

    public SimpleDataSubject(DataSubject subject) {
        this.id = subject.getIdentifier();
    }

    @Override
    public String getIdentifier() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        SimpleDataSubject that = (SimpleDataSubject) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "SimpleDataSubject{" +
                "id='" + this.id + '\'' +
                '}';
    }
}

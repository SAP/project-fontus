package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.Vendor;

import java.util.Objects;

public class SimpleVendor implements Vendor {

    private int id;

    private String name;

    public SimpleVendor() {
        this.id = -1;
        this.name = "DEFAULT";
    }

    public SimpleVendor(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleVendor that = (SimpleVendor) o;
        return id == that.id && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "SimpleVendor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

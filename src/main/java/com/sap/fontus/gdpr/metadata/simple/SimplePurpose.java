package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.Purpose;

import java.util.Objects;

public class SimplePurpose implements Purpose {

    private int id;
    private String name;
    private String description;
    private String legal;

    public SimplePurpose(int id, String name, String description, String legal) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.legal = legal;
    }

    public SimplePurpose(int id, String name) {
        this.id = id;
        this.name = name;
        this.description = new String();
        this.legal = new String();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLegalDescription() {
        return legal;
    }

    public void setDescription(String description) { this.description = description; }

    public void setLegalDescription(String legal) { this.legal = legal; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimplePurpose that = (SimplePurpose) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(legal, that.legal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, legal);
    }

    @Override
    public String toString() {
        return "SimplePurpose{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", legal='" + legal + '\'' +
                '}';
    }
}

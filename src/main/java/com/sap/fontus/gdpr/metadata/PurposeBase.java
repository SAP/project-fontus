package com.sap.fontus.gdpr.metadata;

import java.util.Objects;

public abstract class PurposeBase implements Purpose {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Purpose)) return false;
        Purpose that = (Purpose) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName()) && Objects.equals(getDescription(), that.getDescription()) && Objects.equals(getLegalDescription(), that.getLegalDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription(), getLegalDescription());
    }

    @Override
    public String toString() {
        return "Purpose{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", legal='" + getLegalDescription() + '\'' +
                '}';
    }
}

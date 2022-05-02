package com.sap.fontus.gdpr.metadata;

import java.util.Objects;

public abstract class VendorBase implements Vendor {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vendor)) return false;
        Vendor that = (Vendor) o;
        return getId() == that.getId() && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    @Override
    public String toString() {
        return "Vendor{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                '}';
    }

}

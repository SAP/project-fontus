package com.sap.fontus.gdpr.metadata;

import java.io.Serializable;
import java.util.Objects;

public abstract class VendorBase implements Vendor, Serializable {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vendor)) {
            return false;
        }
        Vendor that = (Vendor) o;
        return this.getId() == that.getId() && Objects.equals(this.getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), this.getName());
    }

    @Override
    public String toString() {
        return "Vendor{" +
                "id=" + this.getId() +
                ", name='" + this.getName() + '\'' +
                '}';
    }

}

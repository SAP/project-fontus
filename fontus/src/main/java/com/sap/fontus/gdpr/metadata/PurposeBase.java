package com.sap.fontus.gdpr.metadata;

import java.io.Serializable;
import java.util.Objects;

public abstract class PurposeBase implements Purpose, Serializable {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Purpose)) {
            return false;
        }
        Purpose that = (Purpose) o;
        return Objects.equals(this.getId(), that.getId()) && Objects.equals(this.getName(), that.getName()) && Objects.equals(this.getDescription(), that.getDescription()) && Objects.equals(this.getLegalDescription(), that.getLegalDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), this.getName(), this.getDescription(), this.getLegalDescription());
    }

    @Override
    public String toString() {
        return "Purpose{" +
                "id='" + this.getId() + '\'' +
                ", name='" + this.getName() + '\'' +
                ", description='" + this.getDescription() + '\'' +
                ", legal='" + this.getLegalDescription() + '\'' +
                '}';
    }
}

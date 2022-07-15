package com.sap.fontus.gdpr.tcf;

import com.sap.fontus.gdpr.metadata.Purpose;

import java.util.Objects;

public class TcfPurpose implements Purpose {

    private final com.iabtcf.extras.gvl.Purpose tcfPurpose;

    public TcfPurpose(com.iabtcf.extras.gvl.Purpose tcfPurpose) {
        this.tcfPurpose = tcfPurpose;
    }

    @Override
    public int getId() {
        return this.tcfPurpose.getId();
    }

    @Override
    public String getName() {
        return this.tcfPurpose.getName();
    }

    @Override
    public String getDescription() {
        return this.tcfPurpose.getDescription();
    }

    @Override
    public String getLegalDescription() {
        return this.tcfPurpose.getDescriptionLegal();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        TcfPurpose that = (TcfPurpose) o;
        return Objects.equals(this.tcfPurpose, that.tcfPurpose);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.tcfPurpose);
    }

    @Override
    public String toString() {
        return "TcfPurpose{" +
                "tcf_purpose=" + this.tcfPurpose +
                '}';
    }
}

package com.sap.fontus.gdpr.metadata.tcf;

import com.sap.fontus.gdpr.metadata.Purpose;

import java.util.Objects;

public class TcfPurpose implements Purpose {

    private com.iabtcf.extras.gvl.Purpose tcf_purpose;

    public TcfPurpose(com.iabtcf.extras.gvl.Purpose tcf_purpose) {
        this.tcf_purpose = tcf_purpose;
    }

    @Override
    public int getId() {
        return tcf_purpose.getId();
    }

    @Override
    public String getName() {
        return tcf_purpose.getName();
    }

    @Override
    public String getDescription() {
        return tcf_purpose.getDescription();
    }

    @Override
    public String getLegalDescription() {
        return tcf_purpose.getDescriptionLegal();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TcfPurpose that = (TcfPurpose) o;
        return Objects.equals(tcf_purpose, that.tcf_purpose);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tcf_purpose);
    }

    @Override
    public String toString() {
        return "TcfPurpose{" +
                "tcf_purpose=" + tcf_purpose +
                '}';
    }
}

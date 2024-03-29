package com.sap.fontus.gdpr.metadata;

import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;

import java.util.Objects;

public class GdprTaintMetadata extends IASBasicMetadata {

    private GdprMetadata metadata;

    public GdprTaintMetadata() {
        super(IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN);
        this.metadata = null;
    }

    public GdprTaintMetadata(IASTaintSource source, GdprMetadata metadata) {
        super(source);
        this.metadata = metadata;
    }

    public GdprTaintMetadata(int sourceId, GdprMetadata metadata) {
        super(sourceId);
        this.metadata = metadata;
    }

    public GdprMetadata getMetadata() {
        return this.metadata;
    }

    public void setMetadata(GdprMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        GdprTaintMetadata that = (GdprTaintMetadata) o;
        return Objects.equals(this.metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.metadata);
    }

    @Override
    public String toString() {
        return "GdprTaintMetadata{" +
                "metadata=" + this.metadata +
                '}';
    }
}

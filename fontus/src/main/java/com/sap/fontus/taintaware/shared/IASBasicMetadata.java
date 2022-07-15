package com.sap.fontus.taintaware.shared;

import java.util.Objects;

public class IASBasicMetadata implements IASTaintMetadata {

    private final IASTaintSource source;

    public IASBasicMetadata() {
        this.source = new IASTaintSource();
    }

    public IASBasicMetadata(IASTaintSource source) {
        this.source = source;
    }

    public IASBasicMetadata(int sourceId) {
        this.source = IASTaintSourceRegistry.getInstance().get(sourceId);
    }

    @Override
    public IASTaintSource getSource() {
        return this.source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        IASBasicMetadata that = (IASBasicMetadata) o;
        return Objects.equals(this.source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.source);
    }

    @Override
    public String toString() {
        return "IASBasicMetadata{" +
                "source=" + this.source +
                '}';
    }
}

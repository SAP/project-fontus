package com.sap.fontus.taintaware.bool;

import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

public enum IASTaintInformation implements IASTaintInformationable {
    UNTAINTED(false), TAINTED(true);

    private final boolean taint;
    private int length;

    IASTaintInformation(boolean taint) {
        this.taint = taint;
        this.length = 0;
    }

    @Override
    public boolean isTainted() {
        return this.taint;
    }

    @Override
    public IASTaintInformationable replaceTaint(int start, int end, IASTaintInformationable taintInformation) {
        return this.mergeTaint(taintInformation);
    }


    public IASTaintMetadata getTaint(int position) {
        return this.taint ? IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN : null;
    }

    @Override
    public IASTaintInformationable setTaint(int index, IASTaintMetadata taint) {
        return IASTaintInformationable.super.setTaint(index, taint);
    }

    @Override
    public IASTaintInformationable setTaint(int start, int end, IASTaintMetadata taint) {
        return this.mergeTaint(taint == null ? UNTAINTED : TAINTED);
    }

    @Override
    public IASTaintInformationable shiftRight(int offset) { return this; }

    @Override
    public IASTaintInformationable deleteWithShift(int start, int end) {
        return this;
    }

    @Override
    public IASTaintInformationable clearTaint(int start, int end) {
        return this;
    }

    @Override
    public IASTaintInformationable insertWithShift(int offset, IASTaintInformationable taintInformation) {
        return this.mergeTaint(taintInformation);
    }

    private IASTaintInformationable mergeTaint(IASTaintInformationable taintInformation) {
        if (this.taint) {
            return this;
        } else {
            return taintInformation;
        }
    }

    @Override
    public IASTaintInformationable copy() {
        return this;
    }

    @Override
    public IASTaintInformationable reversed() {
        return this;
    }

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public IASTaintInformationable resize(int length) {
        this.length = length;
        return this;
    }

    @Override
    public IASTaintInformationable slice(int start, int end) {
        if (start == end) {
            return UNTAINTED;
        }
        return this;
    }

    @Override
    public IASTaintRanges getTaintRanges(int length) {
        if (this.taint) {
            return new IASTaintRanges(length, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN);
        } else {
            return new IASTaintRanges(length);
        }
    }
}

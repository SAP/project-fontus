package com.sap.fontus.taintaware.untainted;

import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintSource;

import java.util.ArrayList;
import java.util.List;

public class IASTaintInformation implements IASTaintInformationable {

    int length;

    public IASTaintInformation(int length) { this.length = length; }

    @Override
    public boolean isTainted() {
        return false;
    }

    @Override
    public IASTaintInformationable deleteWithShift(int start, int end) {
        return this;
    }

    @Override
    public IASTaintInformationable clearTaint(int start, int end) {
        return this;
    }

    @Override
    public IASTaintInformationable replaceTaint(int start, int end, IASTaintInformationable taintInformation) {
        return this;
    }

    @Override
    public IASTaintInformationable shiftRight(int offset) {
        return this;
    }

    @Override
    public IASTaintInformationable insertWithShift(int offset, IASTaintInformationable taintInformation) {
        return this;
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
    public IASTaintMetadata getTaint(int index) {
        return null;
    }

    @Override
    public IASTaintInformationable setTaint(int start, int end, IASTaintMetadata taint) {
        return this;
    }

    @Override
    public IASTaintInformationable resize(int length) {
        this.length = length;
        return this;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public IASTaintInformationable slice(int start, int end) {
        return this;
    }

    @Override
    public IASTaintRanges getTaintRanges(int length) {
        return new IASTaintRanges(length);
    }
}

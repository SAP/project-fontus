package com.sap.fontus.taintaware.range;

import com.sap.fontus.taintaware.shared.*;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import java.lang.annotation.Documented;
import java.util.List;

public class IASTaintInformation implements IASTaintInformationable {
    private final IASTaintRanges ranges;

    public IASTaintInformation(int length) {
        this.ranges = new IASTaintRanges(length);
    }

    public IASTaintInformation(int length, List<IASTaintRange> ranges) {
        this.ranges = new IASTaintRanges(length, ranges);
    }

    public IASTaintInformation(IASTaintRanges ranges) {
        this.ranges = ranges;
    }

    @Override
    public boolean isTainted() {
        return this.ranges.isTainted();
    }

    @Override
    public IASTaintInformationable deleteWithShift(int start, int end) {
        this.ranges.delete(start, end, true);
        return this;
    }

    @Override
    public IASTaintInformationable clearTaint(int start, int end) {
        this.ranges.delete(start, end, false);
        return this;
    }

    @Override
    public IASTaintInformationable replaceTaint(int start, int end, IASTaintInformationable taintInformation) {
        this.ranges.delete(start, end, true);
        this.ranges.insertTaint(start, ((IASTaintInformation) taintInformation).getTaintRanges());
        return this;
    }

    @Override
    public IASTaintInformationable shiftRight(int offset) {
        this.ranges.shiftRight(offset);
        return this;
    }

    @Override
    public IASTaintInformationable insertWithShift(int offset, IASTaintInformationable taintInformation) {
        this.ranges.insertTaint(offset, ((IASTaintInformation) taintInformation).getTaintRanges());
        return this;
    }

    @Override
    public IASTaintInformationable copy() {
        return new IASTaintInformation(this.ranges.copy());
    }

    @Override
    public IASTaintInformationable reversed() {
        this.ranges.reversed();
        return this;
    }

    @Override
    public IASTaintMetadata getTaint(int index) {
        return this.ranges.getTaintFor(index);
    }

    @Override
    public IASTaintInformationable setTaint(int start, int end, IASTaintMetadata taint) {
        this.ranges.setTaint(start, end, taint);
        return this;
    }

    @Override
    public int getLength() {
        return this.ranges.getLength();
    }

    @Override
    public IASTaintInformationable resize(int length) {
        this.ranges.resize(length);
        return this;
    }

    @Override
    public IASTaintInformationable slice(int start, int end) {
        return new IASTaintInformation(this.ranges.slice(start, end));
    }

    public IASTaintRanges getTaintRanges() {
        return this.ranges.copy();
    }

    @Override
    public IASTaintRanges getTaintRanges(int length) {
        return this.getTaintRanges();
    }
}
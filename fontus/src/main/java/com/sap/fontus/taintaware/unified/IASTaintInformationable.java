package com.sap.fontus.taintaware.unified;

import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintRanges;

import java.io.Serializable;

public interface IASTaintInformationable extends Serializable {
    boolean isTainted();

    IASTaintInformationable deleteWithShift(int start, int end);

    IASTaintInformationable clearTaint(int start, int end);

    IASTaintInformationable replaceTaint(int start, int end, IASTaintInformationable taintInformation);

    IASTaintInformationable shiftRight(int offset);

    IASTaintInformationable insertWithShift(int offset, IASTaintInformationable taintInformation);

    IASTaintInformationable copy();

    default IASTaintInformationable addRange(int start, int end, IASTaintMetadata data) {
        return this.setTaint(start, end, data);
    }

    IASTaintInformationable reversed();

    IASTaintMetadata getTaint(int index);

    /**
     * Overwrites the existing taint information at this index
     *
     * @param index index of the taint information to overwrite
     * @param taint taint to set at the position
     */
    default IASTaintInformationable setTaint(int index, IASTaintMetadata taint) {
        return this.setTaint(index, index + 1, taint);
    }

    IASTaintInformationable setTaint(int start, int end, IASTaintMetadata taint);

    int getLength();

    IASTaintInformationable resize(int length);

    IASTaintInformationable slice(int start, int end);

    IASTaintRanges getTaintRanges(int length);
}

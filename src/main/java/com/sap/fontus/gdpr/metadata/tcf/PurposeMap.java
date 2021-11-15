package com.sap.fontus.gdpr.metadata.tcf;

import com.sap.fontus.gdpr.metadata.Purpose;

import java.util.HashMap;
import java.util.Map;

public class PurposeMap {

    private static Map<com.iab.gdpr.Purpose, Purpose> map = new HashMap<>();
    static {
        map.put(com.iab.gdpr.Purpose.STORAGE_AND_ACCESS, Purpose.StorageAndAccess);
        map.put(com.iab.gdpr.Purpose.AD_SELECTION, Purpose.AdSelection);
        map.put(com.iab.gdpr.Purpose.CONTENT_DELIVERY, Purpose.ContentSelection);
        map.put(com.iab.gdpr.Purpose.MEASUREMENT, Purpose.Measurement);
        map.put(com.iab.gdpr.Purpose.UNDEFINED, Purpose.Undefined);
    }

    public static Purpose ConvertFromTcfPurpose(com.iab.gdpr.Purpose p) {
        return map.get(p);
    }
}

package com.sap.fontus.gdpr.tcf;


import com.iabtcf.extras.jackson.Loader;
import com.iabtcf.extras.gvl.Gvl;
import com.sap.fontus.gdpr.metadata.Purpose;

import java.io.IOException;
import java.io.InputStream;


public class VendorList {

    private static Gvl gvl = null;
    // Latest version from here: https://vendor-list.consensu.org/v2/vendor-list.json
    private static String vendorListFile = "vendor-list.json";

    static {
        ClassLoader classLoader = VendorList.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(vendorListFile);

        Loader loader = new Loader();
        try {
            gvl = loader.globalVendorList(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Purpose GetPurposeFromTcfId(int p) {
        if (gvl != null) {
            for (com.iabtcf.extras.gvl.Purpose purpose : gvl.getPurposes()) {
                if (purpose.getId() == p) {
                    return new TcfPurpose(purpose);
                }
            }
        }
        return null;
    }

}

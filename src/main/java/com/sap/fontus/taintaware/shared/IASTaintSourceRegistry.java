package com.sap.fontus.taintaware.shared;

import com.sap.fontus.utils.GenericRegistry;

public class IASTaintSourceRegistry extends GenericRegistry<IASTaintSource> {
    public static final IASTaintSource TS_STRING_CREATED_FROM_CHAR_ARRAY = getInstance().getOrRegisterObject("StringCreatedFromCharArray");
    public static final IASTaintSource TS_CHAR_UNKNOWN_ORIGIN = getInstance().getOrRegisterObject("CharUnknownOrigin");
    public static final IASTaintSource TS_CS_UNKNOWN_ORIGIN = getInstance().getOrRegisterObject("CharSequenceUnknownOrigin");

    public static final IASTaintMetadata MD_STRING_CREATED_FROM_CHAR_ARRAY = new IASBasicMetadata(TS_STRING_CREATED_FROM_CHAR_ARRAY);
    public static final IASTaintMetadata MD_CHAR_UNKNOWN_ORIGIN = new IASBasicMetadata(TS_CHAR_UNKNOWN_ORIGIN);
    public static final IASTaintMetadata MD_CS_UNKNOWN_ORIGIN = new IASBasicMetadata(TS_CS_UNKNOWN_ORIGIN);

    private static IASTaintSourceRegistry instance;

    @Override
    protected synchronized IASTaintSource getNewObject(String name, int id) {
        return new IASTaintSource(name, id);
    }

    public static synchronized IASTaintSourceRegistry getInstance() {
        if (instance == null) {
            instance = new IASTaintSourceRegistry();
        }
        return instance;
    }
}

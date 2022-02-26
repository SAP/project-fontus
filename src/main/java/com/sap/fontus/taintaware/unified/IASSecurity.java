package com.sap.fontus.taintaware.unified;

import java.security.Security;
import java.security.Provider;

public class IASSecurity {

    public static Provider getProvider(IASString name) {
	return Security.getProvider(name.getString());
    }
}

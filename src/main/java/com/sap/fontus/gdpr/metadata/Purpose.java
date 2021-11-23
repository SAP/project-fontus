package com.sap.fontus.gdpr.metadata;

import com.sap.fontus.utils.NamedObject;

public interface Purpose extends NamedObject {

    int getId();

    String getName();

    String getDescription();

    String getLegalDescription();

}

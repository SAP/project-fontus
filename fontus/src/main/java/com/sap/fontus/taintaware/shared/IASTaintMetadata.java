package com.sap.fontus.taintaware.shared;

import java.io.Serializable;

public interface IASTaintMetadata extends Serializable {

    IASTaintSource getSource();

}

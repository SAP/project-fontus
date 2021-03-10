package com.sap.fontus.taintaware.lazycomplex.operations;

import com.sap.fontus.taintaware.lazycomplex.IASString;

public class InsertOperation extends ReplaceOperation {

    public InsertOperation(int index, IASString insertion) {
        super(index, index, insertion);
    }
}

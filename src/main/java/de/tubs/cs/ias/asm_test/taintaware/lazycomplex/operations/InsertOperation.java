package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASString;

public class InsertOperation extends ReplaceOperation {

    public InsertOperation(int index, IASString insertion) {
        super(index, index, insertion);
    }
}

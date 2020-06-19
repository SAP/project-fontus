package de.tubs.cs.ias.asm_test.taintaware.array;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASFactory;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringBuilderable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

public class IASFactoryImpl implements IASFactory {
    @Override
    public IASStringBuilderable createStringBuilder() {
        return new IASStringBuilder();
    }

    @Override
    public IASStringable createString(String s) {
        return new IASString(s);
    }

}

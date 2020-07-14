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

    @Override
    public IASStringable valueOf(Object o) {
        return IASString.valueOf(o);
    }

    @Override
    public IASStringBuilderable createStringBuilder(IASStringable string) {
        return new IASStringBuilder(string);
    }

    @Override
    public Class<? extends IASStringable> getStringClass() {
        return IASString.class;
    }

    @Override
    public Class<? extends IASStringable[]> getStringArrayClass() {
        return IASString[].class;
    }

}

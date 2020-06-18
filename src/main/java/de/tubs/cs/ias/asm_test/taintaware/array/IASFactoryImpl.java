package de.tubs.cs.ias.asm_test.taintaware.array;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASFactory;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringBuilderable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

import java.nio.charset.Charset;

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
    public IASStringable createString(byte[] bytes, Charset encoding) {
        return new IASString(bytes, encoding);
    }
}

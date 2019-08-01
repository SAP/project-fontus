package de.tubs.cs.ias.asm_test;

public final class IASStringUtils {

    public static IASString fromObject(Object obj) {
        if(obj instanceof String) {
            return new IASString((String)obj);
        } else if(obj instanceof IASString) {
            return (IASString) obj;
        } else {
            throw new IllegalArgumentException(String.format("Obj is of type %s, but only String or TString are allowed!", obj.getClass().descriptorString()));
        }
    }

    private IASStringUtils() {

    }
}

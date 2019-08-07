package de.tubs.cs.ias.asm_test;

import java.util.regex.Pattern;

public final class IASStringUtils {
    private static final Pattern CONCAT_PLACEHOLDER = Pattern.compile("\u0001");

    public static IASString fromObject(Object obj) {
        if(obj == null) {
            return null;
        }
        if(obj instanceof String) {
            return new IASString((String)obj);
        } else if(obj instanceof IASString) {
            return (IASString) obj;
        } else {
            throw new IllegalArgumentException(String.format("Obj is of type %s, but only String or TString are allowed!", obj.getClass().getName()));
        }
    }

    public static IASString[] convertStringArray(String[] arr) {
        IASString[] ret = new IASString[arr.length];
        for (int i = 0; i < arr.length; i++) {
            String s = arr[i];
            IASString ts = new IASString(s);
            ret[i] = ts;
        }
        return ret;
    }

    public static String[] convertTaintAwareStringArray(IASString[] arr) {
        String[] ret = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            IASString s = arr[i];
            ret[i] = s.getString();
        }
        return ret;
    }

    public static IASString concat(String format, Object... args) {
        String ret = format;
        boolean taint = false;
        for (int i = args.length - 1; i >= 0; i--) {
            Object a = args[i];
            if (a instanceof IASString) {
                IASString strArg = (IASString) a;
                taint |= strArg.isTainted();
            }
            String arg = a == null ? "null" : a.toString();
            ret = CONCAT_PLACEHOLDER.matcher(ret).replaceFirst(arg);
        }
        return new IASString(ret, taint);

    }
    private IASStringUtils() {

    }
}

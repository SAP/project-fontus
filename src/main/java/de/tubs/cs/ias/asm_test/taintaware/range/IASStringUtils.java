package de.tubs.cs.ias.asm_test.taintaware.range;

import java.util.ArrayList;
import java.util.List;

public final class IASStringUtils {
    private static final IASString CONCAT_PLACEHOLDER = new IASString("\u0001");

    public static void arraycopy(Object src,
             int srcPos,
             Object dest,
             int destPos,
             int length) {
        Object source = src;
        if(src instanceof String[]) {
            String[] strSrc = (String[]) src;
            source = convertStringArray(strSrc);
        }
        System.arraycopy(source, srcPos, dest, destPos, length);
    }

    public static IASString fromObject(Object obj) {
        if(obj == null) {
            return null;
        }
        if(obj instanceof String) {
            return new IASString((String)obj);
        } else if(obj instanceof IASString) {
            return (IASString) obj;
        } else if(obj instanceof IASStringBuilder) {
            IASStringBuilder b = (IASStringBuilder) obj;
            return new IASString(b.toIASString());
        } else if(obj instanceof IASStringBuffer) {
            IASStringBuffer b = (IASStringBuffer) obj;
            return new IASString(b.toIASString());
        } else {

            throw new IllegalArgumentException(String.format("Obj is of type %s, but only String or TString are allowed!", obj.getClass().getName()));
        }
    }

    public static List<IASString> convertStringList(List<String> lst) {
        List<IASString> alst = new ArrayList<>(lst.size());
        for(String s : lst) {
            alst.add(IASString.fromString(s));
        }
        return alst;
    }

    public static List<String> convertTStringList(List<IASString> tlst) {
        List<String> alst = new ArrayList<>(tlst.size());
        for(IASString s : tlst) {
            alst.add(s.getString());
        }
        return alst;
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
        IASString ret = new IASString(format);
        for (int i = args.length - 1; i >= 0; i--) {
            Object a = args[i];
            if (a instanceof IASString) {
                IASString strArg = (IASString) a;
            }
            IASString arg = a == null ? new IASString("null") : IASString.valueOf(a);
            ret = ret.replaceFirst(CONCAT_PLACEHOLDER, arg);
        }
        return ret;

    }

    private IASStringUtils() {

    }
}

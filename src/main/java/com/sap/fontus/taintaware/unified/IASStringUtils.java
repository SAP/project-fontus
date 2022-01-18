package com.sap.fontus.taintaware.unified;

import java.util.*;

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
            return IASString.fromString((String)obj);
        } else if(obj instanceof IASString) {
            return (IASString) obj;
        } else if(obj instanceof IASAbstractStringBuilder) {
            IASAbstractStringBuilder b = (IASAbstractStringBuilder) obj;
            return b.toIASString();
        } else {
            throw new IllegalArgumentException(String.format("Obj is of type %s, but only String or TString are allowed!", obj.getClass().getName()));
        }
    }

    // Check if an Object is a String and convert to IASString
    // TODO: make these conversions use the conversion Utils
    public static Object convertObject(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof String) {
            return IASString.fromString((String)obj);
        } else {
            return obj;
        }
    }

    // Check if an Object is an IASString and convert to String
    // TODO: make these conversions use the conversion Utils
    public static Object convertTObject(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof IASString) {
            return ((IASString) obj).getString();
        } else {
            return obj;
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
        if (arr == null) return null;
         IASString[] ret = new IASString[arr.length];
        for (int i = 0; i < arr.length; i++) {
            String s = arr[i];
            IASString ts = IASString.fromString(s);
            ret[i] = ts;
        }
        return ret;
    }

    public static String[] convertTaintAwareStringArray(IASString[] arr) {
        if (arr == null) return null;
        String[] ret = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            IASString s = arr[i];
            ret[i] = s.getString();
        }
        return ret;
    }

    public static IASString concat(String format, Object... args) {
        IASString ret = IASString.fromString(format);
        for (int i = args.length - 1; i >= 0; i--) {
            Object a = args[i];
            IASString arg = a == null ? IASString.fromString("null") : IASString.valueOf(a);
            arg = IASMatcher.quoteReplacement(arg);
            ret = ret.replaceFirst(CONCAT_PLACEHOLDER, arg);
        }
        return ret;

    }

    public static Map<IASString, IASString> convertStringMapToTStringMap(Map<String, String> tbl) {
        Hashtable<IASString, IASString> result = new Hashtable<>();
        tbl.forEach((key, value) -> result.put(IASString.valueOf(key), IASString.valueOf(value)));
        return result;
    }

    public static Hashtable<IASString, IASString> convertStringHashtableToTStringHashtable(Hashtable<String, String> tbl) {
        Hashtable<IASString, IASString> result = new Hashtable<>();
        tbl.forEach((key, value) -> result.put(IASString.valueOf(key), IASString.valueOf(value)));
        return result;
    }

    public static Hashtable<String, String> convertTStringToTStringHashTable(Hashtable<IASString, IASString> tbl) {
        Hashtable<String, String> result = new Hashtable<>();
        tbl.forEach((key, value) -> result.put(key.getString(), value.getString()));
        return result;
    }

    public static Map<IASString, IASString> getenv() {
        Map<String, String> origEnv = System.getenv();
        Map<IASString, IASString> convertedEnv = convertStringMapToTStringMap(origEnv);
        return Collections.unmodifiableMap(convertedEnv);
    }

    private IASStringUtils() {

    }
}

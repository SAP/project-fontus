package com.sap.fontus.taintaware.shared;


import com.sap.fontus.config.Configuration;

import java.util.*;

public final class IASStringUtils {
    private static final IASFactory factory = Configuration.getConfiguration().getTaintMethod().getFactory();
    private static final IASStringable CONCAT_PLACEHOLDER = factory.createString("\u0001");

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

    public static IASStringable fromObject(Object obj) {
        if(obj == null) {
            return null;
        }
        if(obj instanceof String) {
            return factory.createString((String)obj);
        } else if(obj instanceof IASStringable) {
            return (IASStringable) obj;
        } else if(obj instanceof IASAbstractStringBuilderable) {
            IASAbstractStringBuilderable b = (IASAbstractStringBuilderable) obj;
            return b.toIASString();
        } else {
            throw new IllegalArgumentException(String.format("Obj is of type %s, but only String or TString are allowed!", obj.getClass().getName()));
        }
    }

    public static List<IASStringable> convertStringList(List<String> lst) {
        List<IASStringable> alst = new ArrayList<>(lst.size());
        for(String s : lst) {
            alst.add(factory.createString(s));
        }
        return alst;
    }

    public static List<String> convertTStringList(List<IASStringable> tlst) {
        List<String> alst = new ArrayList<>(tlst.size());
        for(IASStringable s : tlst) {
            alst.add(s.getString());
        }
        return alst;
    }

     public static IASStringable[] convertStringArray(String[] arr) {
        if (arr == null) return null;
         IASStringable[] ret = new IASStringable[arr.length];
        for (int i = 0; i < arr.length; i++) {
            String s = arr[i];
            IASStringable ts = factory.createString(s);
            ret[i] = ts;
        }
        return ret;
    }

    public static String[] convertTaintAwareStringArray(IASStringable[] arr) {
        if (arr == null) return null;
        String[] ret = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            IASStringable s = arr[i];
            ret[i] = s.getString();
        }
        return ret;
    }

    public static IASStringable concat(String format, Object... args) {
        IASStringable ret = factory.createString(format);
        for (int i = args.length - 1; i >= 0; i--) {
            Object a = args[i];
            IASStringable arg = a == null ? factory.createString("null") : factory.valueOf(a);
            arg = factory.quoteReplacement(arg);
            ret = ret.replaceFirst(CONCAT_PLACEHOLDER, arg);
        }
        return ret;

    }

    public static Map<IASStringable, IASStringable> convertStringMapToTStringMap(Map<String, String> tbl) {
        Hashtable<IASStringable, IASStringable> result = new Hashtable<>();
        tbl.forEach((key, value) -> result.put(factory.valueOf(key), factory.valueOf(value)));
        return result;
    }

    public static Hashtable<IASStringable, IASStringable> convertStringHashtableToTStringHashtable(Hashtable<String, String> tbl) {
        Hashtable<IASStringable, IASStringable> result = new Hashtable<>();
        tbl.forEach((key, value) -> result.put(factory.valueOf(key), factory.valueOf(value)));
        return result;
    }

    public static Hashtable<String, String> convertTStringToTStringHashTable(Hashtable<IASStringable, IASStringable> tbl) {
        Hashtable<String, String> result = new Hashtable<>();
        tbl.forEach((key, value) -> result.put(key.getString(), value.getString()));
        return result;
    }

    public static Map<IASStringable, IASStringable> getenv() {
        Map<String, String> origEnv = System.getenv();
        Map<IASStringable, IASStringable> convertedEnv = convertStringMapToTStringMap(origEnv);
        return Collections.unmodifiableMap(convertedEnv);
    }

    private IASStringUtils() {

    }
}

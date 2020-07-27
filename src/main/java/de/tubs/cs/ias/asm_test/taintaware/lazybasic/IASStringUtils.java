package de.tubs.cs.ias.asm_test.taintaware.lazybasic;import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

import java.util.*;

public final class IASStringUtils {

    public static void arraycopy(Object src,
                                 int srcPos,
                                 Object dest,
                                 int destPos,
                                 int length) {
        de.tubs.cs.ias.asm_test.taintaware.shared.IASStringUtils.arraycopy(src, srcPos, dest, destPos, length);
    }

    public static IASString fromObject(Object obj) {
        return (IASString) de.tubs.cs.ias.asm_test.taintaware.shared.IASStringUtils.fromObject(obj);
    }

    public static List<IASString> convertStringList(List<String> lst) {
        List<IASString> alst = new ArrayList<>(lst.size());
        for(String s : lst) {
            alst.add(IASString.fromString(s));
        }
        return alst;
    }

    public static List<String> convertTStringList(List<IASStringable> tlst) {
        return de.tubs.cs.ias.asm_test.taintaware.shared.IASStringUtils.convertTStringList(tlst);
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

    public static String[] convertTaintAwareStringArray(IASStringable[] arr) {
        return de.tubs.cs.ias.asm_test.taintaware.shared.IASStringUtils.convertTaintAwareStringArray(arr);
    }

    public static IASString concat(String format, Object... args) {
        return (IASString) de.tubs.cs.ias.asm_test.taintaware.shared.IASStringUtils.concat(format, args);
    }

    public static Map<IASString, IASString> convertStringMapToTStringMap(Map<String, String> tbl) {
        Hashtable<IASString, IASString> result = new Hashtable<>();
        tbl.forEach((key, value) -> result.put(IASString.valueOf(key), IASString.valueOf(value)));
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


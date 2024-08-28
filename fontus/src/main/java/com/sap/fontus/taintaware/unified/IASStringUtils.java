package com.sap.fontus.taintaware.unified;

import com.sap.fontus.utils.ConversionUtils;

import java.io.BufferedReader;
import java.io.Serializable;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

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

    /**
     * Check if an Object is a String and convert to IASString
     * TODO: make these conversions use the conversion Utils
     */
    public static Object convertObject(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof String) {
            return IASString.fromString((String)obj);
        } else {
            return obj;
        }
    }

    /**
     * Check if an Object is an IASString and convert to String
     * TODO: make these conversions use the conversion Utils
     */
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
        if (arr == null) {
            return null;
        }
         IASString[] ret = new IASString[arr.length];
        for (int i = 0; i < arr.length; i++) {
            String s = arr[i];
            IASString ts = IASString.fromString(s);
            ret[i] = ts;
        }
        return ret;
    }

    public static String[] convertTaintAwareStringArray(IASString[] arr) {
        if (arr == null) {
            return null;
        }
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
            IASString arg = IASString.valueOf(a);
            arg = IASMatcher.quoteReplacement(arg);
            ret = ret.replaceFirst(CONCAT_PLACEHOLDER, arg);
        }
        return ret;

    }

    public static Map<IASString, IASString> convertStringMapToTStringMap(Map<String, String> tbl) {
        Map<IASString, IASString> result = new HashMap<>();
        tbl.forEach((key, value) -> result.put(IASString.valueOfInternal(key), IASString.valueOfInternal(value)));
        return result;
    }

    public static Hashtable<IASString, IASString> convertStringHashtableToTStringHashtable(Hashtable<String, String> tbl) {
        Hashtable<IASString, IASString> result = new Hashtable<>();
        tbl.forEach((key, value) -> result.put(IASString.valueOfInternal(key), IASString.valueOfInternal(value)));
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

    public static <T, U> Comparator<T> comparing(
            Function<? super T, ? extends U> keyExtractor,
            Comparator<? super U> keyComparator)
    {
        Objects.requireNonNull(keyExtractor);
        Objects.requireNonNull(keyComparator);
        if(keyComparator instanceof Collator) {

            return (Comparator<T> & Serializable)
                    (c1, c2) -> {
                        Comparator<Object> comp = (Comparator<Object>) keyComparator;
                        Object o1 = keyExtractor.apply(c1);
                        o1 = ConversionUtils.convertToUninstrumented(o1);
                        Object o2 = keyExtractor.apply(c2);
                        o2 = ConversionUtils.convertToUninstrumented(o2);
                        return comp.compare(o1, o2);
                    };
        } else {
            return (Comparator<T> & Serializable)
                    (c1, c2) ->  keyComparator.compare(keyExtractor.apply(c1), keyExtractor.apply(c2));
            }
    }
    public static IASString cleanUTF8ForXml(IASString string) {
        if(string == null) {
            return null;
        }

        int length = string.length();
        if(length == 0) {
            return string;
        }
        IASTaintInformationable tis = string.getTaintInformation();
        StringBuilder sb = new StringBuilder(length);
        for(int i=0; i<length; i++) {
            int ch = string.codePointAt(i);
            if(ch < 32) {
                switch(ch) {
                    case '\n': //0x000A
                    case '\t': //0x0009
                    case '\r': sb.appendCodePoint(ch); break;//0x000D
                    default: // dump them
                }
            } else if(ch >= 0x0020 && ch <= 0xD7FF) {
                sb.appendCodePoint(ch);
            } else if(ch >= 0xE000 && ch <= 0xFFFD) {
                sb.appendCodePoint(ch);
            } else if(ch >= 0x10000 && ch <= 0x10FFFF) {
                sb.appendCodePoint(ch);
            }
        }
        // TODO: if this works shift length accordingly
        return new IASString(sb.toString(), tis);
    }

    public static Stream<Object> bufferedReaderLines(BufferedReader reader) {
        return reader.lines().map(ConversionUtils::convertToInstrumented);
    }

    public static IASString getStringFromResourceBundle(ResourceBundle rb, IASString key) {
        Object val = rb.getObject(key.getString());
        return fromObject(val);
    }

    public static IASString byteToString(byte b) {
        return IASString.fromString(Byte.toString(b));
    }

    public static IASString shortToString(short s) {
        return IASString.fromString(Short.toString(s));
    }

    public static IASString characterToString(char c) {
        return IASString.fromString(Character.toString(c));
    }

    public static IASString intToString(int i) {
        return IASString.fromString(Integer.toString(i));
    }

    public static IASString longToString(long l) {
        return IASString.fromString(Long.toString(l));
    }

    public static IASString floatToString(float f) {
        return IASString.fromString(Float.toString(f));
    }

    public static IASString doubleToString(double d) {
        return IASString.fromString(Double.toString(d));
    }

    public static IASString booleanToString(boolean b) {
        return IASString.fromString(Boolean.toString(b));
    }

    private IASStringUtils() {

    }
}

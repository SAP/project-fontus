package de.tubs.cs.ias.asm_test.taintaware.shared;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class IASURLEncoder {
    @Deprecated
    public static IASStringable encode(IASStringable url, IASFactory factory) {
        try {
            return encode(url, ((IASStringBuilderable) factory.createStringBuilder().append(Charset.defaultCharset().toString())).toIASString(), factory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isValidChar(char c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9') || c == '.' || c == '-' || c == '_' || c == '*';
    }

    public static IASStringable encode(IASStringable url, IASStringable enc, IASFactory factory) throws UnsupportedEncodingException {
        IASStringBuilderable strb = factory.createStringBuilder();
        boolean isValid = false;
        int start = 0;
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (isValid) {
                if (!isValidChar(c)) {
                    encodeValid(url, strb, start, i);
                    isValid = false;
                    start = i;
                }
            } else {
                if (isValidChar(c)) {
                    encodeNonValid(url, strb, start, i, enc, factory);
                    isValid = true;
                    start = i;
                }
            }
        }
        if (isValid) {
            encodeValid(url, strb, start, url.length());
        } else {
            encodeNonValid(url, strb, start, url.length(), enc, factory);
        }
        return strb.toIASString();
    }

    private static void encodeValid(IASStringable url, IASStringBuilderable strb, int start, int end) {
        strb.append(url.substring(start, end));
    }

    private static void encodeNonValid(IASStringable url, IASStringBuilderable strb, int start, int end, IASStringable enc, IASFactory factory) throws UnsupportedEncodingException {
        for (int i = start; i < end; i++) {
            IASStringable s = url.substring(i, i + 1);
            IASTaintSource source = s.getTaintFor(0);
            if (s.getString().equals(" ")) {
                IASStringBuilderable toAppend = factory.createStringBuilder();
                toAppend.append('+');
                toAppend.setTaint(source);
                strb.append(toAppend);
            } else {
                byte[] bytes = s.getBytes(enc);

                IASStringBuilderable toAppend = factory.createStringBuilder();
                for (byte aByte : bytes) {
                    toAppend.append('%');
                    toAppend.append(String.format("%02X", aByte));
                }
                toAppend.setTaint(source);
                strb.append(toAppend);
            }
        }
    }
}

package com.sap.fontus.taintaware.unified;

import com.sap.fontus.taintaware.shared.IASTaintMetadata;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public final class TURLEncoder {
    private TURLEncoder() {
    }

    @Deprecated
    public static IASString encode(IASString url) {
        try {
            return encode(url, new IASStringBuilder().append(Charset.defaultCharset().toString()).toIASString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isValidChar(char c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9') || c == '.' || c == '-' || c == '_' || c == '*';
    }

    public static IASString encode(IASString url, IASString enc) throws UnsupportedEncodingException {
        IASAbstractStringBuilder strb = new IASStringBuilder();
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
                    encodeNonValid(url, strb, start, i, enc);
                    isValid = true;
                    start = i;
                }
            }
        }
        if (isValid) {
            encodeValid(url, strb, start, url.length());
        } else {
            encodeNonValid(url, strb, start, url.length(), enc);
        }
        return strb.toIASString();
    }

    private static void encodeValid(IASString url, IASAbstractStringBuilder strb, int start, int end) {
        strb.append(url.substring(start, end));
    }

    private static void encodeNonValid(IASString url, IASAbstractStringBuilder strb, int start, int end, IASString enc) throws UnsupportedEncodingException {
        for (int i = start; i < end; i++) {
            IASString s = url.substring(i, i + 1);
            IASTaintMetadata source = s.getTaintInformationInitialized().getTaint(0);
            if (" ".equals(s.getString())) {
                IASAbstractStringBuilder toAppend = new IASStringBuilder();
                toAppend.append('+');
                toAppend.setTaint(source);
                strb.append(toAppend);
            } else {
                byte[] bytes = s.getBytes(enc);

                IASAbstractStringBuilder toAppend = new IASStringBuilder();
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

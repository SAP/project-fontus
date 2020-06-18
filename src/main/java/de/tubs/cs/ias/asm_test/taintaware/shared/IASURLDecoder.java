package de.tubs.cs.ias.asm_test.taintaware.shared;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class IASURLDecoder {
    @Deprecated
    public static IASStringable decode(IASStringable url, IASFactory factory) {
        try {
            return decode(url, ((IASStringBuilderable) factory.createStringBuilder().append(Charset.defaultCharset().toString())).toIASString(),
                    factory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static IASStringable decode(IASStringable url, IASStringable enc, IASFactory factory) throws UnsupportedEncodingException {
        IASStringBuilderable strb = factory.createStringBuilder();
        int start = 0;
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (isSpecial(c)) {
                strb.append(url.substring(start, i));
                if (c == '+') {
                    IASStringBuilderable s = factory.createStringBuilder();
                    s.append(' ');
                    IASTaintSource source = url.getTaintFor(i);
                    s.setTaint(source);
                    strb.append(s);
                } else {
                    i = decodeSpecial(url, strb, i, enc, factory);
                }
                start = i + 1;
            }
        }

        if (start < url.length()) {
            strb.append(url.substring(start));
        }

        return strb.toIASString();
    }

    private static int decodeSpecial(IASStringable url, IASStringBuilderable strb, int start, IASStringable enc, IASFactory factory) throws UnsupportedEncodingException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int i = start;
        do {
            buffer.write((byte) Short.parseShort(url.getString().substring(i + 1, i + 3), 16));
            i += 3;
        } while (i < url.length() && url.charAt(i) == '%');

        IASStringable toAppend = factory.createString(buffer.toByteArray(), Charset.forName(enc.getString()));

        // TODO Better taint merging
        toAppend.setTaint(url.substring(start, i).isTainted());

        strb.append(toAppend);
        return i - 1;
    }

    private static boolean isSpecial(char c) {
        return c == '+' || c == '%';
    }
}

package de.tubs.cs.ias.asm_test.taintaware.shared;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

public class IASURLDecoder {
    @Deprecated
    public static IASStringable decode(IASStringable url, IASFactory factory) {
        return decode(url, Charset.defaultCharset(), factory);
    }

    public static IASStringable decode(IASStringable url, IASStringable enc, IASFactory factory) {
        return decode(url, Charset.forName(enc.getString()), factory);
    }

    public static IASStringable decode(IASStringable url, Charset charset, IASFactory factory) {
        IASAbstractStringBuilderable strb = factory.createStringBuilder();

        int start = 0;
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (isSpecial(c)) {
                strb.append(url.substring(start, i));
                if (c == '+') {
                    IASAbstractStringBuilderable s = factory.createStringBuilder();
                    s.append(' ');
                    IASTaintSource source = url.getTaintFor(i);
                    s.setTaint(source);
                    strb.append(s);
                } else {
                    i = decodeSpecial(url, strb, i, charset, factory);
                }
                start = i + 1;
            }
        }

        if (start < url.length()) {
            strb.append(url.substring(start));
        }

        return strb.toIASString();
    }

    private static int decodeSpecial(final IASStringable url, final IASAbstractStringBuilderable strb, final int start, final Charset enc, final IASFactory factory) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int i = start;
        do {
            buffer.write((byte) Short.parseShort(url.getString().substring(i + 1, i + 3), 16));
            i += 3;
        } while (i < url.length() && url.charAt(i) == '%');

        String decoded = new String(buffer.toByteArray(), enc);
        IASStringable toAppend = getOriginalTaintInformationBack(url, decoded, start, enc, factory);

        strb.append(toAppend);
        return i - 1;
    }

    /**
     * Getting taint information for each decoded character by converting them back bytes and using the array length to determine the taint in the encoded string
     *
     * @return Returns the decoded String with the correct taint information
     */
    private static IASStringable getOriginalTaintInformationBack(IASStringable url, String decoded, int start, Charset enc, IASFactory factory) {
        // Iterating over every character to get back original taint information
        int processedLength = 0;

        IASAbstractStringBuilderable builder = factory.createStringBuilder();
        for (int j = 0; j < decoded.length(); j++) {
            String character = decoded.substring(j, j + 1);
            int length = character.getBytes(enc).length;

            int startInOrig = start + (processedLength * 3);
            int endInOrig = start + ((processedLength + length) * 3);

            IASStringable origChars = url.substring(startInOrig, endInOrig);

            // Iterating through every character of the encoded string, which belongs to the decoded character
            // If the taint information is homogeneous it is used, otherwise the taint information for the String is set to IASTaintSource.TS_CHAR_UNKNOWN_ORIGIN
            IASTaintSource source = null;
            for (int i = 0; i < origChars.length(); i++) {
                IASTaintSource curr = origChars.getTaintFor(i);
                if (source == null) {
                    source = curr;
                } else if(source != curr) {
                    source = IASTaintSourceRegistry.TS_CHAR_UNKNOWN_ORIGIN;
                    break;
                }
            }
            IASStringable toAppend = factory.createString(character);
            toAppend.setTaint(source);
            builder.append(toAppend);

            processedLength += length;
        }

        return builder.toIASString();
    }

    public static boolean isSpecial(char c) {
        return c == '+' || c == '%';
    }
}

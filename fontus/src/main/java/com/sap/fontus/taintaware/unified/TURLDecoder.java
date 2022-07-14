package com.sap.fontus.taintaware.unified;


import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

public class TURLDecoder {
    @Deprecated
    public static IASString decode(IASString url) {
        return decode(url, Charset.defaultCharset());
    }

    public static IASString decode(IASString url, IASString enc) {
        return decode(url, Charset.forName(enc.getString()));
    }

    public static IASString decode(IASString url, Charset charset) {
        IASAbstractStringBuilder strb = new IASStringBuilder();

        int start = 0;
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (isSpecial(c)) {
                strb.append(url.substring(start, i));
                if (c == '+') {
                    IASAbstractStringBuilder s = new IASStringBuilder();
                    s.append(' ');
                    IASTaintMetadata source = url.getTaintInformationInitialized().getTaint(i);
                    s.setTaint(source);
                    strb.append(s);
                } else {
                    i = decodeSpecial(url, strb, i, charset);
                }
                start = i + 1;
            }
        }

        if (start < url.length()) {
            strb.append(url.substring(start));
        }

        return strb.toIASString();
    }

    private static int decodeSpecial(final IASString url, final IASAbstractStringBuilder strb, final int start, final Charset enc) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int i = start;
        do {
            buffer.write((byte) Short.parseShort(url.getString().substring(i + 1, i + 3), 16));
            i += 3;
        } while (i < url.length() && url.charAt(i) == '%');

        String decoded = new String(buffer.toByteArray(), enc);
        IASString toAppend = getOriginalTaintInformationBack(url, decoded, start, enc);

        strb.append(toAppend);
        return i - 1;
    }

    /**
     * Getting taint information for each decoded character by converting them back bytes and using the array length to determine the taint in the encoded string
     *
     * @return Returns the decoded String with the correct taint information
     */
    private static IASString getOriginalTaintInformationBack(IASString url, String decoded, int start, Charset enc) {
        // Iterating over every character to get back original taint information
        int processedLength = 0;

        IASAbstractStringBuilder builder = new IASStringBuilder();
        for (int j = 0; j < decoded.length(); j++) {
            String character = decoded.substring(j, j + 1);
            int length = character.getBytes(enc).length;

            int startInOrig = start + (processedLength * 3);
            int endInOrig = start + ((processedLength + length) * 3);

            IASString origChars = url.substring(startInOrig, endInOrig);

            // Iterating through every character of the encoded string, which belongs to the decoded character
            // If the taint information is homogeneous it is used, otherwise the taint information for the String is set to IASTaintSource.TS_CHAR_UNKNOWN_ORIGIN
            IASTaintMetadata source = null;
            for (int i = 0; i < origChars.length(); i++) {
                IASTaintMetadata curr = origChars.getTaintInformationInitialized().getTaint(i);
                if (source == null) {
                    source = curr;
                } else if(source != curr) {
                    source = IASTaintSourceRegistry.MD_CHAR_UNKNOWN_ORIGIN;
                    break;
                }
            }
            IASString toAppend = IASString.fromString(character);
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

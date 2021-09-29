package com.sap.fontus.sanitizer;

import com.sap.fontus.taintaware.shared.IASTaintRange;
import org.owasp.encoder.Encode;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Sanitization {

    private static Comparator<IASTaintRange> taintRangeComparator = new Comparator<IASTaintRange>() {
        // descending ordering, ranges are assumed to be disjoint
        public int compare(IASTaintRange range1, IASTaintRange range2) {
            if (range1.getStart() != range2.getStart()) {
                return range1.getStart() < range2.getStart() ? 1 : -1;
            } else if (range1.getEnd() != range2.getEnd()) {
                return range1.getEnd() < range2.getEnd() ? 1 : -1;
            } else {
                return 0;
            }
        }
    };

    public static String sanitizeSinks(String taintedString, List<IASTaintRange> taintRanges,
                                       List<String> sinkChecks) {
        for (String attCat : sinkChecks) {
            switch (attCat) {
                case "SQLi":
                    // do something, e.g. sanitizeAndExecuteQuery(..) (need something new -> wait for sanjit's sql parser)
                    // problem: how to detect whole query and connection to DB
                    // e.g. use sql sanitizer or log result or stop program
                    break;
                case "XSS":
                    taintedString = sanitizeHtml(taintedString, taintRanges);
                    break;
                case "LDAP":
                    break;
                case "PATHTRAVERS":
                    break;
                case "CMDi":
                    break;
                case "XPATHi":
                case "TRUSTBOUND":
                default:
                    // problem: where do we get the context from? already part of taint info?
                    // abort if type not known?
            }
        }
        return taintedString;
    }

    // Sanitizes for SQL injection (SQLi) prevention
    // Assumption: Only 1 sql value is included in taintrange. e.g. only
    // >london<, NOT >"london" and ID=2< (would also inclue column name and not only
    // value)
    //TODO: new classes
    protected static ResultSet sanitizeAndExecuteQuery(String taintedString, List<IASTaintRange> taintRanges, Connection con) {
        if (!taintRanges.isEmpty()) {
            // sort taint ranges
            taintRanges.sort(taintRangeComparator);
            // replaces all tainted chars, s
            StringBuilder quoteCountingString = new StringBuilder(taintedString);
            for (IASTaintRange range : taintRanges) {
                for (int i = range.getStart(); i < range.getEnd(); i++) {
                    quoteCountingString.setCharAt(i, ' ');
                }
            }
            // generate new string and values for prepared statement
            String preparedString = taintedString;
            List<String> values = new ArrayList<>();
            for (IASTaintRange range : taintRanges) {
                int taintRangeStart = range.getStart();
                int taintRangeEnd = range.getEnd();

                // check if chars in taintrange of preparedString are the same as before in the
                // "original" taintedString. If NOT, skip this range, since its content was
                // already altered by handling another range
                // TODO: only ignore already changed part.
                if (!preparedString.substring(taintRangeStart, taintRangeEnd)
                        .equals(taintedString.substring(taintRangeStart, taintRangeEnd))) {
                    continue;
                }

                int startTaintIndex = 0;
                int endTaintIndex = preparedString.length() - 1;

                // TODO: quotes might be part of text and not the syntax! should not be counted
                // if part of value and not query syntax
                long numberOfDoubleQuotes = 0;
                long numberOfSingleQuotes = 0;
                for (char c : quoteCountingString.substring(0, taintRangeStart).toCharArray()) {
                    if (c == '\'' && numberOfDoubleQuotes % 2 == 0) {
                        numberOfSingleQuotes++;
                    }
                    if (c == '\"' && numberOfSingleQuotes % 2 == 0) {
                        numberOfDoubleQuotes++;
                    }
                }

                if (numberOfDoubleQuotes % 2 == 0 && numberOfSingleQuotes % 2 == 0) {
                    // attribute value is NOT a text set in quotes
                    char[] notTextChars = new char[]{' ', ',', ';', '<', '>', '=', '(', ')'};
                    String help = preparedString.substring(0, taintRangeStart);
                    for (char c : notTextChars) {
                        startTaintIndex = Math.max(startTaintIndex, help.lastIndexOf(c) + 1);
                    }

                    if (taintRangeEnd < preparedString.length()) {
                        int noMatch = -1;
                        for (char c : notTextChars) {
                            if (preparedString.indexOf(c, taintRangeEnd) != -1) {
                                endTaintIndex = Math.min(endTaintIndex, preparedString.indexOf(c, taintRangeEnd));
                            }
                            noMatch = Math.max(noMatch, preparedString.indexOf(c, taintRangeEnd));
                        }
                        // check is necessary to include whole charsequence at end of sql query
                        if (noMatch != -1) {
                            endTaintIndex = endTaintIndex - 1;
                        } else {
                            endTaintIndex = preparedString.length() - 1;
                        }
                    }
                } else {
                    // attribute value IS a text set in quotes
                    // find closest quote to left and right of taintrange und use this as new
                    // "range" to set the value
                    startTaintIndex = taintRangeStart;
                    endTaintIndex = preparedString.length() - 1;
                    char[] textChars = new char[]{'\"', '\''};
                    String temp = preparedString;
                    if (taintRangeStart > 0 && taintRangeEnd < preparedString.length()) {
                        char quoteType = '\"';
                        for (char c : textChars) {
                            int helpStart = preparedString.substring(0, taintRangeStart).lastIndexOf(c);
                            int helpEnd = preparedString.indexOf(c, taintRangeEnd);
                            if (helpStart != -1 && helpEnd != -1 && helpEnd <= endTaintIndex) {
                                startTaintIndex = helpStart + 1;
                                endTaintIndex = helpEnd - 1;
                                quoteType = c;
                            }
                        }
                        if (preparedString.charAt(startTaintIndex - 1) == quoteType
                                && preparedString.charAt(endTaintIndex + 1) == quoteType) {
                            temp = preparedString.substring(0, startTaintIndex - 1) + " "
                                    + preparedString.substring(startTaintIndex, endTaintIndex + 1);
                            if (endTaintIndex < preparedString.length() - 2) {
                                temp = temp + " " + preparedString.substring(endTaintIndex + 2);
                            }
                        }
                    }
                    preparedString = temp;
                }

                // add value to list
                String value = preparedString.substring(startTaintIndex, endTaintIndex + 1);
                values.add(value);
                // replace values with ?
                if (endTaintIndex < preparedString.length() - 1 && startTaintIndex != 0) {
                    preparedString = preparedString.substring(0, startTaintIndex) + " ? "
                            + preparedString.substring(endTaintIndex + 1);
                } else if (startTaintIndex != 0) {
                    preparedString = preparedString.substring(0, startTaintIndex) + " ? ";
                } else {
                    preparedString = " ? ";
                }
            }

            PreparedStatement p;
            try {
                p = con.prepareStatement(preparedString);
                ParameterMetaData parameterData = p.getParameterMetaData();
                for (int i = 1; i <= parameterData.getParameterCount(); i++) {
                    // values is in descending order, i.e. value for last "?" is first in the list
                    switch (parameterData.getParameterType(i)) {
                        case -16: // LONGNVARCHAR
                        case -15: // NCHAR 
                        case -9: // NVARCHAR
                            p.setNString(i, values.get(parameterData.getParameterCount() - i));
                            break;
                        case -7: // BIT
                        case 16: // BOOLEAN
                            p.setBoolean(i, Boolean.parseBoolean(values.get(parameterData.getParameterCount() - i)));
                            break;
                        case -6: // TINYINT
                            p.setByte(i, Byte.parseByte(values.get(parameterData.getParameterCount() - i)));
                            break;
                        case -5: // BIGINT
                            p.setLong(i, Long.parseLong(values.get(parameterData.getParameterCount() - i)));
                            break;
                        case -4: // LONGVARBINARY
                        case -3: // VARBINARY
                        case -2: // BINARY
                            p.setBytes(i, values.get(parameterData.getParameterCount() - i).getBytes());
                            break;
                        case -1: // LONGVARCHAR
                        case 1: // CHAR
                        case 12: // VARCHAR
                            p.setString(i, values.get(parameterData.getParameterCount() - i));
                            break;
                        case 2: // NUMERIC
                        case 3: // DECIMAL
                            p.setBigDecimal(i, new BigDecimal(values.get(parameterData.getParameterCount() - i)));
                            break;
                        case 4: // INTEGER
                            p.setInt(i, Integer.parseInt(values.get(parameterData.getParameterCount() - i)));
                            break;
                        case 5: // SMALLINT
                            p.setShort(i, Short.parseShort(values.get(parameterData.getParameterCount() - i)));
                            break;
                        case 6: // FLOAT
                        case 8: // DOUBLE
                            p.setDouble(i, Double.parseDouble(values.get(parameterData.getParameterCount() - i)));
                            break;
                        case 7: // REAL
                            p.setFloat(i, Float.parseFloat(values.get(parameterData.getParameterCount() - i)));
                            break;
                        case 91: // DATE
                            p.setDate(i, Date.valueOf(values.get(parameterData.getParameterCount() - i)));
                            break;
                        case 92: // TIME
                            p.setTime(i, Time.valueOf(values.get(parameterData.getParameterCount() - i)));
                            break;
                        case 93: // TIMESTAMP
                            p.setTimestamp(i, Timestamp.valueOf(values.get(parameterData.getParameterCount() - i)));
                            break;
                        case 0: // NULL
                        default:
                            p.setObject(i, values.get(parameterData.getParameterCount() - i),
                                    parameterData.getParameterType(i));
                            break;
                    }
                }
                // sanitization successful, execute query and return ResultSet
                return p.executeQuery();
            } catch (SQLException | NullPointerException | NumberFormatException e) {
                // Cannot create prepared statement due to syntax error.
                // OR Input does not match sql syntax
                // OR Input cannot be replaced by prepared statement
                // OR Input does not match required data type!
                // -> sanitization NOT successful
                return null;
            }// Input does not match sql syntax
            // Input can't be replaced by prepared statement
            // Input does not match required data type!

        } else {
            try {
                // no sanatization necessary, use regular statement to execute the query
                return con.createStatement().executeQuery(taintedString);
            } catch (Exception e) {
                // Cannot create prepared statement due to syntax error.
                // sanatization NOT successful
                return null;
            }
        }
    }

    // check if char of string is tainted
    private static boolean charIsTainted(int charPosition, List<IASTaintRange> taintRanges) {
        for (IASTaintRange range : taintRanges) {
            if (charPosition >= range.getStart() && charPosition < range.getEnd()) {
                return true;
            }
        }
        return false;
    }

    // taint-aware html parser
    public static String sanitizeHtml(String taintedString, List<IASTaintRange> taintRanges) {
        boolean tagDeclaration = false;
        boolean insideTag = false;
        boolean attDeclaration = false;
        boolean closingTag = false;
        boolean comment = false;
        boolean insideRoundBracket = false;
        List<String> tags = new ArrayList<>();
        StringBuilder tag = new StringBuilder();
        StringBuilder attributeName = new StringBuilder();
        StringBuilder sanitizedString = new StringBuilder();
        // loop over each char in string
        for (int i = 0; i < taintedString.length(); i++) {
            // Check if the current character is tainted
            boolean charIsTainted = false;
            IASTaintRange taintedRange = null;
            for (IASTaintRange range : taintRanges) {
                if (i >= range.getStart() && i < range.getEnd()) {
                    charIsTainted = true;
                    taintedRange = range;
                    break;
                }
            }

            if (charIsTainted) {
                // Call the encoder relevant for the current context and encode the complete
                // taintrange. Note there may be contexts where tainted strings are not allowed
                // at all (e.g. in a tag name).
                if (tagDeclaration) {
                    // <tag> should not be tainted i.e. not inserted by the user
                    return null;
                } else if (insideTag && attDeclaration) {
                    /**
                     * Alternative for getting attribute name String attributeName =
                     * taintedString.substring(0, taintedString.lastIndexOf("=")).trim();
                     * attributeName = attributeName.substring(attributeName.lastIndexOf(" "),
                     * attributeName.length());
                     */

                    // attribute values are allowed to be inserted by the user
                    switch (attributeName.toString().toLowerCase()) {
                        // TODO: more cases
                        // complex attributes
                        case "href":
                        case "src":
                        case "style":
                        case "onmouseover":
                            sanitizedString.append(Encode.forJavaScript(
                                    taintedString.substring(taintedRange.getStart(), taintedRange.getEnd())));
                            i = taintedRange.getEnd() - 1;
                            break;
                        // typical attribute values like width, name or value
                        default:
                            sanitizedString.append(Encode.forHtml(
                                    taintedString.substring(taintedRange.getStart(), taintedRange.getEnd())));
                            i = taintedRange.getEnd() - 1;
                    }
                } else if (insideTag) {
                    // attribute names should NOT be given by a user
                    return null;
                } else if (comment) {
                    // NEVER insert untrusted data in html comments
                    return null;
                } else {
                    // content is tainted, i.e. text between two tags is tainted
                    // check which context

                    // script context and inside a function call!
                    //For nearly all functions a possible attack i.e. don't allow it -> return null
                    if (tags.contains("script") && insideRoundBracket) {
                        return null;
                        // script context
                    } else if (tags.contains("script")) {
                        sanitizedString.append(" \"").append(Encode.forJavaScript(
                                taintedString.substring(taintedRange.getStart(), taintedRange.getEnd()))).append("\" ");
                        i = taintedRange.getEnd() - 1;
                    } else {
                        // html context
                        sanitizedString.append(Encode.forHtml(taintedString.substring(taintedRange.getStart(), taintedRange.getEnd())));
                        i = taintedRange.getEnd() - 1;
                        // ???
                        // TODO: add more else cases for other contexts
                    }
                }
                // not tainted context
            } else {
                if (insideTag) {
                    if (taintedString.charAt(i) <= ' ') {
                        // ignore whitespaces
                        if (!tag.toString().equals("")) {
                            tagDeclaration = false;
                        }
                    } else if (taintedString.charAt(i) == '>') { // end of tag
                        insideTag = false;
                        tagDeclaration = false;
                        attDeclaration = false;
                        tags.add(tag.toString().toLowerCase());
                        attributeName = new StringBuilder();
                        tag = new StringBuilder();
                    } else if (taintedString.charAt(i) == '=') { // = is followed by attribute value
                        attDeclaration = true;
                        tagDeclaration = false;
                    } else if (tagDeclaration) {
                        tag.append(taintedString.charAt(i));
                    } else if (!attDeclaration) {
                        attributeName.append(taintedString.charAt(i));
                    } else {
                        // ignore remaining chars
                        attributeName = new StringBuilder();
                    }
                } else if (closingTag) {
                    if (taintedString.charAt(i) <= ' ') {
                        // ignore whitespaces
                    } else if (taintedString.charAt(i) == '>') { // end of tag
                        closingTag = false;
                        tags.remove(tags.size() - 1);
                        tag = new StringBuilder();
                    } else {
                        tag.append(taintedString.charAt(i));
                    }
                } else if (taintedString.charAt(i) == '<' && !comment) {
                    if (i + 1 < taintedString.length() && taintedString.charAt(i + 1) == '/') {
                        closingTag = true;
                    } else if (i + 3 < taintedString.length() && taintedString.charAt(i + 1) == '!'
                            && taintedString.charAt(i + 2) == '-' && taintedString.charAt(i + 3) == '-') {
                        comment = true;
                    } else {
                        insideTag = true;
                        tagDeclaration = true;
                    }
                } else if (comment && taintedString.charAt(i) == '>' && taintedString.charAt(i - 1) == '-'
                        && taintedString.charAt(i - 2) == '-') {
                    comment = false;
                } else if (taintedString.charAt(i) == '(') {
                    insideRoundBracket = true;
                } else if (taintedString.charAt(i) == ')') {
                    insideRoundBracket = false;
                } else {
                    // ignore remaining chars
                }
                // not tainted: parse the next character and calculate the context
                // Keep track of the current HTML context (e.g. attributeName, attributeValue,
                // innerHTML, ...)
                // TODO:
                sanitizedString.append(taintedString.charAt(i));
            }
        }
        // return
        return sanitizedString.toString();

    }

    // Not used method!!!
    // XSS prevention
    protected static String sanitizeForXssPrevention(String taintedString, List<IASTaintRange> taintRanges,
                                                     HashMap<IASTaintRange, XssContext> contexts) {
        // sanitization is only necessary if something is tainted
        if (!taintRanges.isEmpty()) {

            // check if html, css or else...
            // use owasp encode if fine with license
            taintRanges.sort(taintRangeComparator);
            StringBuilder sb = new StringBuilder(taintedString);
            for (IASTaintRange range : taintRanges) {
                String sanitizedSubstring = "";

                // switch over context of different taintranges of the string
                switch (contexts.get(range)) {

                    // Encodes for (X)HTML text content and text attributes but NOT comments or
                    // attribute names
                    case HtmlTextContent:
                    case HtmlAttributeValue:
                        sanitizedSubstring = Encode
                                .forHtml(taintedString.substring(range.getStart(), range.getEnd()));
                        break;

                    // Encodes for CSS strings in style blocks and attributes in Html
                    // MUST be surrounded by quotes
                    // stricter alternative to current approach: only allow insertion of untrusted
                    // data in property value of css
                    // AND don't allow URL's to start with javascript or expression
                    // TODO: decide whether or not these measures should be taken
                    case CssInlineString:
                        if (taintedString.substring(0, range.getStart()).trim().endsWith("\"")
                                && taintedString.substring(range.getEnd()).trim().startsWith("\"")) {
                            sanitizedSubstring = Encode
                                    .forCssString(taintedString.substring(range.getStart(), range.getEnd()));
                        } else {
                            sanitizedSubstring = "\""
                                    + Encode.forCssString(taintedString.substring(range.getStart(), range.getEnd()))
                                    + "\"";
                        }
                        break;
                    case CssInternalString:
                        sanitizedSubstring = Encode
                                .forCssString(taintedString.substring(range.getStart(), range.getEnd()));
                        break;

                    // Encodes for CSS URLs in style blocks and attributes in Html
                    // Must be surrounded by "url(" and ")"
                    // NOTE: this does NOT check the quality or safety of the URL itself
                    // TODO: add method that checks if given URL is safe. If not abort.
                    case CssUrl:
                        if (taintedString.substring(0, range.getStart()).trim().endsWith("\"url(\"")
                                && taintedString.substring(range.getEnd()).trim().startsWith("\")\"")) {
                            sanitizedSubstring = Encode
                                    .forCssUrl(taintedString.substring(range.getStart(), range.getEnd()));
                        } else {
                            sanitizedSubstring = "url("
                                    + Encode.forCssUrl(taintedString.substring(range.getStart(), range.getEnd()))
                                    + ")";
                        }
                        break;

                    // Percent-encoding of a URL according to RFC 3986.
                    // ONLY ADDITIONAL to the other contexts. Not safe on its own.
                    // NOTE: this does NOT check the quality or safety of the URL itself
                    // TODO: add method that checks if given URL is safe. If not abort.
                    case Uri:
                        try {
                            sanitizedSubstring = new URI(taintedString.substring(range.getStart(), range.getEnd()))
                                    .toString();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                        break;

                    // Percent-encoding of component of a URI, such as a query parameter, name or
                    // value, path or query-string.
                    case UriComponent:
                        sanitizedSubstring = Encode
                                .forUriComponent(taintedString.substring(range.getStart(), range.getEnd()));
                        break;

                    // Encodes XML and XHTML text content
                    case XmlContent:
                    case XHtmlContent:
                        sanitizedSubstring = Encode
                                .forXmlContent(taintedString.substring(range.getStart(), range.getEnd()));
                        break;

                    // Encodes XML and XHTML attribut content
                    case XmlAttributeValue:
                    case XHtmlAttributeValue:
                        sanitizedSubstring = Encode
                                .forXmlAttribute(taintedString.substring(range.getStart(), range.getEnd()));
                        break;

                    // ONLY encodes XML comments. NOT for XHTML comments
                    case XmlComment:
                        sanitizedSubstring = Encode
                                .forXmlComment(taintedString.substring(range.getStart(), range.getEnd()));
                        break;

                    // Encodes XML CDATA
                    case CDATA:
                        sanitizedSubstring = Encode
                                .forCDATA(taintedString.substring(range.getStart(), range.getEnd()));
                        break;

                    // Encodes java code given as string
                    case Java:
                        if (taintedString.substring(0, range.getStart()).trim().endsWith("\"")
                                && taintedString.substring(range.getEnd()).trim().startsWith("\"")) {
                            sanitizedSubstring = Encode
                                    .forJava(taintedString.substring(range.getStart(), range.getEnd()));
                        } else {
                            sanitizedSubstring = "\""
                                    + Encode.forJava(taintedString.substring(range.getStart(), range.getEnd()))
                                    + "\"";
                        }
                        break;

                    // Encodes javascript code given as string
                    // safe for use in HTML, script attributes, script blocks, JSON files, and
                    // JavaScript source.
                    // BUT there are javascript functions which arn't safe when inserting untrusted
                    // data even if escaped, e.g. in window.setInterval(...)
                    // TODO: decide whether dynamically generated javascript code should be allowed
                    case JavaScript:
                        if (taintedString.substring(0, range.getStart()).trim().endsWith("\"")
                                && taintedString.substring(range.getEnd()).trim().startsWith("\"")) {
                            sanitizedSubstring = Encode
                                    .forJavaScript(taintedString.substring(range.getStart(), range.getEnd()));
                        } else {
                            sanitizedSubstring = "\"" + Encode.forJavaScript(
                                    taintedString.substring(range.getStart(), range.getEnd())) + "\"";
                        }
                        break;

                    // none of the above? Then deny untrusted data insertion!
                    // This includes nested data insertion!
                    // Don't allow untrusted data directly in script, Html comment, attribute name,
                    // tag name or directly in CSS in style content
                    case HtmlAttributeName:
                    case XmlAttributeName:
                    case XHtmlAttributeName:
                    case HtmlComment:
                    case XHtmlComment:
                    default:
                        return null;
                }
                sb.replace(range.getStart(), range.getEnd(), sanitizedSubstring);
            }
            return sb.toString();
        } else {
            return taintedString;
        }
    }

    protected enum XssContext {
        HtmlAttributeName, HtmlAttributeValue, HtmlComment, HtmlTextContent, CssInlineString, CssInternalString, CssUrl,
        Uri, UriComponent, XmlContent, XmlAttributeValue, XmlComment, XmlAttributeName, XHtmlContent,
        XHtmlAttributeValue, XHtmlComment, XHtmlAttributeName, CDATA, Java, JavaScript
    }
}
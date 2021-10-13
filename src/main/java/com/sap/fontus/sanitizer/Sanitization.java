package com.sap.fontus.sanitizer;

import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;
import org.owasp.encoder.Encode;

import java.io.File;
import java.math.BigDecimal;
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
import java.util.List;
import java.util.Random;

public class Sanitization {

    // descending ordering, ranges are assumed to be disjoint
    private static Comparator<IASTaintRange> taintRangeComparator = (range1, range2) -> {
        if (range1.getStart() != range2.getStart()) {
            return range1.getStart() < range2.getStart() ? 1 : -1;
        } else if (range1.getEnd() != range2.getEnd()) {
            return range1.getEnd() < range2.getEnd() ? 1 : -1;
        } else {
            return 0;
        }
    };

    public static String sanitizeSinks(String taintedString, IASTaintInformationable taintInfo, List<String> sinkChecks) {
        String sanitizedString = taintedString;
        IASTaintRanges taintRanges = taintInfo.getTaintRanges(sanitizedString.length());
        for (String attCat : sinkChecks) {
            switch (attCat) {
                case "SQLi":
                    System.out.println("Not yet implemented!");
                    throwRandomRuntimeException();
                    // do something, e.g. sanitizeAndExecuteQuery(..) (need something new -> wait for sanjit's sql parser)
                    // problem: how to detect whole query and connection to DB
                    // e.g. use sql sanitizer or log result or stop program
                    break;
                case "XSS":
                    sanitizedString = sanitizeHtml(taintedString, taintRanges);
                    break;
                case "PATHTRAVERS":
                    sanitizedString = sanitizePath(taintedString, taintRanges);
                    break;
                case "CMDi":
                    sanitizedString = sanitizeCommands(taintedString, taintRanges);
                    break;
                case "LDAP":
                case "XPATHi":
                case "TRUSTBOUND":
                default:
                    // abort if type not known?
                    throwRandomRuntimeException();
            }
        }
        return sanitizedString;
    }

    private static String sanitizeCommands(String taintedString, IASTaintRanges taintRanges) {
        if (!taintRanges.isEmpty()) {
            throwRandomRuntimeException();
        }
        return taintedString;
    }

    private static String sanitizePath(String taintedString, IASTaintRanges taintRanges) {
        int startIndex = 0;
        while (startIndex < taintedString.length()) {
            int i = taintedString.indexOf(".." + File.separator, startIndex);
            // if sequence "../" doesn't occur end while loop
            if (i == -1) {
                break;
            }
            // check for each taint range if it overlaps with "../" of the taintedString
            for (IASTaintRange range : taintRanges) {
                // if either of the chars of "../" is tainted, then throw an exception
                if ((range.getStart() <= i && i < range.getEnd()) || (range.getStart() <= i + 1 && i + 1 < range.getEnd()) || (range.getStart() <= i + 2 && i + 2 < range.getEnd())) {
                    throwRandomRuntimeException();
                }
            }
            // update start index to next char after the last occurance of "../"
            startIndex = i + 3;
        }
        return taintedString;
    }

    private static void throwRandomRuntimeException() {
        // List of runtime exceptions (without parameter)
        RuntimeException[] exceptions = {new ArithmeticException(), new ArrayStoreException(), new ClassCastException(), new IllegalArgumentException(),
                new IllegalThreadStateException(), new NumberFormatException(), new IllegalCallerException(), new IllegalMonitorStateException(),
                new IllegalStateException(), new IndexOutOfBoundsException(), new ArrayIndexOutOfBoundsException(), new StringIndexOutOfBoundsException(),
                new LayerInstantiationException(), new NegativeArraySizeException(), new NullPointerException(), new RuntimeException(),
                new SecurityException(), new UnsupportedOperationException()};
        // Generate random number
        Random r = new Random();
        int pos = r.nextInt(exceptions.length);
        // Throw random exception
        throw exceptions[pos];
    }

    // Sanitizes for SQL injection (SQLi) prevention
    // Assumption: Only 1 sql value is included in taintrange. e.g. only >london<, NOT >"london" and ID=2< (would also inclue column name and not only value)
    protected static ResultSet sanitizeAndExecuteQuery(String taintedString, IASTaintRanges taintRanges, Connection con) {
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

    // taint-aware html parser
    public static String sanitizeHtml(String taintedString, IASTaintRanges taintRanges) {
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
                }
                // else {  ignore remaining chars  }

                // not tainted: parse the next character and calculate the context
                // Keep track of the current HTML context (e.g. attributeName, attributeValue,
                // innerHTML, ...)
                sanitizedString.append(taintedString.charAt(i));
            }
        }
        // return
        return sanitizedString.toString();
    }
}

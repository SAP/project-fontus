package com.sap.fontus.taintaware.unified;

import com.sap.fontus.taintaware.IASTaintAware;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;

public class IASFormatter implements Closeable, Flushable {
    private final Appendable output;
    private final Locale locale;
    private IOException lastIOException;
    private boolean closed;

    public IASFormatter(Formatter formatter) {
        this(formatter.out(), formatter.locale());
    }

    public IASFormatter() {
        this(new IASStringBuffer());
    }

    public IASFormatter(Appendable a) {
        this(Objects.requireNonNull(a), Locale.getDefault());
    }

    public IASFormatter(Appendable a, Locale l) {
        this.output = a;
        this.locale = l;
    }

    public IASFormatter(File file) throws FileNotFoundException {
        this(file, IASString.fromString(Charset.defaultCharset().name()));
    }

    public IASFormatter(File file, IASString csn) throws FileNotFoundException {
        this(file, csn, Locale.getDefault());
    }

    public IASFormatter(File file, IASString csn, Locale l) throws FileNotFoundException {
        this(new FileOutputStream(file), csn, l);
    }

    public IASFormatter(Locale l) {
        this(new IASStringBuffer(), l);
    }

    public IASFormatter(OutputStream o) {
        this(o, IASString.fromString(Charset.defaultCharset().name()));
    }

    public IASFormatter(OutputStream o, IASString csn) {
        this(o, csn, Locale.getDefault());
    }

    public IASFormatter(OutputStream o, IASString csn, Locale l) {
        this(new PrintWriter(new OutputStreamWriter(o, Charset.forName(csn.getString()))), l);
    }

    public IASFormatter(PrintStream o) {
        this((Appendable) o);
    }

    public IASFormatter(IASString fileName) throws FileNotFoundException {
        this(new File(fileName.getString()));
    }

    public IASFormatter(IASString fileName, IASString csn) throws FileNotFoundException {
        this(new File(fileName.getString()), csn);

    }

    public IASFormatter(IASString fileName, IASString csn, Locale l) throws FileNotFoundException {
        this(new File(fileName.getString()), csn, l);

    }

    public static IASFormatter fromFormatter(Formatter param) {
        if (param == null) {
            return null;
        }
        return new IASFormatter(param);
    }

    public IASFormatter format(IASString format, Object... args) {
        return this.format(this.locale, format, args);
    }

    public IASFormatter format(Locale l, IASString format, Object... args) {
        this.checkClosed();
        IASCharBuffer formatBuffer = new IASCharBuffer(format);
        IASFormatter.ParserStateMachine parser = new IASFormatter.ParserStateMachine(formatBuffer);
        IASFormatter.Transformer transformer = new IASFormatter.Transformer(this, l);

        int currentObjectIndex = 0;
        Object lastArgument = null;
        boolean hasLastArgumentSet = false;
        while (formatBuffer.hasRemaining()) {
            parser.reset();
            IASFormatter.FormatToken token = parser.getNextFormatToken();
            IASString result;
            IASString plainText = token.getPlainText();
            if (token.getConversionType() == (char) IASFormatter.FormatToken.UNSET) {
                result = plainText;
            } else {
                plainText = plainText.substring(0, plainText.indexOf('%'));
                Object argument = null;
                if (token.requireArgument()) {
                    int index = token.getArgIndex() == IASFormatter.FormatToken.UNSET ? currentObjectIndex++
                            : token.getArgIndex();
                    argument = this.getArgument(args, index, token, lastArgument,
                            hasLastArgumentSet);
                    lastArgument = argument;
                    hasLastArgumentSet = true;
                }
                result = transformer.transform(token, argument);
                result = (null == result ? plainText : plainText.concat(result));
            }
            // if output is made by formattable callback
            if (null != result) {
                try {
                    this.output.append(result);
                } catch (IOException e) {
                    this.lastIOException = e;
                }
            }
        }
        return this;
    }

    private void checkClosed() {
        if (this.closed) {
            throw new IllegalStateException("Formatter already closed!");
        }
    }

    public IOException ioException() {
        return this.lastIOException;
    }

    public Locale locale() {
        return this.locale;
    }

    public Appendable out() {
        return this.output;
    }

    @Override
    public void close() {
        if (this.output instanceof AutoCloseable) {
            try {
                ((AutoCloseable) this.output).close();
                this.closed = true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if (this.output instanceof Flushable) {
            ((Flushable) this.output).flush();
        }
    }

    @Override
    public String toString() {
        return this.output.toString();
    }

    public IASString toIASString() {
        return IASString.valueOf(this.output);
    }

    public Formatter getFormatter() {
        return new Formatter(this.output, this.locale);
    }

    private static class ParserStateMachine {

        private static final char EOS = (char) -1;

        private static final int EXIT_STATE = 0;

        private static final int ENTRY_STATE = 1;

        private static final int START_CONVERSION_STATE = 2;

        private static final int FLAGS_STATE = 3;

        private static final int WIDTH_STATE = 4;

        private static final int PRECISION_STATE = 5;

        private static final int CONVERSION_TYPE_STATE = 6;

        private static final int SUFFIX_STATE = 7;

        private IASFormatter.FormatToken token;

        private int state = ENTRY_STATE;

        private char currentChar = 0;

        private IASCharBuffer format;

        ParserStateMachine(IASCharBuffer format) {
            this.format = format;
        }

        void reset() {
            this.currentChar = (char) IASFormatter.FormatToken.UNSET;
            this.state = ENTRY_STATE;
            this.token = null;
        }

        /*
         * Gets the information about the current format token. Information is
         * recorded in the FormatToken returned and the position of the stream
         * for the format string will be advanced till the next format token.
         */
        IASFormatter.FormatToken getNextFormatToken() {
            this.token = new IASFormatter.FormatToken();
            this.token.setFormatStringStartIndex(this.format.position());

            // FINITE AUTOMATIC MACHINE
            while (true) {

                if (EXIT_STATE != this.state) {
                    // exit state does not need to get next char
                    this.currentChar = this.getNextFormatChar();
                    if (EOS == this.currentChar
                            && ENTRY_STATE != this.state) {
                        throw new UnknownFormatConversionException(this.getFormatString().toString());
                    }
                }

                switch (this.state) {
                    // exit state
                    case EXIT_STATE: {
                        this.process_EXIT_STATE();
                        return this.token;
                    }
                    // plain text state, not yet applied converter
                    case ENTRY_STATE: {
                        this.process_ENTRY_STATE();
                        break;
                    }
                    // begins converted string
                    case START_CONVERSION_STATE: {
                        this.process_START_CONVERSION_STATE();
                        break;
                    }
                    case FLAGS_STATE: {
                        this.process_FlAGS_STATE();
                        break;
                    }
                    case WIDTH_STATE: {
                        this.process_WIDTH_STATE();
                        break;
                    }
                    case PRECISION_STATE: {
                        this.process_PRECISION_STATE();
                        break;
                    }
                    case CONVERSION_TYPE_STATE: {
                        this.process_CONVERSION_TYPE_STATE();
                        break;
                    }
                    case SUFFIX_STATE: {
                        this.process_SUFFIX_STATE();
                        break;
                    }
                }
            }
        }

        /*
         * Gets next char from the format string.
         */
        private char getNextFormatChar() {
            if (this.format.hasRemaining()) {
                return this.format.get();
            }
            return EOS;
        }

        private IASString getFormatString() {
            int end = this.format.position();
            this.format.rewind();
            IASString formatString = this.format.subSequence(
                    this.token.getFormatStringStartIndex(), end);
            this.format.position(end);
            return formatString;
        }

        private void process_ENTRY_STATE() {
            if (EOS == this.currentChar) {
                this.state = EXIT_STATE;
            } else if ('%' == this.currentChar) {
                // change to conversion type state
                this.state = START_CONVERSION_STATE;
            }
            // else remains in ENTRY_STATE
        }

        private void process_START_CONVERSION_STATE() {
            if (Character.isDigit(this.currentChar)) {
                int position = this.format.position() - 1;
                int number = this.parseInt(this.format);
                char nextChar = (char) 0;
                if (this.format.hasRemaining()) {
                    nextChar = this.format.get();
                }
                if ('$' == nextChar) {
                    // the digital sequence stands for the argument
                    // index.
                    int argIndex = number;
                    // k$ stands for the argument whose index is k-1 except that
                    // 0$ and 1$ both stands for the first element.
                    if (argIndex > 0) {
                        this.token.setArgIndex(argIndex - 1);
                    } else if (argIndex == IASFormatter.FormatToken.UNSET) {
                        throw new MissingFormatArgumentException(
                                this.getFormatString().toString());
                    }
                    this.state = FLAGS_STATE;
                } else {
                    // the digital zero stands for one format flag.
                    if ('0' == this.currentChar) {
                        this.state = FLAGS_STATE;
                        this.format.position(position);
                    } else {
                        // the digital sequence stands for the width.
                        this.state = WIDTH_STATE;
                        // do not get the next char.
                        this.format.position(this.format.position() - 1);
                        this.token.setWidth(number);
                    }
                }
                this.currentChar = nextChar;
            } else if ('<' == this.currentChar) {
                this.state = FLAGS_STATE;
                this.token.setArgIndex(IASFormatter.FormatToken.LAST_ARGUMENT_INDEX);
            } else {
                this.state = FLAGS_STATE;
                // do not get the next char.
                this.format.position(this.format.position() - 1);
            }

        }

        private void process_FlAGS_STATE() {
            if (this.token.setFlag(this.currentChar)) {
                // remains in FLAGS_STATE
            } else if (Character.isDigit(this.currentChar)) {
                this.token.setWidth(this.parseInt(this.format));
                this.state = WIDTH_STATE;
            } else if ('.' == this.currentChar) {
                this.state = PRECISION_STATE;
            } else {
                this.state = CONVERSION_TYPE_STATE;
                // do not get the next char.
                this.format.position(this.format.position() - 1);
            }
        }

        private void process_WIDTH_STATE() {
            if ('.' == this.currentChar) {
                this.state = PRECISION_STATE;
            } else {
                this.state = CONVERSION_TYPE_STATE;
                // do not get the next char.
                this.format.position(this.format.position() - 1);
            }
        }

        private void process_PRECISION_STATE() {
            if (Character.isDigit(this.currentChar)) {
                this.token.setPrecision(this.parseInt(this.format));
            } else {
                // the precision is required but not given by the
                // format string.
                throw new UnknownFormatConversionException(this.getFormatString().toString());
            }
            this.state = CONVERSION_TYPE_STATE;
        }

        private void process_CONVERSION_TYPE_STATE() {
            this.token.setConversionType(this.currentChar);
            if ('t' == this.currentChar || 'T' == this.currentChar) {
                this.state = SUFFIX_STATE;
            } else {
                this.state = EXIT_STATE;
            }

        }

        private void process_SUFFIX_STATE() {
            this.token.setDateSuffix(this.currentChar);
            this.state = EXIT_STATE;
        }

        private void process_EXIT_STATE() {
            this.token.setPlainText(this.getFormatString());
        }

        /*
         * Parses integer value from the given buffer
         */
        private int parseInt(IASCharBuffer buffer) {
            int start = buffer.position() - 1;
            int end = buffer.limit();
            while (buffer.hasRemaining()) {
                if (!Character.isDigit(buffer.get())) {
                    end = buffer.position() - 1;
                    break;
                }
            }
            buffer.position(0);
            IASString intStr = buffer.subSequence(start, end);
            buffer.position(end);
            try {
                return Integer.parseInt(intStr.getString());
            } catch (NumberFormatException e) {
                return IASFormatter.FormatToken.UNSET;
            }
        }
    }

    private static class DateTimeUtil {
        private Calendar calendar;

        private final Locale locale;

        private IASStringBuilder result;

        private DateFormatSymbols dateFormatSymbols;

        DateTimeUtil(Locale locale) {
            this.locale = locale;
        }

        void transform(IASFormatter.FormatToken formatToken, Calendar aCalendar,
                       IASStringBuilder aResult) {
            this.result = aResult;
            this.calendar = aCalendar;
            char suffix = formatToken.getDateSuffix();

            switch (suffix) {
                case 'H': {
                    this.transform_H();
                    break;
                }
                case 'I': {
                    this.transform_I();
                    break;
                }
                case 'M': {
                    this.transform_M();
                    break;
                }
                case 'S': {
                    this.transform_S();
                    break;
                }
                case 'L': {
                    this.transform_L();
                    break;
                }
                case 'N': {
                    this.transform_N();
                    break;
                }
                case 'k': {
                    this.transform_k();
                    break;
                }
                case 'l': {
                    this.transform_l();
                    break;
                }
                case 'p': {
                    this.transform_p(true);
                    break;
                }
                case 's': {
                    this.transform_s();
                    break;
                }
                case 'z': {
                    this.transform_z();
                    break;
                }
                case 'Z': {
                    this.transform_Z();
                    break;
                }
                case 'Q': {
                    this.transform_Q();
                    break;
                }
                case 'B': {
                    this.transform_B();
                    break;
                }
                case 'b':
                case 'h': {
                    this.transform_b();
                    break;
                }
                case 'A': {
                    this.transform_A();
                    break;
                }
                case 'a': {
                    this.transform_a();
                    break;
                }
                case 'C': {
                    this.transform_C();
                    break;
                }
                case 'Y': {
                    this.transform_Y();
                    break;
                }
                case 'y': {
                    this.transform_y();
                    break;
                }
                case 'j': {
                    this.transform_j();
                    break;
                }
                case 'm': {
                    this.transform_m();
                    break;
                }
                case 'd': {
                    this.transform_d();
                    break;
                }
                case 'e': {
                    this.transform_e();
                    break;
                }
                case 'R': {
                    this.transform_R();
                    break;
                }

                case 'T': {
                    this.transform_T();
                    break;
                }
                case 'r': {
                    this.transform_r();
                    break;
                }
                case 'D': {
                    this.transform_D();
                    break;
                }
                case 'F': {
                    this.transform_F();
                    break;
                }
                case 'c': {
                    this.transform_c();
                    break;
                }
                default: {
                    throw new UnknownFormatConversionException(String
                            .valueOf(formatToken.getConversionType())
                            + formatToken.getDateSuffix());
                }
            }
        }

        private void transform_e() {
            int day = this.calendar.get(Calendar.DAY_OF_MONTH);
            this.result.append(day);
        }

        private void transform_d() {
            int day = this.calendar.get(Calendar.DAY_OF_MONTH);
            this.result.append(paddingZeros(day, 2));
        }

        private void transform_m() {
            int month = this.calendar.get(Calendar.MONTH);
            // The returned month starts from zero, which needs to be
            // incremented by 1.
            month++;
            this.result.append(paddingZeros(month, 2));
        }

        private void transform_j() {
            int day = this.calendar.get(Calendar.DAY_OF_YEAR);
            this.result.append(paddingZeros(day, 3));
        }

        private void transform_y() {
            int year = this.calendar.get(Calendar.YEAR);
            year %= 100;
            this.result.append(paddingZeros(year, 2));
        }

        private void transform_Y() {
            int year = this.calendar.get(Calendar.YEAR);
            this.result.append(paddingZeros(year, 4));
        }

        private void transform_C() {
            int year = this.calendar.get(Calendar.YEAR);
            year /= 100;
            this.result.append(paddingZeros(year, 2));
        }

        private void transform_a() {
            int day = this.calendar.get(Calendar.DAY_OF_WEEK);
            this.result.append(this.getDateFormatSymbols().getShortWeekdays()[day]);
        }

        private void transform_A() {
            int day = this.calendar.get(Calendar.DAY_OF_WEEK);
            this.result.append(this.getDateFormatSymbols().getWeekdays()[day]);
        }

        private void transform_b() {
            int month = this.calendar.get(Calendar.MONTH);
            this.result.append(this.getDateFormatSymbols().getShortMonths()[month]);
        }

        private void transform_B() {
            int month = this.calendar.get(Calendar.MONTH);
            this.result.append(this.getDateFormatSymbols().getMonths()[month]);
        }

        private void transform_Q() {
            long milliSeconds = this.calendar.getTimeInMillis();
            this.result.append(milliSeconds);
        }

        private void transform_s() {
            long milliSeconds = this.calendar.getTimeInMillis();
            milliSeconds /= 1000;
            this.result.append(milliSeconds);
        }

        private void transform_Z() {
            TimeZone timeZone = this.calendar.getTimeZone();
            this.result.append(timeZone
                    .getDisplayName(
                            timeZone.inDaylightTime(this.calendar.getTime()),
                            TimeZone.SHORT, this.locale));
        }

        private void transform_z() {
            int zoneOffset = this.calendar.get(Calendar.ZONE_OFFSET);
            zoneOffset /= 3600000;
            zoneOffset *= 100;
            if (zoneOffset >= 0) {
                this.result.append('+');
            }
            this.result.append(paddingZeros(zoneOffset, 4));
        }

        private void transform_p(boolean isLowerCase) {
            int i = this.calendar.get(Calendar.AM_PM);
            IASString s = IASString.valueOf(this.getDateFormatSymbols().getAmPmStrings()[i]);
            if (isLowerCase) {
                s = s.toLowerCase(this.locale);
            }
            this.result.append(s);
        }

        private void transform_N() {
            // TODO System.nanoTime();
            long nanosecond = this.calendar.get(Calendar.MILLISECOND) * 1000000L;
            this.result.append(paddingZeros(nanosecond, 9));
        }

        private void transform_L() {
            int millisecond = this.calendar.get(Calendar.MILLISECOND);
            this.result.append(paddingZeros(millisecond, 3));
        }

        private void transform_S() {
            int second = this.calendar.get(Calendar.SECOND);
            this.result.append(paddingZeros(second, 2));
        }

        private void transform_M() {
            int minute = this.calendar.get(Calendar.MINUTE);
            this.result.append(paddingZeros(minute, 2));
        }

        private void transform_l() {
            int hour = this.calendar.get(Calendar.HOUR);
            if (0 == hour) {
                hour = 12;
            }
            this.result.append(hour);
        }

        private void transform_k() {
            int hour = this.calendar.get(Calendar.HOUR_OF_DAY);
            this.result.append(hour);
        }

        private void transform_I() {
            int hour = this.calendar.get(Calendar.HOUR);
            if (0 == hour) {
                hour = 12;
            }
            this.result.append(paddingZeros(hour, 2));
        }

        private void transform_H() {
            int hour = this.calendar.get(Calendar.HOUR_OF_DAY);
            this.result.append(paddingZeros(hour, 2));
        }

        private void transform_R() {
            this.transform_H();
            this.result.append(':');
            this.transform_M();
        }

        private void transform_T() {
            this.transform_H();
            this.result.append(':');
            this.transform_M();
            this.result.append(':');
            this.transform_S();
        }

        private void transform_r() {
            this.transform_I();
            this.result.append(':');
            this.transform_M();
            this.result.append(':');
            this.transform_S();
            this.result.append(' ');
            this.transform_p(false);
        }

        private void transform_D() {
            this.transform_m();
            this.result.append('/');
            this.transform_d();
            this.result.append('/');
            this.transform_y();
        }

        private void transform_F() {
            this.transform_Y();
            this.result.append('-');
            this.transform_m();
            this.result.append('-');
            this.transform_d();
        }

        private void transform_c() {
            this.transform_a();
            this.result.append(' ');
            this.transform_b();
            this.result.append(' ');
            this.transform_d();
            this.result.append(' ');
            this.transform_T();
            this.result.append(' ');
            this.transform_Z();
            this.result.append(' ');
            this.transform_Y();
        }

        private static IASString paddingZeros(long number, int length) {
            int len = length;
            IASStringBuilder result = new IASStringBuilder();
            result.append(number);
            int startIndex = 0;
            if (number < 0) {
                len++;
                startIndex = 1;
            }
            len -= result.length();
            if (len > 0) {
                char[] zeros = new char[len];
                Arrays.fill(zeros, '0');
                result.insert(startIndex, zeros);
            }
            return result.toIASString();
        }

        private DateFormatSymbols getDateFormatSymbols() {
            if (null == this.dateFormatSymbols) {
                this.dateFormatSymbols = new DateFormatSymbols(this.locale);
            }
            return this.dateFormatSymbols;
        }
    }/*
     * Transforms the argument to the formatted string according to the format
     * information contained in the format token.
     */

    private static class Transformer {

        private final IASFormatter formatter;

        private IASFormatter.FormatToken formatToken;

        private Object arg;

        private final Locale locale;

        private static IASString lineSeparator;

        private NumberFormat numberFormat;

        private DecimalFormatSymbols decimalFormatSymbols;

        private IASFormatter.DateTimeUtil dateTimeUtil;

        Transformer(IASFormatter formatter, Locale locale) {
            this.formatter = formatter;
            this.locale = (null == locale ? Locale.US : locale);
        }

        private NumberFormat getNumberFormat() {
            if (null == this.numberFormat) {
                this.numberFormat = NumberFormat.getInstance(this.locale);
            }
            return this.numberFormat;
        }

        private DecimalFormatSymbols getDecimalFormatSymbols() {
            if (null == this.decimalFormatSymbols) {
                this.decimalFormatSymbols = new DecimalFormatSymbols(this.locale);
            }
            return this.decimalFormatSymbols;
        }

        /*
         * Gets the formatted string according to the format token and the
         * argument.
         */
        IASString transform(IASFormatter.FormatToken token, Object argument) {

            /* init data member to print */
            this.formatToken = token;
            this.arg = argument;

            IASString result;
            switch (token.getConversionType()) {
                case 'B':
                case 'b': {
                    result = this.transformFromBoolean();
                    break;
                }
                case 'H':
                case 'h': {
                    result = this.transformFromHashCode();
                    break;
                }
                case 'S':
                case 's': {
                    result = this.transformFromString();
                    break;
                }
                case 'C':
                case 'c': {
                    result = this.transformFromCharacter();
                    break;
                }
                case 'd':
                case 'o':
                case 'x':
                case 'X': {
                    if (null == this.arg || this.arg instanceof BigInteger) {
                        result = this.transformFromBigInteger();
                    } else {
                        result = this.transformFromInteger();
                    }
                    break;
                }
                case 'e':
                case 'E':
                case 'g':
                case 'G':
                case 'f':
                case 'a':
                case 'A': {
                    result = this.transformFromFloat();
                    break;
                }
                case '%': {
                    result = this.transformFromPercent();
                    break;
                }
                case 'n': {
                    result = this.transformFromLineSeparator();
                    break;
                }
                case 't':
                case 'T': {
                    result = this.transformFromDateTime();
                    break;
                }
                default: {
                    throw new UnknownFormatConversionException(String
                            .valueOf(token.getConversionType()));
                }
            }

            if (Character.isUpperCase(token.getConversionType())) {
                if (null != result) {
                    result = result.toUpperCase(Locale.US);
                }
            }
            return result;
        }

        /*
         * Transforms the Boolean argument to a formatted string.
         */
        private IASString transformFromBoolean() {
            IASStringBuilder result = new IASStringBuilder();
            int startIndex = 0;
            int flags = this.formatToken.getFlags();

            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_MINUS)
                    && !this.formatToken.isWidthSet()) {
                throw new MissingFormatWidthException("-" //$NON-NLS-1$
                        + this.formatToken.getConversionType());
            }

            // only '-' is valid for flags
            if (IASFormatter.FormatToken.FLAGS_UNSET != flags
                    && IASFormatter.FormatToken.FLAG_MINUS != flags) {
                throw new FormatFlagsConversionMismatchException(this.formatToken
                        .getStrFlags().getString(), this.formatToken.getConversionType());
            }

            if (null == this.arg) {
                result.append("false"); //$NON-NLS-1$
            } else if (this.arg instanceof Boolean) {
                result.append(this.arg);
            } else {
                result.append("true"); //$NON-NLS-1$
            }
            return this.padding(result, startIndex);
        }

        /*
         * Transforms the hashcode of the argument to a formatted string.
         */
        private IASString transformFromHashCode() {
            IASStringBuilder result = new IASStringBuilder();

            int startIndex = 0;
            int flags = this.formatToken.getFlags();

            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_MINUS)
                    && !this.formatToken.isWidthSet()) {
                throw new MissingFormatWidthException("-" //$NON-NLS-1$
                        + this.formatToken.getConversionType());
            }

            // only '-' is valid for flags
            if (IASFormatter.FormatToken.FLAGS_UNSET != flags
                    && IASFormatter.FormatToken.FLAG_MINUS != flags) {
                throw new FormatFlagsConversionMismatchException(this.formatToken
                        .getStrFlags().toString(), this.formatToken.getConversionType());
            }

            if (null == this.arg) {
                result.append("null"); //$NON-NLS-1$
            } else {
                String hexString = Integer.toHexString(this.arg.hashCode());
                IASString taintedHexString = new IASString(hexString);
                if (this.arg instanceof IASTaintAware && ((IASTaintAware) this.arg).isTainted()) {
                    taintedHexString.setTaint(true);
                }
                result.append(taintedHexString);
            }
            return this.padding(result, startIndex);
        }

        /*
         * Transforms the String to a formatted string.
         */
        private IASString transformFromString() {
            IASStringBuilder result = new IASStringBuilder();
            int startIndex = 0;
            int flags = this.formatToken.getFlags();

            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_MINUS)
                    && !this.formatToken.isWidthSet()) {
                throw new MissingFormatWidthException("-" //$NON-NLS-1$
                        + this.formatToken.getConversionType());
            }

            if (this.arg instanceof Formattable) {
                int flag = 0;
                // only minus and sharp flag is valid
                if (IASFormatter.FormatToken.FLAGS_UNSET != (flags & ~IASFormatter.FormatToken.FLAG_MINUS & ~IASFormatter.FormatToken.FLAG_SHARP)) {
                    throw new IllegalFormatFlagsException(this.formatToken
                            .getStrFlags().toString());
                }
                if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_MINUS)) {
                    flag |= FormattableFlags.LEFT_JUSTIFY;
                }
                if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_SHARP)) {
                    flag |= FormattableFlags.ALTERNATE;
                }
                if (Character.isUpperCase(this.formatToken.getConversionType())) {
                    flag |= FormattableFlags.UPPERCASE;
                }
                ((Formattable) this.arg).formatTo(this.formatter.getFormatter(), flag, this.formatToken
                        .getWidth(), this.formatToken.getPrecision());
                // all actions have been taken out in the
                // Formattable.formatTo, thus there is nothing to do, just
                // returns null, which tells the Parser to add nothing to the
                // output.
                return null;
            }
            // only '-' is valid for flags if the argument is not an
            // instance of Formattable
            if (IASFormatter.FormatToken.FLAGS_UNSET != flags
                    && IASFormatter.FormatToken.FLAG_MINUS != flags) {
                throw new FormatFlagsConversionMismatchException(this.formatToken
                        .getStrFlags().toString(), this.formatToken.getConversionType());
            }

            result.append(this.arg);
            return this.padding(result, startIndex);
        }

        /*
         * Transforms the Character to a formatted string.
         */
        private IASString transformFromCharacter() {
            IASStringBuilder result = new IASStringBuilder();

            int startIndex = 0;
            int flags = this.formatToken.getFlags();

            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_MINUS)
                    && !this.formatToken.isWidthSet()) {
                throw new MissingFormatWidthException("-" //$NON-NLS-1$
                        + this.formatToken.getConversionType());
            }

            // only '-' is valid for flags
            if (IASFormatter.FormatToken.FLAGS_UNSET != flags
                    && IASFormatter.FormatToken.FLAG_MINUS != flags) {
                throw new FormatFlagsConversionMismatchException(this.formatToken
                        .getStrFlags().toString(), this.formatToken.getConversionType());
            }

            if (this.formatToken.isPrecisionSet()) {
                throw new IllegalFormatPrecisionException(this.formatToken
                        .getPrecision());
            }

            if (null == this.arg) {
                result.append("null"); //$NON-NLS-1$
            } else {
                if (this.arg instanceof Character) {
                    result.append(this.arg);
                } else if (this.arg instanceof Byte) {
                    byte b = ((Byte) this.arg).byteValue();
                    if (!Character.isValidCodePoint(b)) {
                        throw new IllegalFormatCodePointException(b);
                    }
                    result.append((char) b);
                } else if (this.arg instanceof Short) {
                    short s = ((Short) this.arg).shortValue();
                    if (!Character.isValidCodePoint(s)) {
                        throw new IllegalFormatCodePointException(s);
                    }
                    result.append((char) s);
                } else if (this.arg instanceof Integer) {
                    int codePoint = ((Integer) this.arg).intValue();
                    if (!Character.isValidCodePoint(codePoint)) {
                        throw new IllegalFormatCodePointException(codePoint);
                    }
                    result.append(IASString.valueOf(Character.toChars(codePoint)));
                } else {
                    // argument of other class is not acceptable.
                    throw new IllegalFormatConversionException(this.formatToken
                            .getConversionType(), this.arg.getClass());
                }
            }
            return this.padding(result, startIndex);
        }

        /*
         * Transforms percent to a formatted string. Only '-' is legal flag.
         * Precision is illegal.
         */
        private IASString transformFromPercent() {
            IASStringBuilder result = new IASStringBuilder("%"); //$NON-NLS-1$

            int startIndex = 0;
            int flags = this.formatToken.getFlags();

            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_MINUS)
                    && !this.formatToken.isWidthSet()) {
                throw new MissingFormatWidthException("-" //$NON-NLS-1$
                        + this.formatToken.getConversionType());
            }

            if (IASFormatter.FormatToken.FLAGS_UNSET != flags
                    && IASFormatter.FormatToken.FLAG_MINUS != flags) {
                throw new FormatFlagsConversionMismatchException(this.formatToken
                        .getStrFlags().getString(), this.formatToken.getConversionType());
            }
            if (this.formatToken.isPrecisionSet()) {
                throw new IllegalFormatPrecisionException(this.formatToken
                        .getPrecision());
            }
            return this.padding(result, startIndex);
        }

        /*
         * Transforms line separator to a formatted string. Any flag, the width
         * or the precision is illegal.
         */
        private IASString transformFromLineSeparator() {
            if (this.formatToken.isPrecisionSet()) {
                throw new IllegalFormatPrecisionException(this.formatToken
                        .getPrecision());
            }

            if (this.formatToken.isWidthSet()) {
                throw new IllegalFormatWidthException(this.formatToken.getWidth());
            }

            int flags = this.formatToken.getFlags();
            if (IASFormatter.FormatToken.FLAGS_UNSET != flags) {
                throw new IllegalFormatFlagsException(this.formatToken.getStrFlags().getString());
            }

            if (null == lineSeparator) {
                lineSeparator = AccessController
                        .doPrivileged((PrivilegedAction<IASString>) () -> {
                            return IASString.valueOf(System.getProperty("line.separator")); //$NON-NLS-1$
                        });
            }
            return lineSeparator;
        }

        /*
         * Pads characters to the formatted string.
         */
        private IASString padding(IASStringBuilder source, int startIndex) {
            int start = startIndex;
            boolean paddingRight = this.formatToken
                    .isFlagSet(IASFormatter.FormatToken.FLAG_MINUS);
            char paddingChar = '\u0020';// space as padding char.
            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ZERO)) {
                if ('d' == this.formatToken.getConversionType()) {
                    paddingChar = this.getDecimalFormatSymbols().getZeroDigit();
                } else {
                    paddingChar = '0';
                }
            } else {
                // if padding char is space, always padding from the head
                // location.
                start = 0;
            }
            int width = this.formatToken.getWidth();
            int precision = this.formatToken.getPrecision();

            int length = source.length();
            if (precision >= 0) {
                length = Math.min(length, precision);
                source.delete(length, source.length());
            }
            if (width > 0) {
                width = Math.max(source.length(), width);
            }
            if (length >= width) {
                return source.toIASString();
            }

            char[] paddings = new char[width - length];
            Arrays.fill(paddings, paddingChar);
            IASString insertString = new IASString(paddings);

            if (paddingRight) {
                source.append(insertString);
            } else {
                source.insert(start, insertString);
            }
            return source.toIASString();
        }

        /*
         * Transforms the Integer to a formatted string.
         */
        private IASString transformFromInteger() {
            int startIndex = 0;
            boolean isNegative = false;
            IASStringBuilder result = new IASStringBuilder();
            char currentConversionType = this.formatToken.getConversionType();
            long value;

            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_MINUS)
                    || this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ZERO)) {
                if (!this.formatToken.isWidthSet()) {
                    throw new MissingFormatWidthException(this.formatToken
                            .getStrFlags().getString());
                }
            }
            // Combination of '+' & ' ' is illegal.
            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ADD)
                    && this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_SPACE)) {
                throw new IllegalFormatFlagsException(this.formatToken.getStrFlags().getString());
            }
            if (this.formatToken.isPrecisionSet()) {
                throw new IllegalFormatPrecisionException(this.formatToken
                        .getPrecision());
            }
            if (this.arg instanceof Long) {
                value = ((Long) this.arg).longValue();
            } else if (this.arg instanceof Integer) {
                value = ((Integer) this.arg).longValue();
            } else if (this.arg instanceof Short) {
                value = ((Short) this.arg).longValue();
            } else if (this.arg instanceof Byte) {
                value = ((Byte) this.arg).longValue();
            } else {
                throw new IllegalFormatConversionException(this.formatToken
                        .getConversionType(), this.arg.getClass());
            }
            if ('d' != currentConversionType) {
                if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ADD)
                        || this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_SPACE)
                        || this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_COMMA)
                        || this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_PARENTHESIS)) {
                    throw new FormatFlagsConversionMismatchException(
                            this.formatToken.getStrFlags().getString(), this.formatToken
                            .getConversionType());
                }
            }

            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_SHARP)) {
                if ('d' == currentConversionType) {
                    throw new FormatFlagsConversionMismatchException(
                            this.formatToken.getStrFlags().getString(), this.formatToken
                            .getConversionType());
                } else if ('o' == currentConversionType) {
                    result.append("0"); //$NON-NLS-1$
                    startIndex += 1;
                } else {
                    result.append("0x"); //$NON-NLS-1$
                    startIndex += 2;
                }
            }

            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_MINUS)
                    && this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ZERO)) {
                throw new IllegalFormatFlagsException(this.formatToken.getStrFlags().getString());
            }

            if (value < 0) {
                isNegative = true;
            }

            if ('d' == currentConversionType) {
                NumberFormat numberFormat = this.getNumberFormat();
                numberFormat.setGroupingUsed(this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_COMMA));
                result.append(numberFormat.format(this.arg));
            } else {
                long BYTE_MASK = 0x00000000000000FFL;
                long SHORT_MASK = 0x000000000000FFFFL;
                long INT_MASK = 0x00000000FFFFFFFFL;
                if (isNegative) {
                    if (this.arg instanceof Byte) {
                        value &= BYTE_MASK;
                    } else if (this.arg instanceof Short) {
                        value &= SHORT_MASK;
                    } else if (this.arg instanceof Integer) {
                        value &= INT_MASK;
                    }
                }
                if ('o' == currentConversionType) {
                    result.append(Long.toOctalString(value));
                } else {
                    result.append(Long.toHexString(value));
                }
                isNegative = false;
            }

            if (!isNegative) {
                if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ADD)) {
                    result.insert(0, '+');
                    startIndex += 1;
                }
                if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_SPACE)) {
                    result.insert(0, ' ');
                    startIndex += 1;
                }
            }

            /* pad paddingChar to the output */
            if (isNegative
                    && this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_PARENTHESIS)) {
                result = this.wrapParentheses(result);
                return result.toIASString();

            }
            if (isNegative && this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ZERO)) {
                startIndex++;
            }
            return this.padding(result, startIndex);
        }

        /*
         * add () to the output,if the value is negative and
         * formatToken.FLAG_PARENTHESIS is set. 'result' is used as an in-out
         * parameter.
         */
        private IASStringBuilder wrapParentheses(IASStringBuilder result) {
            // delete the '-'
            result.deleteCharAt(0);
            result.insert(0, '(');
            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ZERO)) {
                this.formatToken.setWidth(this.formatToken.getWidth() - 1);
                this.padding(result, 1);
                result.append(')');
            } else {
                result.append(')');
                this.padding(result, 0);
            }
            return result;
        }

        private IASString transformFromSpecialNumber() {
            IASString source = null;

            if (!(this.arg instanceof Number) || this.arg instanceof BigDecimal) {
                return null;
            }

            Number number = (Number) this.arg;
            double d = number.doubleValue();
            if (Double.isNaN(d)) {
                source = IASString.valueOf("NaN"); //$NON-NLS-1$
            } else if (Double.isInfinite(d)) {
                if (d >= 0) {
                    if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ADD)) {
                        source = IASString.valueOf("+Infinity"); //$NON-NLS-1$
                    } else if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_SPACE)) {
                        source = IASString.valueOf(" Infinity"); //$NON-NLS-1$
                    } else {
                        source = IASString.valueOf("Infinity"); //$NON-NLS-1$
                    }
                } else {
                    if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_PARENTHESIS)) {
                        source = IASString.valueOf("(Infinity)"); //$NON-NLS-1$
                    } else {
                        source = IASString.valueOf("-Infinity"); //$NON-NLS-1$
                    }
                }
            }

            if (null != source) {
                this.formatToken.setPrecision(IASFormatter.FormatToken.UNSET);
                this.formatToken.setFlags(this.formatToken.getFlags()
                        & (~IASFormatter.FormatToken.FLAG_ZERO));
                source = this.padding(new IASStringBuilder(source), 0);
            }
            return source;
        }

        private IASString transformFromNull() {
            this.formatToken.setFlags(this.formatToken.getFlags()
                    & (~IASFormatter.FormatToken.FLAG_ZERO));
            return this.padding(new IASStringBuilder("null"), 0); //$NON-NLS-1$
        }

        /*
         * Transforms a BigInteger to a formatted string.
         */
        private IASString transformFromBigInteger() {
            int startIndex = 0;
            boolean isNegative = false;
            IASStringBuilder result = new IASStringBuilder();
            BigInteger bigInt = (BigInteger) this.arg;
            char currentConversionType = this.formatToken.getConversionType();

            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_MINUS)
                    || this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ZERO)) {
                if (!this.formatToken.isWidthSet()) {
                    throw new MissingFormatWidthException(this.formatToken
                            .getStrFlags().getString());
                }
            }

            // Combination of '+' & ' ' is illegal.
            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ADD)
                    && this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_SPACE)) {
                throw new IllegalFormatFlagsException(this.formatToken.getStrFlags().getString());
            }

            // Combination of '-' & '0' is illegal.
            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ZERO)
                    && this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_MINUS)) {
                throw new IllegalFormatFlagsException(this.formatToken.getStrFlags().getString());
            }

            if (this.formatToken.isPrecisionSet()) {
                throw new IllegalFormatPrecisionException(this.formatToken
                        .getPrecision());
            }

            if ('d' != currentConversionType
                    && this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_COMMA)) {
                throw new FormatFlagsConversionMismatchException(this.formatToken
                        .getStrFlags().getString(), currentConversionType);
            }

            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_SHARP)
                    && 'd' == currentConversionType) {
                throw new FormatFlagsConversionMismatchException(this.formatToken
                        .getStrFlags().getString(), 'd');
            }

            if (null == bigInt) {
                return this.transformFromNull();
            }

            isNegative = (bigInt.compareTo(BigInteger.ZERO) < 0);

            if ('d' == currentConversionType) {
                NumberFormat numberFormat = this.getNumberFormat();
                boolean readableName = this.formatToken
                        .isFlagSet(IASFormatter.FormatToken.FLAG_COMMA);
                numberFormat.setGroupingUsed(readableName);
                result.append(numberFormat.format(bigInt));
            } else if ('o' == currentConversionType) {
                // convert BigInteger to a string presentation using radix 8
                result.append(bigInt.toString(8));
            } else {
                // convert BigInteger to a string presentation using radix 16
                result.append(bigInt.toString(16));
            }
            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_SHARP)) {
                startIndex = isNegative ? 1 : 0;
                if ('o' == currentConversionType) {
                    result.insert(startIndex, "0"); //$NON-NLS-1$
                    startIndex += 1;
                } else if ('x' == currentConversionType
                        || 'X' == currentConversionType) {
                    result.insert(startIndex, "0x"); //$NON-NLS-1$
                    startIndex += 2;
                }
            }

            if (!isNegative) {
                if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ADD)) {
                    result.insert(0, '+');
                    startIndex += 1;
                }
                if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_SPACE)) {
                    result.insert(0, ' ');
                    startIndex += 1;
                }
            }

            /* pad paddingChar to the output */
            if (isNegative
                    && this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_PARENTHESIS)) {
                result = this.wrapParentheses(result);
                return result.toIASString();

            }
            if (isNegative && this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ZERO)) {
                startIndex++;
            }
            return this.padding(result, startIndex);
        }

        /*
         * Transforms a Float,Double or BigDecimal to a formatted string.
         */
        private IASString transformFromFloat() {
            IASStringBuilder result = new IASStringBuilder();
            int startIndex = 0;
            char currentConversionType = this.formatToken.getConversionType();

            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_MINUS
                    | IASFormatter.FormatToken.FLAG_ZERO)) {
                if (!this.formatToken.isWidthSet()) {
                    throw new MissingFormatWidthException(this.formatToken
                            .getStrFlags().getString());
                }
            }

            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ADD)
                    && this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_SPACE)) {
                throw new IllegalFormatFlagsException(this.formatToken.getStrFlags().getString());
            }

            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_MINUS)
                    && this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ZERO)) {
                throw new IllegalFormatFlagsException(this.formatToken.getStrFlags().getString());
            }

            if ('e' == Character.toLowerCase(currentConversionType)) {
                if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_COMMA)) {
                    throw new FormatFlagsConversionMismatchException(
                            this.formatToken.getStrFlags().getString(), currentConversionType);
                }
            }

            if ('g' == Character.toLowerCase(currentConversionType)) {
                if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_SHARP)) {
                    throw new FormatFlagsConversionMismatchException(
                            this.formatToken.getStrFlags().getString(), currentConversionType);
                }
            }

            if ('a' == Character.toLowerCase(currentConversionType)) {
                if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_COMMA)
                        || this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_PARENTHESIS)) {
                    throw new FormatFlagsConversionMismatchException(
                            this.formatToken.getStrFlags().getString(), currentConversionType);
                }
            }

            if (null == this.arg) {
                return this.transformFromNull();
            }

            if (!(this.arg instanceof Float || this.arg instanceof Double || this.arg instanceof BigDecimal)) {
                throw new IllegalFormatConversionException(
                        currentConversionType, this.arg.getClass());
            }

            IASString specialNumberResult = this.transformFromSpecialNumber();
            if (null != specialNumberResult) {
                return specialNumberResult;
            }

            if ('a' != Character.toLowerCase(currentConversionType)) {
                this.formatToken
                        .setPrecision(this.formatToken.isPrecisionSet() ? this.formatToken
                                .getPrecision()
                                : IASFormatter.FormatToken.DEFAULT_PRECISION);
            }
            // output result
            IASFormatter.FloatUtil floatUtil = new IASFormatter.FloatUtil(result, this.formatToken,
                    (DecimalFormat) NumberFormat.getInstance(this.locale), this.arg);
            floatUtil.transform(this.formatToken, result);

            this.formatToken.setPrecision(IASFormatter.FormatToken.UNSET);

            if (this.getDecimalFormatSymbols().getMinusSign() == result.charAt(0)) {
                if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_PARENTHESIS)) {
                    result = this.wrapParentheses(result);
                    return result.toIASString();
                }
            } else {
                if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_SPACE)) {
                    result.insert(0, ' ');
                    startIndex++;
                }
                if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ADD)) {
                    result.insert(0, floatUtil.getAddSign());
                    startIndex++;
                }
            }

            char firstChar = result.charAt(0);
            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_ZERO)
                    && (firstChar == floatUtil.getAddSign() || firstChar == floatUtil
                    .getMinusSign())) {
                startIndex = 1;
            }

            if ('a' == Character.toLowerCase(currentConversionType)) {
                startIndex += 2;
            }
            return this.padding(result, startIndex);
        }

        /*
         * Transforms a Date to a formatted string.
         */
        private IASString transformFromDateTime() {
            int startIndex = 0;
            char currentConversionType = this.formatToken.getConversionType();

            if (this.formatToken.isPrecisionSet()) {
                throw new IllegalFormatPrecisionException(this.formatToken
                        .getPrecision());
            }

            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_SHARP)) {
                throw new FormatFlagsConversionMismatchException(this.formatToken
                        .getStrFlags().getString(), currentConversionType);
            }

            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_MINUS)
                    && IASFormatter.FormatToken.UNSET == this.formatToken.getWidth()) {
                throw new MissingFormatWidthException("-" //$NON-NLS-1$
                        + currentConversionType);
            }

            if (null == this.arg) {
                return this.transformFromNull();
            }

            Calendar calendar;
            if (this.arg instanceof Calendar) {
                calendar = (Calendar) this.arg;
            } else {
                Date date = null;
                if (this.arg instanceof Long) {
                    date = new Date(((Long) this.arg).longValue());
                } else if (this.arg instanceof Date) {
                    date = (Date) this.arg;
                } else {
                    throw new IllegalFormatConversionException(
                            currentConversionType, this.arg.getClass());
                }
                calendar = Calendar.getInstance(this.locale);
                calendar.setTime(date);
            }

            if (null == this.dateTimeUtil) {
                this.dateTimeUtil = new IASFormatter.DateTimeUtil(this.locale);
            }
            IASStringBuilder result = new IASStringBuilder();
            // output result
            this.dateTimeUtil.transform(this.formatToken, calendar, result);
            return this.padding(result, startIndex);
        }
    }

    private static class FloatUtil {
        private IASStringBuilder result;

        private final DecimalFormat decimalFormat;

        private IASFormatter.FormatToken formatToken;

        private final Object argument;

        private final char minusSign;

        FloatUtil(IASStringBuilder result, IASFormatter.FormatToken formatToken,
                  DecimalFormat decimalFormat, Object argument) {
            this.result = result;
            this.formatToken = formatToken;
            this.decimalFormat = decimalFormat;
            this.argument = argument;
            this.minusSign = decimalFormat.getDecimalFormatSymbols()
                    .getMinusSign();
        }

        void transform(IASFormatter.FormatToken aFormatToken, IASStringBuilder aResult) {
            this.result = aResult;
            this.formatToken = aFormatToken;
            switch (this.formatToken.getConversionType()) {
                case 'e':
                case 'E': {
                    this.transform_e();
                    break;
                }
                case 'f': {
                    this.transform_f();
                    break;
                }
                case 'g':
                case 'G': {
                    this.transform_g();
                    break;
                }
                case 'a':
                case 'A': {
                    this.transform_a();
                    break;
                }
                default: {
                    throw new UnknownFormatConversionException(String
                            .valueOf(this.formatToken.getConversionType()));
                }
            }
        }

        char getMinusSign() {
            return this.minusSign;
        }

        char getAddSign() {
            return '+';
        }

        void transform_e() {
            IASStringBuilder pattern = new IASStringBuilder();
            pattern.append('0');
            if (this.formatToken.getPrecision() > 0) {
                pattern.append('.');
                char[] zeros = new char[this.formatToken.getPrecision()];
                Arrays.fill(zeros, '0');
                pattern.append(zeros);
            }
            pattern.append('E');
            pattern.append("+00"); //$NON-NLS-1$
            this.decimalFormat.applyPattern(pattern.toString());
            IASString formattedString = IASString.valueOf(this.decimalFormat.format(this.argument));
            this.result.append(formattedString.replace('E', 'e'));

            // if the flag is sharp and decimal seperator is always given
            // out.
            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_SHARP)
                    && 0 == this.formatToken.getPrecision()) {
                int indexOfE = this.result.indexOf(IASString.fromString("e")); //$NON-NLS-1$
                char dot = this.decimalFormat.getDecimalFormatSymbols()
                        .getDecimalSeparator();
                this.result.insert(indexOfE, dot);
            }
        }

        void transform_g() {
            int precision = this.formatToken.getPrecision();
            precision = (0 == precision ? 1 : precision);
            this.formatToken.setPrecision(precision);

            if (0.0 == ((Number) this.argument).doubleValue()) {
                precision--;
                this.formatToken.setPrecision(precision);
                this.transform_f();
                return;
            }

            boolean requireScientificRepresentation = true;
            double d = ((Number) this.argument).doubleValue();
            d = Math.abs(d);
            if (Double.isInfinite(d)) {
                precision = this.formatToken.getPrecision();
                precision--;
                this.formatToken.setPrecision(precision);
                this.transform_e();
                return;
            }
            BigDecimal b = new BigDecimal(d, new MathContext(precision));
            d = b.doubleValue();
            long l = b.longValue();

            if (d >= 1 && d < Math.pow(10, precision)) {
                if (l < Math.pow(10, precision)) {
                    requireScientificRepresentation = false;
                    precision -= IASString.valueOf(l).length();
                    precision = Math.max(precision, 0);
                    l = Math.round(d * Math.pow(10, precision + 1));
                    if (IASString.valueOf(l).length() <= this.formatToken
                            .getPrecision()) {
                        precision++;
                    }
                    this.formatToken.setPrecision(precision);
                }

            } else {
                l = b.movePointRight(4).longValue();
                if (d >= Math.pow(10, -4) && d < 1) {
                    requireScientificRepresentation = false;
                    precision += 4 - IASString.valueOf(l).length();
                    l = b.movePointRight(precision + 1).longValue();
                    if (IASString.valueOf(l).length() <= this.formatToken
                            .getPrecision()) {
                        precision++;
                    }
                    l = b.movePointRight(precision).longValue();
                    if (l >= Math.pow(10, precision - 4)) {
                        this.formatToken.setPrecision(precision);
                    }
                }
            }
            if (requireScientificRepresentation) {
                precision = this.formatToken.getPrecision();
                precision--;
                this.formatToken.setPrecision(precision);
                this.transform_e();
            } else {
                this.transform_f();
            }

        }

        void transform_f() {
            IASStringBuilder pattern = new IASStringBuilder();
            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_COMMA)) {
                pattern.append(',');
                int groupingSize = this.decimalFormat.getGroupingSize();
                if (groupingSize > 1) {
                    char[] sharps = new char[groupingSize - 1];
                    Arrays.fill(sharps, '#');
                    pattern.append(sharps);
                }
            }

            pattern.append(0);

            if (this.formatToken.getPrecision() > 0) {
                pattern.append('.');
                char[] zeros = new char[this.formatToken.getPrecision()];
                Arrays.fill(zeros, '0');
                pattern.append(zeros);
            }
            this.decimalFormat.applyPattern(pattern.toString());
            this.result.append(this.decimalFormat.format(this.argument));
            // if the flag is sharp and decimal seperator is always given
            // out.
            if (this.formatToken.isFlagSet(IASFormatter.FormatToken.FLAG_SHARP)
                    && 0 == this.formatToken.getPrecision()) {
                char dot = this.decimalFormat.getDecimalFormatSymbols()
                        .getDecimalSeparator();
                this.result.append(dot);
            }

        }

        void transform_a() {
            char currentConversionType = this.formatToken.getConversionType();

            if (this.argument instanceof Float) {
                Float F = (Float) this.argument;
                this.result.append(Float.toHexString(F.floatValue()));

            } else if (this.argument instanceof Double) {
                Double D = (Double) this.argument;
                this.result.append(Double.toHexString(D.doubleValue()));
            } else {
                // BigInteger is not supported.
                throw new IllegalFormatConversionException(
                        currentConversionType, this.argument.getClass());
            }

            if (!this.formatToken.isPrecisionSet()) {
                return;
            }

            int precision = this.formatToken.getPrecision();
            precision = (0 == precision ? 1 : precision);
            int indexOfFirstFracitoanlDigit = this.result.indexOf(IASString.fromString(".")) + 1; //$NON-NLS-1$
            int indexOfP = this.result.indexOf(IASString.fromString("p")); //$NON-NLS-1$
            int fractionalLength = indexOfP - indexOfFirstFracitoanlDigit;

            if (fractionalLength == precision) {
                return;
            }

            if (fractionalLength < precision) {
                char[] zeros = new char[precision - fractionalLength];
                Arrays.fill(zeros, '0');
                this.result.insert(indexOfP, zeros);
                return;
            }
            this.result.delete(indexOfFirstFracitoanlDigit + precision, indexOfP);
        }
    }/*
     * Information about the format string of a specified argument, which
     * includes the conversion type, flags, width, precision and the argument
     * index as well as the plainText that contains the whole format string used
     * as the result for output if necessary. Besides, the string for flags is
     * recorded to construct corresponding FormatExceptions if necessary.
     */

    private static class FormatToken {

        static final int LAST_ARGUMENT_INDEX = -2;

        static final int UNSET = -1;

        static final int FLAGS_UNSET = 0;

        static final int DEFAULT_PRECISION = 6;

        static final int FLAG_MINUS = 1;

        static final int FLAG_SHARP = 1 << 1;

        static final int FLAG_ADD = 1 << 2;

        static final int FLAG_SPACE = 1 << 3;

        static final int FLAG_ZERO = 1 << 4;

        static final int FLAG_COMMA = 1 << 5;

        static final int FLAG_PARENTHESIS = 1 << 6;

        private static final int FLAGT_TYPE_COUNT = 6;

        private int formatStringStartIndex;

        private IASString plainText;

        private int argIndex = UNSET;

        private int flags = 0;

        private int width = UNSET;

        private int precision = UNSET;

        private final IASStringBuilder strFlags = new IASStringBuilder(FLAGT_TYPE_COUNT);

        private char dateSuffix;// will be used in new feature.

        private char conversionType = (char) UNSET;

        boolean isPrecisionSet() {
            return this.precision != UNSET;
        }

        boolean isWidthSet() {
            return this.width != UNSET;
        }

        boolean isFlagSet(int flag) {
            return 0 != (this.flags & flag);
        }

        int getArgIndex() {
            return this.argIndex;
        }

        void setArgIndex(int index) {
            this.argIndex = index;
        }

        IASString getPlainText() {
            return this.plainText;
        }

        void setPlainText(IASString plainText) {
            this.plainText = plainText;
        }

        int getWidth() {
            return this.width;
        }

        void setWidth(int width) {
            this.width = width;
        }

        int getPrecision() {
            return this.precision;
        }

        void setPrecision(int precise) {
            this.precision = precise;
        }

        IASString getStrFlags() {
            return this.strFlags.toIASString();
        }

        int getFlags() {
            return this.flags;
        }

        void setFlags(int flags) {
            this.flags = flags;
        }

        /*
         * Sets qualified char as one of the flags. If the char is qualified,
         * sets it as a flag and returns true. Or else returns false.
         */
        boolean setFlag(char c) {
            int newFlag;
            switch (c) {
                case '-': {
                    newFlag = FLAG_MINUS;
                    break;
                }
                case '#': {
                    newFlag = FLAG_SHARP;
                    break;
                }
                case '+': {
                    newFlag = FLAG_ADD;
                    break;
                }
                case ' ': {
                    newFlag = FLAG_SPACE;
                    break;
                }
                case '0': {
                    newFlag = FLAG_ZERO;
                    break;
                }
                case ',': {
                    newFlag = FLAG_COMMA;
                    break;
                }
                case '(': {
                    newFlag = FLAG_PARENTHESIS;
                    break;
                }
                default:
                    return false;
            }
            if (0 != (this.flags & newFlag)) {
                throw new DuplicateFormatFlagsException(String.valueOf(c));
            }
            this.flags |= newFlag;
            this.strFlags.append(c);
            return true;

        }

        int getFormatStringStartIndex() {
            return this.formatStringStartIndex;
        }

        void setFormatStringStartIndex(int index) {
            this.formatStringStartIndex = index;
        }

        char getConversionType() {
            return this.conversionType;
        }

        void setConversionType(char c) {
            this.conversionType = c;
        }

        char getDateSuffix() {
            return this.dateSuffix;
        }

        void setDateSuffix(char c) {
            this.dateSuffix = c;
        }

        boolean requireArgument() {
            return this.conversionType != '%' && this.conversionType != 'n';
        }
    }

    private Object getArgument(Object[] args, int index, IASFormatter.FormatToken token,
                               Object lastArgument, boolean hasLastArgumentSet) {
        if (index == IASFormatter.FormatToken.LAST_ARGUMENT_INDEX && !hasLastArgumentSet) {
            throw new MissingFormatArgumentException("<"); //$NON-NLS-1$
        }

        if (null == args) {
            return null;
        }

        if (index >= args.length) {
            throw new MissingFormatArgumentException(token.getPlainText().getString());
        }

        if (index == IASFormatter.FormatToken.LAST_ARGUMENT_INDEX) {
            return lastArgument;
        }

        return args[index];
    }
}

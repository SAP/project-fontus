package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;

public abstract class IASAbstractFormatter implements Closeable, Flushable, AutoCloseable {
    protected final Appendable output;
    private final Locale locale;
    private IOException lastIOException;
    private boolean closed;
    private final IASFactory factory;

    public IASAbstractFormatter(Appendable a, Locale l, IASFactory factory) {
        this.output = a;
        this.locale = l;
        this.factory = factory;
    }

    public IASAbstractFormatter format(IASStringable format, Object... args) {
        return format(this.locale, format, args);
    }

    public IASAbstractFormatter format(Locale l, IASStringable format, Object... args) {
        checkClosed();
        IASAbstractCharBuffer formatBuffer = new IASAbstractCharBuffer(format, factory);
        ParserStateMachine parser = new ParserStateMachine(formatBuffer);
        Transformer transformer = new Transformer(this, l);

        int currentObjectIndex = 0;
        Object lastArgument = null;
        boolean hasLastArgumentSet = false;
        while (formatBuffer.hasRemaining()) {
            parser.reset();
            FormatToken token = parser.getNextFormatToken();
            IASStringable result;
            IASStringable plainText = token.getPlainText();
            if (token.getConversionType() == (char) FormatToken.UNSET) {
                result = plainText;
            } else {
                plainText = plainText.substring(0, plainText.indexOf('%'));
                Object argument = null;
                if (token.requireArgument()) {
                    int index = token.getArgIndex() == FormatToken.UNSET ? currentObjectIndex++
                            : token.getArgIndex();
                    argument = getArgument(args, index, token, lastArgument,
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
                    output.append(result);
                } catch (IOException e) {
                    lastIOException = e;
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
        return lastIOException;
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

    public Formatter getFormatter() {
        return new Formatter(this.output, this.locale);
    }

    private class ParserStateMachine {

        private static final char EOS = (char) -1;

        private static final int EXIT_STATE = 0;

        private static final int ENTRY_STATE = 1;

        private static final int START_CONVERSION_STATE = 2;

        private static final int FLAGS_STATE = 3;

        private static final int WIDTH_STATE = 4;

        private static final int PRECISION_STATE = 5;

        private static final int CONVERSION_TYPE_STATE = 6;

        private static final int SUFFIX_STATE = 7;

        private FormatToken token;

        private int state = ENTRY_STATE;

        private char currentChar = 0;

        private IASAbstractCharBuffer format = null;

        ParserStateMachine(IASAbstractCharBuffer format) {
            this.format = format;
        }

        void reset() {
            this.currentChar = (char) FormatToken.UNSET;
            this.state = ENTRY_STATE;
            this.token = null;
        }

        /*
         * Gets the information about the current format token. Information is
         * recorded in the FormatToken returned and the position of the stream
         * for the format string will be advanced till the next format token.
         */
        FormatToken getNextFormatToken() {
            token = new FormatToken();
            token.setFormatStringStartIndex(format.position());

            // FINITE AUTOMATIC MACHINE
            while (true) {

                if (ParserStateMachine.EXIT_STATE != state) {
                    // exit state does not need to get next char
                    currentChar = getNextFormatChar();
                    if (EOS == currentChar
                            && ParserStateMachine.ENTRY_STATE != state) {
                        throw new UnknownFormatConversionException(getFormatString().toString());
                    }
                }

                switch (state) {
                    // exit state
                    case ParserStateMachine.EXIT_STATE: {
                        process_EXIT_STATE();
                        return token;
                    }
                    // plain text state, not yet applied converter
                    case ParserStateMachine.ENTRY_STATE: {
                        process_ENTRY_STATE();
                        break;
                    }
                    // begins converted string
                    case ParserStateMachine.START_CONVERSION_STATE: {
                        process_START_CONVERSION_STATE();
                        break;
                    }
                    case ParserStateMachine.FLAGS_STATE: {
                        process_FlAGS_STATE();
                        break;
                    }
                    case ParserStateMachine.WIDTH_STATE: {
                        process_WIDTH_STATE();
                        break;
                    }
                    case ParserStateMachine.PRECISION_STATE: {
                        process_PRECISION_STATE();
                        break;
                    }
                    case ParserStateMachine.CONVERSION_TYPE_STATE: {
                        process_CONVERSION_TYPE_STATE();
                        break;
                    }
                    case ParserStateMachine.SUFFIX_STATE: {
                        process_SUFFIX_STATE();
                        break;
                    }
                }
            }
        }

        /*
         * Gets next char from the format string.
         */
        private char getNextFormatChar() {
            if (format.hasRemaining()) {
                return format.get();
            }
            return EOS;
        }

        private IASStringable getFormatString() {
            int end = format.position();
            format.rewind();
            IASStringable formatString = format.subSequence(
                    token.getFormatStringStartIndex(), end);
            format.position(end);
            return formatString;
        }

        private void process_ENTRY_STATE() {
            if (EOS == currentChar) {
                state = ParserStateMachine.EXIT_STATE;
            } else if ('%' == currentChar) {
                // change to conversion type state
                state = START_CONVERSION_STATE;
            }
            // else remains in ENTRY_STATE
        }

        private void process_START_CONVERSION_STATE() {
            if (Character.isDigit(currentChar)) {
                int position = format.position() - 1;
                int number = parseInt(format);
                char nextChar = (char) 0;
                if (format.hasRemaining()) {
                    nextChar = format.get();
                }
                if ('$' == nextChar) {
                    // the digital sequence stands for the argument
                    // index.
                    int argIndex = number;
                    // k$ stands for the argument whose index is k-1 except that
                    // 0$ and 1$ both stands for the first element.
                    if (argIndex > 0) {
                        token.setArgIndex(argIndex - 1);
                    } else if (argIndex == FormatToken.UNSET) {
                        throw new MissingFormatArgumentException(
                                getFormatString().toString());
                    }
                    state = FLAGS_STATE;
                } else {
                    // the digital zero stands for one format flag.
                    if ('0' == currentChar) {
                        state = FLAGS_STATE;
                        format.position(position);
                    } else {
                        // the digital sequence stands for the width.
                        state = WIDTH_STATE;
                        // do not get the next char.
                        format.position(format.position() - 1);
                        token.setWidth(number);
                    }
                }
                currentChar = nextChar;
            } else if ('<' == currentChar) {
                state = FLAGS_STATE;
                token.setArgIndex(FormatToken.LAST_ARGUMENT_INDEX);
            } else {
                state = FLAGS_STATE;
                // do not get the next char.
                format.position(format.position() - 1);
            }

        }

        private void process_FlAGS_STATE() {
            if (token.setFlag(currentChar)) {
                // remains in FLAGS_STATE
            } else if (Character.isDigit(currentChar)) {
                token.setWidth(parseInt(format));
                state = WIDTH_STATE;
            } else if ('.' == currentChar) {
                state = PRECISION_STATE;
            } else {
                state = CONVERSION_TYPE_STATE;
                // do not get the next char.
                format.position(format.position() - 1);
            }
        }

        private void process_WIDTH_STATE() {
            if ('.' == currentChar) {
                state = PRECISION_STATE;
            } else {
                state = CONVERSION_TYPE_STATE;
                // do not get the next char.
                format.position(format.position() - 1);
            }
        }

        private void process_PRECISION_STATE() {
            if (Character.isDigit(currentChar)) {
                token.setPrecision(parseInt(format));
            } else {
                // the precision is required but not given by the
                // format string.
                throw new UnknownFormatConversionException(getFormatString().toString());
            }
            state = CONVERSION_TYPE_STATE;
        }

        private void process_CONVERSION_TYPE_STATE() {
            token.setConversionType(currentChar);
            if ('t' == currentChar || 'T' == currentChar) {
                state = SUFFIX_STATE;
            } else {
                state = EXIT_STATE;
            }

        }

        private void process_SUFFIX_STATE() {
            token.setDateSuffix(currentChar);
            state = EXIT_STATE;
        }

        private void process_EXIT_STATE() {
            token.setPlainText(getFormatString());
        }

        /*
         * Parses integer value from the given buffer
         */
        private int parseInt(IASAbstractCharBuffer buffer) {
            int start = buffer.position() - 1;
            int end = buffer.limit();
            while (buffer.hasRemaining()) {
                if (!Character.isDigit(buffer.get())) {
                    end = buffer.position() - 1;
                    break;
                }
            }
            buffer.position(0);
            IASStringable intStr = buffer.subSequence(start, end);
            buffer.position(end);
            try {
                return Integer.parseInt(intStr.getString());
            } catch (NumberFormatException e) {
                return FormatToken.UNSET;
            }
        }
    }

    private class DateTimeUtil {
        private Calendar calendar;

        private final Locale locale;

        private IASStringBuilderable result;

        private DateFormatSymbols dateFormatSymbols;

        DateTimeUtil(Locale locale) {
            this.locale = locale;
        }

        void transform(FormatToken formatToken, Calendar aCalendar,
                       IASStringBuilderable aResult) {
            this.result = aResult;
            this.calendar = aCalendar;
            char suffix = formatToken.getDateSuffix();

            switch (suffix) {
                case 'H': {
                    transform_H();
                    break;
                }
                case 'I': {
                    transform_I();
                    break;
                }
                case 'M': {
                    transform_M();
                    break;
                }
                case 'S': {
                    transform_S();
                    break;
                }
                case 'L': {
                    transform_L();
                    break;
                }
                case 'N': {
                    transform_N();
                    break;
                }
                case 'k': {
                    transform_k();
                    break;
                }
                case 'l': {
                    transform_l();
                    break;
                }
                case 'p': {
                    transform_p(true);
                    break;
                }
                case 's': {
                    transform_s();
                    break;
                }
                case 'z': {
                    transform_z();
                    break;
                }
                case 'Z': {
                    transform_Z();
                    break;
                }
                case 'Q': {
                    transform_Q();
                    break;
                }
                case 'B': {
                    transform_B();
                    break;
                }
                case 'b':
                case 'h': {
                    transform_b();
                    break;
                }
                case 'A': {
                    transform_A();
                    break;
                }
                case 'a': {
                    transform_a();
                    break;
                }
                case 'C': {
                    transform_C();
                    break;
                }
                case 'Y': {
                    transform_Y();
                    break;
                }
                case 'y': {
                    transform_y();
                    break;
                }
                case 'j': {
                    transform_j();
                    break;
                }
                case 'm': {
                    transform_m();
                    break;
                }
                case 'd': {
                    transform_d();
                    break;
                }
                case 'e': {
                    transform_e();
                    break;
                }
                case 'R': {
                    transform_R();
                    break;
                }

                case 'T': {
                    transform_T();
                    break;
                }
                case 'r': {
                    transform_r();
                    break;
                }
                case 'D': {
                    transform_D();
                    break;
                }
                case 'F': {
                    transform_F();
                    break;
                }
                case 'c': {
                    transform_c();
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
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            result.append(day);
        }

        private void transform_d() {
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            result.append(paddingZeros(day, 2));
        }

        private void transform_m() {
            int month = calendar.get(Calendar.MONTH);
            // The returned month starts from zero, which needs to be
            // incremented by 1.
            month++;
            result.append(paddingZeros(month, 2));
        }

        private void transform_j() {
            int day = calendar.get(Calendar.DAY_OF_YEAR);
            result.append(paddingZeros(day, 3));
        }

        private void transform_y() {
            int year = calendar.get(Calendar.YEAR);
            year %= 100;
            result.append(paddingZeros(year, 2));
        }

        private void transform_Y() {
            int year = calendar.get(Calendar.YEAR);
            result.append(paddingZeros(year, 4));
        }

        private void transform_C() {
            int year = calendar.get(Calendar.YEAR);
            year /= 100;
            result.append(paddingZeros(year, 2));
        }

        private void transform_a() {
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            result.append(getDateFormatSymbols().getShortWeekdays()[day]);
        }

        private void transform_A() {
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            result.append(getDateFormatSymbols().getWeekdays()[day]);
        }

        private void transform_b() {
            int month = calendar.get(Calendar.MONTH);
            result.append(getDateFormatSymbols().getShortMonths()[month]);
        }

        private void transform_B() {
            int month = calendar.get(Calendar.MONTH);
            result.append(getDateFormatSymbols().getMonths()[month]);
        }

        private void transform_Q() {
            long milliSeconds = calendar.getTimeInMillis();
            result.append(milliSeconds);
        }

        private void transform_s() {
            long milliSeconds = calendar.getTimeInMillis();
            milliSeconds /= 1000;
            result.append(milliSeconds);
        }

        private void transform_Z() {
            TimeZone timeZone = calendar.getTimeZone();
            result.append(timeZone
                    .getDisplayName(
                            timeZone.inDaylightTime(calendar.getTime()),
                            TimeZone.SHORT, locale));
        }

        private void transform_z() {
            int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
            zoneOffset /= 3600000;
            zoneOffset *= 100;
            if (zoneOffset >= 0) {
                result.append('+');
            }
            result.append(paddingZeros(zoneOffset, 4));
        }

        private void transform_p(boolean isLowerCase) {
            int i = calendar.get(Calendar.AM_PM);
            IASStringable s = factory.createString(getDateFormatSymbols().getAmPmStrings()[i]);
            if (isLowerCase) {
                s = s.toLowerCase(locale);
            }
            result.append(s);
        }

        private void transform_N() {
            // TODO System.nanoTime();
            long nanosecond = calendar.get(Calendar.MILLISECOND) * 1000000L;
            result.append(paddingZeros(nanosecond, 9));
        }

        private void transform_L() {
            int millisecond = calendar.get(Calendar.MILLISECOND);
            result.append(paddingZeros(millisecond, 3));
        }

        private void transform_S() {
            int second = calendar.get(Calendar.SECOND);
            result.append(paddingZeros(second, 2));
        }

        private void transform_M() {
            int minute = calendar.get(Calendar.MINUTE);
            result.append(paddingZeros(minute, 2));
        }

        private void transform_l() {
            int hour = calendar.get(Calendar.HOUR);
            if (0 == hour) {
                hour = 12;
            }
            result.append(hour);
        }

        private void transform_k() {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            result.append(hour);
        }

        private void transform_I() {
            int hour = calendar.get(Calendar.HOUR);
            if (0 == hour) {
                hour = 12;
            }
            result.append(paddingZeros(hour, 2));
        }

        private void transform_H() {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            result.append(paddingZeros(hour, 2));
        }

        private void transform_R() {
            transform_H();
            result.append(':');
            transform_M();
        }

        private void transform_T() {
            transform_H();
            result.append(':');
            transform_M();
            result.append(':');
            transform_S();
        }

        private void transform_r() {
            transform_I();
            result.append(':');
            transform_M();
            result.append(':');
            transform_S();
            result.append(' ');
            transform_p(false);
        }

        private void transform_D() {
            transform_m();
            result.append('/');
            transform_d();
            result.append('/');
            transform_y();
        }

        private void transform_F() {
            transform_Y();
            result.append('-');
            transform_m();
            result.append('-');
            transform_d();
        }

        private void transform_c() {
            transform_a();
            result.append(' ');
            transform_b();
            result.append(' ');
            transform_d();
            result.append(' ');
            transform_T();
            result.append(' ');
            transform_Z();
            result.append(' ');
            transform_Y();
        }

        private IASStringable paddingZeros(long number, int length) {
            int len = length;
            IASStringBuilderable result = factory.createStringBuilder();
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
            if (null == dateFormatSymbols) {
                dateFormatSymbols = new DateFormatSymbols(locale);
            }
            return dateFormatSymbols;
        }
    }/*
     * Transforms the argument to the formatted string according to the format
     * information contained in the format token.
     */

    private class Transformer {

        private final IASAbstractFormatter formatter;

        private FormatToken formatToken;

        private Object arg;

        private final Locale locale;

        private IASStringable lineSeparator;

        private NumberFormat numberFormat;

        private DecimalFormatSymbols decimalFormatSymbols;

        private DateTimeUtil dateTimeUtil;

        Transformer(IASAbstractFormatter formatter, Locale locale) {
            this.formatter = formatter;
            this.locale = (null == locale ? Locale.US : locale);
        }

        private NumberFormat getNumberFormat() {
            if (null == numberFormat) {
                numberFormat = NumberFormat.getInstance(locale);
            }
            return numberFormat;
        }

        private DecimalFormatSymbols getDecimalFormatSymbols() {
            if (null == decimalFormatSymbols) {
                decimalFormatSymbols = new DecimalFormatSymbols(locale);
            }
            return decimalFormatSymbols;
        }

        /*
         * Gets the formatted string according to the format token and the
         * argument.
         */
        IASStringable transform(FormatToken token, Object argument) {

            /* init data member to print */
            this.formatToken = token;
            this.arg = argument;

            IASStringable result;
            switch (token.getConversionType()) {
                case 'B':
                case 'b': {
                    result = transformFromBoolean();
                    break;
                }
                case 'H':
                case 'h': {
                    result = transformFromHashCode();
                    break;
                }
                case 'S':
                case 's': {
                    result = transformFromString();
                    break;
                }
                case 'C':
                case 'c': {
                    result = transformFromCharacter();
                    break;
                }
                case 'd':
                case 'o':
                case 'x':
                case 'X': {
                    if (null == arg || arg instanceof BigInteger) {
                        result = transformFromBigInteger();
                    } else {
                        result = transformFromInteger();
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
                    result = transformFromFloat();
                    break;
                }
                case '%': {
                    result = transformFromPercent();
                    break;
                }
                case 'n': {
                    result = transformFromLineSeparator();
                    break;
                }
                case 't':
                case 'T': {
                    result = transformFromDateTime();
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
        private IASStringable transformFromBoolean() {
            IASStringBuilderable result = factory.createStringBuilder();
            int startIndex = 0;
            int flags = formatToken.getFlags();

            if (formatToken.isFlagSet(FormatToken.FLAG_MINUS)
                    && !formatToken.isWidthSet()) {
                throw new MissingFormatWidthException("-" //$NON-NLS-1$
                        + formatToken.getConversionType());
            }

            // only '-' is valid for flags
            if (FormatToken.FLAGS_UNSET != flags
                    && FormatToken.FLAG_MINUS != flags) {
                throw new FormatFlagsConversionMismatchException(formatToken
                        .getStrFlags().getString(), formatToken.getConversionType());
            }

            if (null == arg) {
                result.append("false"); //$NON-NLS-1$
            } else if (arg instanceof Boolean) {
                result.append(arg);
            } else {
                result.append("true"); //$NON-NLS-1$
            }
            return padding(result, startIndex);
        }

        /*
         * Transforms the hashcode of the argument to a formatted string.
         */
        private IASStringable transformFromHashCode() {
            IASStringBuilderable result = factory.createStringBuilder();

            int startIndex = 0;
            int flags = formatToken.getFlags();

            if (formatToken.isFlagSet(FormatToken.FLAG_MINUS)
                    && !formatToken.isWidthSet()) {
                throw new MissingFormatWidthException("-" //$NON-NLS-1$
                        + formatToken.getConversionType());
            }

            // only '-' is valid for flags
            if (FormatToken.FLAGS_UNSET != flags
                    && FormatToken.FLAG_MINUS != flags) {
                throw new FormatFlagsConversionMismatchException(formatToken
                        .getStrFlags().toString(), formatToken.getConversionType());
            }

            if (null == arg) {
                result.append("null"); //$NON-NLS-1$
            } else {
                String hexString = Integer.toHexString(arg.hashCode());
                IASStringable taintedHexString = factory.createString(hexString);
                if (arg instanceof IASTaintRangeAware && ((IASTaintAware) arg).isTainted()) {
                    List<IASTaintRange> ranges = ((IASTaintRangeAware) arg).getTaintRanges();
                    if (ranges.size() == 1) {
                        taintedHexString.setTaint(IASTaintSource.getInstanceById(ranges.get(0).getSource()));
                    } else {
                        taintedHexString.setTaint(true);
                    }
                }
                result.append(taintedHexString);
            }
            return padding(result, startIndex);
        }

        /*
         * Transforms the String to a formatted string.
         */
        private IASStringable transformFromString() {
            IASStringBuilderable result = factory.createStringBuilder();
            int startIndex = 0;
            int flags = formatToken.getFlags();

            if (formatToken.isFlagSet(FormatToken.FLAG_MINUS)
                    && !formatToken.isWidthSet()) {
                throw new MissingFormatWidthException("-" //$NON-NLS-1$
                        + formatToken.getConversionType());
            }

            if (arg instanceof Formattable) {
                int flag = 0;
                // only minus and sharp flag is valid
                if (FormatToken.FLAGS_UNSET != (flags & ~FormatToken.FLAG_MINUS & ~FormatToken.FLAG_SHARP)) {
                    throw new IllegalFormatFlagsException(formatToken
                            .getStrFlags().toString());
                }
                if (formatToken.isFlagSet(FormatToken.FLAG_MINUS)) {
                    flag |= FormattableFlags.LEFT_JUSTIFY;
                }
                if (formatToken.isFlagSet(FormatToken.FLAG_SHARP)) {
                    flag |= FormattableFlags.ALTERNATE;
                }
                if (Character.isUpperCase(formatToken.getConversionType())) {
                    flag |= FormattableFlags.UPPERCASE;
                }
                ((Formattable) arg).formatTo(formatter.getFormatter(), flag, formatToken
                        .getWidth(), formatToken.getPrecision());
                // all actions have been taken out in the
                // Formattable.formatTo, thus there is nothing to do, just
                // returns null, which tells the Parser to add nothing to the
                // output.
                return null;
            }
            // only '-' is valid for flags if the argument is not an
            // instance of Formattable
            if (FormatToken.FLAGS_UNSET != flags
                    && FormatToken.FLAG_MINUS != flags) {
                throw new FormatFlagsConversionMismatchException(formatToken
                        .getStrFlags().toString(), formatToken.getConversionType());
            }

            result.append(arg);
            return padding(result, startIndex);
        }

        /*
         * Transforms the Character to a formatted string.
         */
        private IASStringable transformFromCharacter() {
            IASStringBuilderable result = factory.createStringBuilder();

            int startIndex = 0;
            int flags = formatToken.getFlags();

            if (formatToken.isFlagSet(FormatToken.FLAG_MINUS)
                    && !formatToken.isWidthSet()) {
                throw new MissingFormatWidthException("-" //$NON-NLS-1$
                        + formatToken.getConversionType());
            }

            // only '-' is valid for flags
            if (FormatToken.FLAGS_UNSET != flags
                    && FormatToken.FLAG_MINUS != flags) {
                throw new FormatFlagsConversionMismatchException(formatToken
                        .getStrFlags().toString(), formatToken.getConversionType());
            }

            if (formatToken.isPrecisionSet()) {
                throw new IllegalFormatPrecisionException(formatToken
                        .getPrecision());
            }

            if (null == arg) {
                result.append("null"); //$NON-NLS-1$
            } else {
                if (arg instanceof Character) {
                    result.append(arg);
                } else if (arg instanceof Byte) {
                    byte b = ((Byte) arg).byteValue();
                    if (!Character.isValidCodePoint(b)) {
                        throw new IllegalFormatCodePointException(b);
                    }
                    result.append((char) b);
                } else if (arg instanceof Short) {
                    short s = ((Short) arg).shortValue();
                    if (!Character.isValidCodePoint(s)) {
                        throw new IllegalFormatCodePointException(s);
                    }
                    result.append((char) s);
                } else if (arg instanceof Integer) {
                    int codePoint = ((Integer) arg).intValue();
                    if (!Character.isValidCodePoint(codePoint)) {
                        throw new IllegalFormatCodePointException(codePoint);
                    }
                    result.append(factory.createString(new String(Character.toChars(codePoint))));
                } else {
                    // argument of other class is not acceptable.
                    throw new IllegalFormatConversionException(formatToken
                            .getConversionType(), arg.getClass());
                }
            }
            return padding(result, startIndex);
        }

        /*
         * Transforms percent to a formatted string. Only '-' is legal flag.
         * Precision is illegal.
         */
        private IASStringable transformFromPercent() {
            IASStringBuilderable result = factory.createStringBuilder().append("%"); //$NON-NLS-1$

            int startIndex = 0;
            int flags = formatToken.getFlags();

            if (formatToken.isFlagSet(FormatToken.FLAG_MINUS)
                    && !formatToken.isWidthSet()) {
                throw new MissingFormatWidthException("-" //$NON-NLS-1$
                        + formatToken.getConversionType());
            }

            if (FormatToken.FLAGS_UNSET != flags
                    && FormatToken.FLAG_MINUS != flags) {
                throw new FormatFlagsConversionMismatchException(formatToken
                        .getStrFlags().getString(), formatToken.getConversionType());
            }
            if (formatToken.isPrecisionSet()) {
                throw new IllegalFormatPrecisionException(formatToken
                        .getPrecision());
            }
            return padding(result, startIndex);
        }

        /*
         * Transforms line separator to a formatted string. Any flag, the width
         * or the precision is illegal.
         */
        private IASStringable transformFromLineSeparator() {
            if (formatToken.isPrecisionSet()) {
                throw new IllegalFormatPrecisionException(formatToken
                        .getPrecision());
            }

            if (formatToken.isWidthSet()) {
                throw new IllegalFormatWidthException(formatToken.getWidth());
            }

            int flags = formatToken.getFlags();
            if (FormatToken.FLAGS_UNSET != flags) {
                throw new IllegalFormatFlagsException(formatToken.getStrFlags().getString());
            }

            if (null == lineSeparator) {
                lineSeparator = AccessController
                        .doPrivileged((PrivilegedAction<IASStringable>) () -> {
                            return factory.createString(System.getProperty("line.separator")); //$NON-NLS-1$
                        });
            }
            return lineSeparator;
        }

        /*
         * Pads characters to the formatted string.
         */
        private IASStringable padding(IASStringBuilderable source, int startIndex) {
            int start = startIndex;
            boolean paddingRight = formatToken
                    .isFlagSet(FormatToken.FLAG_MINUS);
            char paddingChar = '\u0020';// space as padding char.
            if (formatToken.isFlagSet(FormatToken.FLAG_ZERO)) {
                if ('d' == formatToken.getConversionType()) {
                    paddingChar = getDecimalFormatSymbols().getZeroDigit();
                } else {
                    paddingChar = '0';
                }
            } else {
                // if padding char is space, always padding from the head
                // location.
                start = 0;
            }
            int width = formatToken.getWidth();
            int precision = formatToken.getPrecision();

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
            IASStringable insertString = factory.createString(new String(paddings));

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
        private IASStringable transformFromInteger() {
            int startIndex = 0;
            boolean isNegative = false;
            IASStringBuilderable result = factory.createStringBuilder();
            char currentConversionType = formatToken.getConversionType();
            long value;

            if (formatToken.isFlagSet(FormatToken.FLAG_MINUS)
                    || formatToken.isFlagSet(FormatToken.FLAG_ZERO)) {
                if (!formatToken.isWidthSet()) {
                    throw new MissingFormatWidthException(formatToken
                            .getStrFlags().getString());
                }
            }
            // Combination of '+' & ' ' is illegal.
            if (formatToken.isFlagSet(FormatToken.FLAG_ADD)
                    && formatToken.isFlagSet(FormatToken.FLAG_SPACE)) {
                throw new IllegalFormatFlagsException(formatToken.getStrFlags().getString());
            }
            if (formatToken.isPrecisionSet()) {
                throw new IllegalFormatPrecisionException(formatToken
                        .getPrecision());
            }
            if (arg instanceof Long) {
                value = ((Long) arg).longValue();
            } else if (arg instanceof Integer) {
                value = ((Integer) arg).longValue();
            } else if (arg instanceof Short) {
                value = ((Short) arg).longValue();
            } else if (arg instanceof Byte) {
                value = ((Byte) arg).longValue();
            } else {
                throw new IllegalFormatConversionException(formatToken
                        .getConversionType(), arg.getClass());
            }
            if ('d' != currentConversionType) {
                if (formatToken.isFlagSet(FormatToken.FLAG_ADD)
                        || formatToken.isFlagSet(FormatToken.FLAG_SPACE)
                        || formatToken.isFlagSet(FormatToken.FLAG_COMMA)
                        || formatToken.isFlagSet(FormatToken.FLAG_PARENTHESIS)) {
                    throw new FormatFlagsConversionMismatchException(
                            formatToken.getStrFlags().getString(), formatToken
                            .getConversionType());
                }
            }

            if (formatToken.isFlagSet(FormatToken.FLAG_SHARP)) {
                if ('d' == currentConversionType) {
                    throw new FormatFlagsConversionMismatchException(
                            formatToken.getStrFlags().getString(), formatToken
                            .getConversionType());
                } else if ('o' == currentConversionType) {
                    result.append("0"); //$NON-NLS-1$
                    startIndex += 1;
                } else {
                    result.append("0x"); //$NON-NLS-1$
                    startIndex += 2;
                }
            }

            if (formatToken.isFlagSet(FormatToken.FLAG_MINUS)
                    && formatToken.isFlagSet(FormatToken.FLAG_ZERO)) {
                throw new IllegalFormatFlagsException(formatToken.getStrFlags().getString());
            }

            if (value < 0) {
                isNegative = true;
            }

            if ('d' == currentConversionType) {
                NumberFormat numberFormat = getNumberFormat();
                numberFormat.setGroupingUsed(formatToken.isFlagSet(FormatToken.FLAG_COMMA));
                result.append(numberFormat.format(arg));
            } else {
                long BYTE_MASK = 0x00000000000000FFL;
                long SHORT_MASK = 0x000000000000FFFFL;
                long INT_MASK = 0x00000000FFFFFFFFL;
                if (isNegative) {
                    if (arg instanceof Byte) {
                        value &= BYTE_MASK;
                    } else if (arg instanceof Short) {
                        value &= SHORT_MASK;
                    } else if (arg instanceof Integer) {
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
                if (formatToken.isFlagSet(FormatToken.FLAG_ADD)) {
                    result.insert(0, '+');
                    startIndex += 1;
                }
                if (formatToken.isFlagSet(FormatToken.FLAG_SPACE)) {
                    result.insert(0, ' ');
                    startIndex += 1;
                }
            }

            /* pad paddingChar to the output */
            if (isNegative
                    && formatToken.isFlagSet(FormatToken.FLAG_PARENTHESIS)) {
                result = wrapParentheses(result);
                return result.toIASString();

            }
            if (isNegative && formatToken.isFlagSet(FormatToken.FLAG_ZERO)) {
                startIndex++;
            }
            return padding(result, startIndex);
        }

        /*
         * add () to the output,if the value is negative and
         * formatToken.FLAG_PARENTHESIS is set. 'result' is used as an in-out
         * parameter.
         */
        private IASStringBuilderable wrapParentheses(IASStringBuilderable result) {
            // delete the '-'
            result.deleteCharAt(0);
            result.insert(0, '(');
            if (formatToken.isFlagSet(FormatToken.FLAG_ZERO)) {
                formatToken.setWidth(formatToken.getWidth() - 1);
                padding(result, 1);
                result.append(')');
            } else {
                result.append(')');
                padding(result, 0);
            }
            return result;
        }

        private IASStringable transformFromSpecialNumber() {
            IASStringable source = null;

            if (!(arg instanceof Number) || arg instanceof BigDecimal) {
                return null;
            }

            Number number = (Number) arg;
            double d = number.doubleValue();
            if (Double.isNaN(d)) {
                source = factory.createString("NaN"); //$NON-NLS-1$
            } else if (Double.isInfinite(d)) {
                if (d >= 0) {
                    if (formatToken.isFlagSet(FormatToken.FLAG_ADD)) {
                        source = factory.createString("+Infinity"); //$NON-NLS-1$
                    } else if (formatToken.isFlagSet(FormatToken.FLAG_SPACE)) {
                        source = factory.createString(" Infinity"); //$NON-NLS-1$
                    } else {
                        source = factory.createString("Infinity"); //$NON-NLS-1$
                    }
                } else {
                    if (formatToken.isFlagSet(FormatToken.FLAG_PARENTHESIS)) {
                        source = factory.createString("(Infinity)"); //$NON-NLS-1$
                    } else {
                        source = factory.createString("-Infinity"); //$NON-NLS-1$
                    }
                }
            }

            if (null != source) {
                formatToken.setPrecision(FormatToken.UNSET);
                formatToken.setFlags(formatToken.getFlags()
                        & (~FormatToken.FLAG_ZERO));
                source = padding(factory.createStringBuilder().append(source), 0);
            }
            return source;
        }

        private IASStringable transformFromNull() {
            formatToken.setFlags(formatToken.getFlags()
                    & (~FormatToken.FLAG_ZERO));
            return padding(factory.createStringBuilder().append("null"), 0); //$NON-NLS-1$
        }

        /*
         * Transforms a BigInteger to a formatted string.
         */
        private IASStringable transformFromBigInteger() {
            int startIndex = 0;
            boolean isNegative = false;
            IASStringBuilderable result = factory.createStringBuilder();
            BigInteger bigInt = (BigInteger) arg;
            char currentConversionType = formatToken.getConversionType();

            if (formatToken.isFlagSet(FormatToken.FLAG_MINUS)
                    || formatToken.isFlagSet(FormatToken.FLAG_ZERO)) {
                if (!formatToken.isWidthSet()) {
                    throw new MissingFormatWidthException(formatToken
                            .getStrFlags().getString());
                }
            }

            // Combination of '+' & ' ' is illegal.
            if (formatToken.isFlagSet(FormatToken.FLAG_ADD)
                    && formatToken.isFlagSet(FormatToken.FLAG_SPACE)) {
                throw new IllegalFormatFlagsException(formatToken.getStrFlags().getString());
            }

            // Combination of '-' & '0' is illegal.
            if (formatToken.isFlagSet(FormatToken.FLAG_ZERO)
                    && formatToken.isFlagSet(FormatToken.FLAG_MINUS)) {
                throw new IllegalFormatFlagsException(formatToken.getStrFlags().getString());
            }

            if (formatToken.isPrecisionSet()) {
                throw new IllegalFormatPrecisionException(formatToken
                        .getPrecision());
            }

            if ('d' != currentConversionType
                    && formatToken.isFlagSet(FormatToken.FLAG_COMMA)) {
                throw new FormatFlagsConversionMismatchException(formatToken
                        .getStrFlags().getString(), currentConversionType);
            }

            if (formatToken.isFlagSet(FormatToken.FLAG_SHARP)
                    && 'd' == currentConversionType) {
                throw new FormatFlagsConversionMismatchException(formatToken
                        .getStrFlags().getString(), currentConversionType);
            }

            if (null == bigInt) {
                return transformFromNull();
            }

            isNegative = (bigInt.compareTo(BigInteger.ZERO) < 0);

            if ('d' == currentConversionType) {
                NumberFormat numberFormat = getNumberFormat();
                boolean readableName = formatToken
                        .isFlagSet(FormatToken.FLAG_COMMA);
                numberFormat.setGroupingUsed(readableName);
                result.append(numberFormat.format(bigInt));
            } else if ('o' == currentConversionType) {
                // convert BigInteger to a string presentation using radix 8
                result.append(bigInt.toString(8));
            } else {
                // convert BigInteger to a string presentation using radix 16
                result.append(bigInt.toString(16));
            }
            if (formatToken.isFlagSet(FormatToken.FLAG_SHARP)) {
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
                if (formatToken.isFlagSet(FormatToken.FLAG_ADD)) {
                    result.insert(0, '+');
                    startIndex += 1;
                }
                if (formatToken.isFlagSet(FormatToken.FLAG_SPACE)) {
                    result.insert(0, ' ');
                    startIndex += 1;
                }
            }

            /* pad paddingChar to the output */
            if (isNegative
                    && formatToken.isFlagSet(FormatToken.FLAG_PARENTHESIS)) {
                result = wrapParentheses(result);
                return result.toIASString();

            }
            if (isNegative && formatToken.isFlagSet(FormatToken.FLAG_ZERO)) {
                startIndex++;
            }
            return padding(result, startIndex);
        }

        /*
         * Transforms a Float,Double or BigDecimal to a formatted string.
         */
        private IASStringable transformFromFloat() {
            IASStringBuilderable result = factory.createStringBuilder();
            int startIndex = 0;
            char currentConversionType = formatToken.getConversionType();

            if (formatToken.isFlagSet(FormatToken.FLAG_MINUS
                    | FormatToken.FLAG_ZERO)) {
                if (!formatToken.isWidthSet()) {
                    throw new MissingFormatWidthException(formatToken
                            .getStrFlags().getString());
                }
            }

            if (formatToken.isFlagSet(FormatToken.FLAG_ADD)
                    && formatToken.isFlagSet(FormatToken.FLAG_SPACE)) {
                throw new IllegalFormatFlagsException(formatToken.getStrFlags().getString());
            }

            if (formatToken.isFlagSet(FormatToken.FLAG_MINUS)
                    && formatToken.isFlagSet(FormatToken.FLAG_ZERO)) {
                throw new IllegalFormatFlagsException(formatToken.getStrFlags().getString());
            }

            if ('e' == Character.toLowerCase(currentConversionType)) {
                if (formatToken.isFlagSet(FormatToken.FLAG_COMMA)) {
                    throw new FormatFlagsConversionMismatchException(
                            formatToken.getStrFlags().getString(), currentConversionType);
                }
            }

            if ('g' == Character.toLowerCase(currentConversionType)) {
                if (formatToken.isFlagSet(FormatToken.FLAG_SHARP)) {
                    throw new FormatFlagsConversionMismatchException(
                            formatToken.getStrFlags().getString(), currentConversionType);
                }
            }

            if ('a' == Character.toLowerCase(currentConversionType)) {
                if (formatToken.isFlagSet(FormatToken.FLAG_COMMA)
                        || formatToken.isFlagSet(FormatToken.FLAG_PARENTHESIS)) {
                    throw new FormatFlagsConversionMismatchException(
                            formatToken.getStrFlags().getString(), currentConversionType);
                }
            }

            if (null == arg) {
                return transformFromNull();
            }

            if (!(arg instanceof Float || arg instanceof Double || arg instanceof BigDecimal)) {
                throw new IllegalFormatConversionException(
                        currentConversionType, arg.getClass());
            }

            IASStringable specialNumberResult = transformFromSpecialNumber();
            if (null != specialNumberResult) {
                return specialNumberResult;
            }

            if ('a' != Character.toLowerCase(currentConversionType)) {
                formatToken
                        .setPrecision(formatToken.isPrecisionSet() ? formatToken
                                .getPrecision()
                                : FormatToken.DEFAULT_PRECISION);
            }
            // output result
            FloatUtil floatUtil = new FloatUtil(result, formatToken,
                    (DecimalFormat) NumberFormat.getInstance(locale), arg);
            floatUtil.transform(formatToken, result);

            formatToken.setPrecision(FormatToken.UNSET);

            if (getDecimalFormatSymbols().getMinusSign() == result.charAt(0)) {
                if (formatToken.isFlagSet(FormatToken.FLAG_PARENTHESIS)) {
                    result = wrapParentheses(result);
                    return result.toIASString();
                }
            } else {
                if (formatToken.isFlagSet(FormatToken.FLAG_SPACE)) {
                    result.insert(0, ' ');
                    startIndex++;
                }
                if (formatToken.isFlagSet(FormatToken.FLAG_ADD)) {
                    result.insert(0, floatUtil.getAddSign());
                    startIndex++;
                }
            }

            char firstChar = result.charAt(0);
            if (formatToken.isFlagSet(FormatToken.FLAG_ZERO)
                    && (firstChar == floatUtil.getAddSign() || firstChar == floatUtil
                    .getMinusSign())) {
                startIndex = 1;
            }

            if ('a' == Character.toLowerCase(currentConversionType)) {
                startIndex += 2;
            }
            return padding(result, startIndex);
        }

        /*
         * Transforms a Date to a formatted string.
         */
        private IASStringable transformFromDateTime() {
            int startIndex = 0;
            char currentConversionType = formatToken.getConversionType();

            if (formatToken.isPrecisionSet()) {
                throw new IllegalFormatPrecisionException(formatToken
                        .getPrecision());
            }

            if (formatToken.isFlagSet(FormatToken.FLAG_SHARP)) {
                throw new FormatFlagsConversionMismatchException(formatToken
                        .getStrFlags().getString(), currentConversionType);
            }

            if (formatToken.isFlagSet(FormatToken.FLAG_MINUS)
                    && FormatToken.UNSET == formatToken.getWidth()) {
                throw new MissingFormatWidthException("-" //$NON-NLS-1$
                        + currentConversionType);
            }

            if (null == arg) {
                return transformFromNull();
            }

            Calendar calendar;
            if (arg instanceof Calendar) {
                calendar = (Calendar) arg;
            } else {
                Date date = null;
                if (arg instanceof Long) {
                    date = new Date(((Long) arg).longValue());
                } else if (arg instanceof Date) {
                    date = (Date) arg;
                } else {
                    throw new IllegalFormatConversionException(
                            currentConversionType, arg.getClass());
                }
                calendar = Calendar.getInstance(locale);
                calendar.setTime(date);
            }

            if (null == dateTimeUtil) {
                dateTimeUtil = new DateTimeUtil(locale);
            }
            IASStringBuilderable result = factory.createStringBuilder();
            // output result
            dateTimeUtil.transform(formatToken, calendar, result);
            return padding(result, startIndex);
        }
    }

    private class FloatUtil {
        private IASStringBuilderable result;

        private final DecimalFormat decimalFormat;

        private FormatToken formatToken;

        private final Object argument;

        private final char minusSign;

        FloatUtil(IASStringBuilderable result, FormatToken formatToken,
                  DecimalFormat decimalFormat, Object argument) {
            this.result = result;
            this.formatToken = formatToken;
            this.decimalFormat = decimalFormat;
            this.argument = argument;
            this.minusSign = decimalFormat.getDecimalFormatSymbols()
                    .getMinusSign();
        }

        void transform(FormatToken aFormatToken, IASStringBuilderable aResult) {
            this.result = aResult;
            this.formatToken = aFormatToken;
            switch (formatToken.getConversionType()) {
                case 'e':
                case 'E': {
                    transform_e();
                    break;
                }
                case 'f': {
                    transform_f();
                    break;
                }
                case 'g':
                case 'G': {
                    transform_g();
                    break;
                }
                case 'a':
                case 'A': {
                    transform_a();
                    break;
                }
                default: {
                    throw new UnknownFormatConversionException(String
                            .valueOf(formatToken.getConversionType()));
                }
            }
        }

        char getMinusSign() {
            return minusSign;
        }

        char getAddSign() {
            return '+';
        }

        void transform_e() {
            IASStringBuilderable pattern = factory.createStringBuilder();
            pattern.append('0');
            if (formatToken.getPrecision() > 0) {
                pattern.append('.');
                char[] zeros = new char[formatToken.getPrecision()];
                Arrays.fill(zeros, '0');
                pattern.append(zeros);
            }
            pattern.append('E');
            pattern.append("+00"); //$NON-NLS-1$
            decimalFormat.applyPattern(pattern.toString());
            IASStringable formattedString = factory.createString(decimalFormat.format(argument));
            result.append(formattedString.replace('E', 'e'));

            // if the flag is sharp and decimal seperator is always given
            // out.
            if (formatToken.isFlagSet(FormatToken.FLAG_SHARP)
                    && 0 == formatToken.getPrecision()) {
                int indexOfE = result.indexOf(factory.createString("e")); //$NON-NLS-1$
                char dot = decimalFormat.getDecimalFormatSymbols()
                        .getDecimalSeparator();
                result.insert(indexOfE, dot);
            }
        }

        void transform_g() {
            int precision = formatToken.getPrecision();
            precision = (0 == precision ? 1 : precision);
            formatToken.setPrecision(precision);

            if (0.0 == ((Number) argument).doubleValue()) {
                precision--;
                formatToken.setPrecision(precision);
                transform_f();
                return;
            }

            boolean requireScientificRepresentation = true;
            double d = ((Number) argument).doubleValue();
            d = Math.abs(d);
            if (Double.isInfinite(d)) {
                precision = formatToken.getPrecision();
                precision--;
                formatToken.setPrecision(precision);
                transform_e();
                return;
            }
            BigDecimal b = new BigDecimal(d, new MathContext(precision));
            d = b.doubleValue();
            long l = b.longValue();

            if (d >= 1 && d < Math.pow(10, precision)) {
                if (l < Math.pow(10, precision)) {
                    requireScientificRepresentation = false;
                    precision -= factory.createString(String.valueOf(l)).length();
                    precision = precision < 0 ? 0 : precision;
                    l = Math.round(d * Math.pow(10, precision + 1));
                    if (factory.createString(String.valueOf(l)).length() <= formatToken
                            .getPrecision()) {
                        precision++;
                    }
                    formatToken.setPrecision(precision);
                }

            } else {
                l = b.movePointRight(4).longValue();
                if (d >= Math.pow(10, -4) && d < 1) {
                    requireScientificRepresentation = false;
                    precision += 4 - factory.createString(String.valueOf(l)).length();
                    l = b.movePointRight(precision + 1).longValue();
                    if (factory.createString(String.valueOf(l)).length() <= formatToken
                            .getPrecision()) {
                        precision++;
                    }
                    l = b.movePointRight(precision).longValue();
                    if (l >= Math.pow(10, precision - 4)) {
                        formatToken.setPrecision(precision);
                    }
                }
            }
            if (requireScientificRepresentation) {
                precision = formatToken.getPrecision();
                precision--;
                formatToken.setPrecision(precision);
                transform_e();
            } else {
                transform_f();
            }

        }

        void transform_f() {
            IASStringBuilderable pattern = factory.createStringBuilder();
            if (formatToken.isFlagSet(FormatToken.FLAG_COMMA)) {
                pattern.append(',');
                int groupingSize = decimalFormat.getGroupingSize();
                if (groupingSize > 1) {
                    char[] sharps = new char[groupingSize - 1];
                    Arrays.fill(sharps, '#');
                    pattern.append(sharps);
                }
            }

            pattern.append(0);

            if (formatToken.getPrecision() > 0) {
                pattern.append('.');
                char[] zeros = new char[formatToken.getPrecision()];
                Arrays.fill(zeros, '0');
                pattern.append(zeros);
            }
            decimalFormat.applyPattern(pattern.toString());
            result.append(decimalFormat.format(argument));
            // if the flag is sharp and decimal seperator is always given
            // out.
            if (formatToken.isFlagSet(FormatToken.FLAG_SHARP)
                    && 0 == formatToken.getPrecision()) {
                char dot = decimalFormat.getDecimalFormatSymbols()
                        .getDecimalSeparator();
                result.append(dot);
            }

        }

        void transform_a() {
            char currentConversionType = formatToken.getConversionType();

            if (argument instanceof Float) {
                Float F = (Float) argument;
                result.append(Float.toHexString(F.floatValue()));

            } else if (argument instanceof Double) {
                Double D = (Double) argument;
                result.append(Double.toHexString(D.doubleValue()));
            } else {
                // BigInteger is not supported.
                throw new IllegalFormatConversionException(
                        currentConversionType, argument.getClass());
            }

            if (!formatToken.isPrecisionSet()) {
                return;
            }

            int precision = formatToken.getPrecision();
            precision = (0 == precision ? 1 : precision);
            int indexOfFirstFracitoanlDigit = result.indexOf(factory.createString(".")) + 1; //$NON-NLS-1$
            int indexOfP = result.indexOf(factory.createString("p")); //$NON-NLS-1$
            int fractionalLength = indexOfP - indexOfFirstFracitoanlDigit;

            if (fractionalLength == precision) {
                return;
            }

            if (fractionalLength < precision) {
                char[] zeros = new char[precision - fractionalLength];
                Arrays.fill(zeros, '0');
                result.insert(indexOfP, zeros);
                return;
            }
            result.delete(indexOfFirstFracitoanlDigit + precision, indexOfP);
        }
    }/*
     * Information about the format string of a specified argument, which
     * includes the conversion type, flags, width, precision and the argument
     * index as well as the plainText that contains the whole format string used
     * as the result for output if necessary. Besides, the string for flags is
     * recorded to construct corresponding FormatExceptions if necessary.
     */

    private class FormatToken {

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

        private IASStringable plainText;

        private int argIndex = UNSET;

        private int flags = 0;

        private int width = UNSET;

        private int precision = UNSET;

        private final IASStringBuilderable strFlags = factory.createStringBuilder().append(FLAGT_TYPE_COUNT);

        private char dateSuffix;// will be used in new feature.

        private char conversionType = (char) UNSET;

        boolean isPrecisionSet() {
            return precision != UNSET;
        }

        boolean isWidthSet() {
            return width != UNSET;
        }

        boolean isFlagSet(int flag) {
            return 0 != (flags & flag);
        }

        int getArgIndex() {
            return argIndex;
        }

        void setArgIndex(int index) {
            argIndex = index;
        }

        IASStringable getPlainText() {
            return plainText;
        }

        void setPlainText(IASStringable plainText) {
            this.plainText = plainText;
        }

        int getWidth() {
            return width;
        }

        void setWidth(int width) {
            this.width = width;
        }

        int getPrecision() {
            return precision;
        }

        void setPrecision(int precise) {
            this.precision = precise;
        }

        IASStringable getStrFlags() {
            return strFlags.toIASString();
        }

        int getFlags() {
            return flags;
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
            if (0 != (flags & newFlag)) {
                throw new DuplicateFormatFlagsException(String.valueOf(c));
            }
            flags = (flags | newFlag);
            strFlags.append(c);
            return true;

        }

        int getFormatStringStartIndex() {
            return formatStringStartIndex;
        }

        void setFormatStringStartIndex(int index) {
            formatStringStartIndex = index;
        }

        char getConversionType() {
            return conversionType;
        }

        void setConversionType(char c) {
            conversionType = c;
        }

        char getDateSuffix() {
            return dateSuffix;
        }

        void setDateSuffix(char c) {
            dateSuffix = c;
        }

        boolean requireArgument() {
            return conversionType != '%' && conversionType != 'n';
        }
    }

    private Object getArgument(Object[] args, int index, FormatToken token,
                               Object lastArgument, boolean hasLastArgumentSet) {
        if (index == FormatToken.LAST_ARGUMENT_INDEX && !hasLastArgumentSet) {
            throw new MissingFormatArgumentException("<"); //$NON-NLS-1$
        }

        if (null == args) {
            return null;
        }

        if (index >= args.length) {
            throw new MissingFormatArgumentException(token.getPlainText().getString());
        }

        if (index == FormatToken.LAST_ARGUMENT_INDEX) {
            return lastArgument;
        }

        return args[index];
    }
}

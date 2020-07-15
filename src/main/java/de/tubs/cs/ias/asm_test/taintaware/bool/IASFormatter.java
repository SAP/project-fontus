package de.tubs.cs.ias.asm_test.taintaware.bool;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASFormatterable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

import java.io.*;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IASFormatter implements IASTaintAware, Closeable, Flushable, AutoCloseable, IASFormatterable {
    private final Formatter formatter;

    public IASFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    public IASFormatter() {
        this(new Formatter(new IASStringBuilder()));
    }

    public IASFormatter(Appendable a) {
        this(new Formatter(a));
    }

    public IASFormatter(Appendable a, Locale l) {
        this(new Formatter(a, l));
    }

    public IASFormatter(File file) throws FileNotFoundException {
        this(new Formatter(file));
    }

    public IASFormatter(File file, IASStringable csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(new Formatter(file, csn.getString()));
    }

    public IASFormatter(File file, IASStringable csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
        this(new Formatter(file, csn.getString(), l));
    }

    public IASFormatter(Locale l) {
        this(new Formatter(new IASStringBuilder(), l));
    }

    public IASFormatter(OutputStream o) {
        this(new Formatter(o));
    }

    public IASFormatter(OutputStream o, IASStringable csn) throws UnsupportedEncodingException {
        this(new Formatter(o, csn.getString()));
    }

    public IASFormatter(OutputStream o, IASStringable csn, Locale l) throws UnsupportedEncodingException {
        this(new Formatter(o, csn.getString(), l));
    }

    public IASFormatter(PrintStream o) {
        this(new Formatter(o));
    }

    public IASFormatter(IASStringable fileName) throws FileNotFoundException {
        this(new Formatter(fileName.getString()));
    }

    public IASFormatter(IASStringable fileName, IASStringable csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(new Formatter(fileName.getString(), csn.getString()));

    }

    public IASFormatter(IASStringable fileName, IASStringable csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
        this(new Formatter(fileName.getString(), csn.getString(), l));

    }

    public static IASFormatterable fromFormatter(Formatter param) {
        if (param == null) {
            return null;
        }
        return new IASFormatter(param);
    }

    private int countSpecifier(String format) {
        String formatSpecifier
                = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";
        Matcher m = Pattern.compile(formatSpecifier).matcher(format);
        int i = 0;
        while(m.find()) {
            i++;
        }
        return i;
    }

    public IASFormatter format(IASStringable format, Object... args) {
        boolean taintedArgs = IASString.isTainted(Arrays.copyOfRange(args, 0, this.countSpecifier(format.getString())));
        this.formatter.format(format.getString(), args);
        Appendable internal = this.formatter.out();
        if (internal instanceof IASTaintAware) {
            IASTaintAware tInternal = (IASTaintAware) internal;
            tInternal.setTaint(taintedArgs || tInternal.isTainted());
        }
        return this;
    }

    public IASFormatter format(Locale l, IASStringable format, Object... args) {
        boolean taintedArgs = IASString.isTainted(Arrays.copyOfRange(args, 0, this.countSpecifier(format.getString())));
        this.formatter.format(l, format.getString(), args);
        Appendable internal = this.formatter.out();
        if (internal instanceof IASTaintAware) {
            IASTaintAware tInternal = (IASTaintAware) internal;
            tInternal.setTaint(taintedArgs || tInternal.isTainted());
        }
        return this;
    }

    public IOException ioException() {
        return this.formatter.ioException();
    }

    public Locale locale() {
        return this.formatter.locale();
    }

    public Appendable out() {
        return this.formatter.out();
    }

    @Override
    public void close() {
        this.formatter.close();
    }

    @Override
    public void flush() {
        this.formatter.flush();
    }

    @Override
    public String toString() {
        return this.formatter.toString();
    }

    public IASString toIASString() {
        return IASString.valueOf(this.formatter.out());
    }

    public Formatter getFormatter() {
        return this.formatter;
    }

    @Override
    public boolean isTainted() {
        if (this.formatter.out() instanceof IASTaintAware) {
            return ((IASTaintAware) this.formatter.out()).isTainted();
        }
        return false;
    }

    @Override
    public void setTaint(boolean taint) {
        if (this.formatter.out() instanceof IASTaintAware) {
            ((IASTaintAware) this.formatter.out()).setTaint(taint);
        }
    }
}

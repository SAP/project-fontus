package de.tubs.cs.ias.asm_test.taintaware.bool;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

import java.io.*;
import java.util.Formatter;
import java.util.Locale;

public class IASFormatter implements IASTaintAware, Closeable, Flushable, AutoCloseable {
    private final Formatter formatter;

    public IASFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    public IASFormatter() {
        this(new Formatter());
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

    public IASFormatter(File file, IASString csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(new Formatter(file, csn.getString()));
    }

    public IASFormatter(File file, IASString csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
        this(new Formatter(file, csn.getString(), l));
    }

    public IASFormatter(Locale l) {
        this(new Formatter(l));
    }

    public IASFormatter(OutputStream o) {
        this(new Formatter(o));
    }

    public IASFormatter(OutputStream o, IASString csn) throws UnsupportedEncodingException {
        this(new Formatter(o, csn.getString()));
    }

    public IASFormatter(OutputStream o, IASString csn, Locale l) throws UnsupportedEncodingException {
        this(new Formatter(o, csn.getString(), l));
    }

    public IASFormatter(PrintStream o) {
        this(new Formatter(o));
    }

    public IASFormatter(IASString fileName) throws FileNotFoundException {
        this(new Formatter(fileName.getString()));
    }

    public IASFormatter(IASString fileName, IASString csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(new Formatter(fileName.getString(), csn.getString()));

    }

    public IASFormatter(IASString fileName, IASString csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
        this(new Formatter(fileName.getString(), csn.getString(), l));

    }

    public IASFormatter format(IASString format, Object... args) {
        boolean taintedArgs = IASString.isTainted(args);
        formatter.format(format.getString(), args);
        if (this.formatter.out() instanceof IASTaintAware) {
            ((IASTaintAware) this.formatter.out()).setTaint(taintedArgs | ((IASTaintAware) this.formatter.out()).isTainted());
        }
        return this;
    }

    public IASFormatter format(Locale l, IASString format, Object... args) {
        boolean taintedArgs = IASString.isTainted(args);
        formatter.format(l, format.getString(), args);
        if (this.formatter.out() instanceof IASTaintAware) {
            ((IASTaintAware) this.formatter.out()).setTaint(taintedArgs | ((IASTaintAware) this.formatter.out()).isTainted());
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

package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.taintaware.lazybasic.IASMatcher;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations.FormatOperation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASFormatterable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

import java.io.*;
import java.util.Formatter;
import java.util.Locale;

public class IASFormatter implements IASFormatterable {
    private final Formatter formatter;

    public IASFormatter() {
        this.formatter = new Formatter(new IASStringBuilder());
    }

    public IASFormatter(Appendable a) {
        this.formatter = new Formatter(a);
    }

    public IASFormatter(Appendable a, Locale l) {
        this.formatter = new Formatter(a, l);
    }

    public IASFormatter(File file) throws FileNotFoundException {
        this.formatter = new Formatter(file);
    }

    public IASFormatter(File file, IASStringable csn) throws FileNotFoundException, UnsupportedEncodingException {
        this.formatter = new Formatter(file, csn.getString());
    }

    public IASFormatter(File file, IASStringable csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
        this.formatter = new Formatter(file, csn.getString(), l);
    }

    public IASFormatter(Locale l) {
        this.formatter = new Formatter(new IASStringBuilder(), l);
    }

    public IASFormatter(OutputStream o) {
        this.formatter = new Formatter(o);
    }

    public IASFormatter(OutputStream o, IASStringable csn) throws UnsupportedEncodingException {
        this.formatter = new Formatter(o, csn.getString());
    }

    public IASFormatter(OutputStream o, IASStringable csn, Locale l) throws UnsupportedEncodingException {
        this.formatter = new Formatter(o, csn.getString(), l);
    }

    public IASFormatter(PrintStream o) {
        this.formatter = new Formatter(o);
    }

    public IASFormatter(IASStringable fileName) throws FileNotFoundException {
        this.formatter = new Formatter(fileName.getString());
    }

    public IASFormatter(IASStringable fileName, IASStringable csn) throws UnsupportedEncodingException, FileNotFoundException {
        this.formatter = new Formatter(fileName.getString(), csn.getString());

    }

    public IASFormatter(IASStringable fileName, IASStringable csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
        this.formatter = new Formatter(fileName.getString(), csn.getString(), l);

    }

    public IASFormatter(Formatter formatter) {
        this(formatter.out(), formatter.locale());
    }

    public static IASFormatterable fromFormatter(Formatter param) {
        if (param == null) {
            return null;
        }
        return new IASFormatter(param);
    }

    public IASFormatter format(IASStringable format, Object... args) {
        this.formatter.format(format.getString(), args);
        if (this.formatter.out() instanceof IASAbstractStringBuilder) {
            ((IASAbstractStringBuilder) this.formatter.out()).derive(new FormatOperation(this.locale(), (IASString) format, args), true);
        }
        return this;
    }

    public IASFormatter format(Locale l, IASStringable format, Object... args) {
        this.formatter.format(l, format.getString(), args);
        if (this.formatter.out() instanceof IASAbstractStringBuilder) {
            ((IASAbstractStringBuilder) this.formatter.out()).derive(new FormatOperation(l, (IASString) format, args), true);
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
        return IASString.valueOf(this.out());
    }

    public Formatter getFormatter() {
        return this.formatter;
    }
}

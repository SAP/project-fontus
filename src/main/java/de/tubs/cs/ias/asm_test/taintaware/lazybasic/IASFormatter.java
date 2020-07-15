package de.tubs.cs.ias.asm_test.taintaware.lazybasic;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASAbstractFormatter;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASFormatterable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Formatter;
import java.util.Locale;
import java.util.Objects;

public class IASFormatter extends IASAbstractFormatter {
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
        super(a, l, new IASFactoryImpl());
    }

    public IASFormatter(File file) throws FileNotFoundException {
        this(file, IASString.fromString(Charset.defaultCharset().name()));
    }

    public IASFormatter(File file, IASStringable csn) throws FileNotFoundException {
        this(file, csn, Locale.getDefault());
    }

    public IASFormatter(File file, IASStringable csn, Locale l) throws FileNotFoundException {
        this(new FileOutputStream(file), csn, l);
    }

    public IASFormatter(Locale l) {
        this(new IASStringBuffer(), l);
    }

    public IASFormatter(OutputStream o) {
        this(o, IASString.fromString(Charset.defaultCharset().name()));
    }

    public IASFormatter(OutputStream o, IASStringable csn) {
        this(o, csn, Locale.getDefault());
    }

    public IASFormatter(OutputStream o, IASStringable csn, Locale l) {
        this(new PrintWriter(new OutputStreamWriter(o, Charset.forName(csn.getString()))), l);
    }

    public IASFormatter(PrintStream o) {
        this((Appendable) o);
    }

    public IASFormatter(IASStringable fileName) throws FileNotFoundException {
        this(new File(fileName.getString()));
    }

    public IASFormatter(IASStringable fileName, IASStringable csn) throws FileNotFoundException {
        this(new File(fileName.getString()), csn);
    }

    public IASFormatter(IASStringable fileName, IASStringable csn, Locale l) throws FileNotFoundException {
        this(new File(fileName.getString()), csn, l);
    }

    public static IASFormatterable fromFormatter(Formatter param) {
        if (param == null) {
            return null;
        }
        return new IASFormatter(param);
    }

    @Override
    public IASString toIASString() {
        return (IASString) super.toIASString();
    }

    @Override
    public IASFormatter format(IASStringable format, Object... args) {
        return (IASFormatter) super.format(format, args);
    }

    @Override
    public IASFormatter format(Locale l, IASStringable format, Object... args) {
        return (IASFormatter) super.format(l, format, args);
    }
}
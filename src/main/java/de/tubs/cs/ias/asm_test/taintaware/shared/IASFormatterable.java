package de.tubs.cs.ias.asm_test.taintaware.shared;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;

public interface IASFormatterable extends Closeable, Flushable, AutoCloseable {
    IASFormatterable format(IASStringable format, Object... args);

    IASFormatterable format(Locale l, IASStringable format, Object... args);

    IOException ioException();

    Locale locale();

    Appendable out();

    IASStringable toIASString();

    Formatter getFormatter();
}

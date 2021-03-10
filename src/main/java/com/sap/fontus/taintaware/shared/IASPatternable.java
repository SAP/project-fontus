package com.sap.fontus.taintaware.shared;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public interface IASPatternable {
    Pattern getPattern();

    int flags();

    IASMatcherable matcher(CharSequence input);

    IASStringable pattern();

    IASStringable[] split(CharSequence input);

    IASStringable[] split(CharSequence input, int limit);

    default Stream<? extends IASStringable> splitAsStream(CharSequence input) {
        return Arrays.stream(split(input));
    }

    String toString();

    IASStringable toIASString();
}

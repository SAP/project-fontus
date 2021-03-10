package com.sap.fontus.utils;

import com.sap.fontus.Constants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtils {
    @SuppressWarnings("Since15")
    public synchronized static Logger getLogger() {
        Class<?> callerClass;
        if (Constants.JAVA_VERSION >= 9) {
            callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                    .getCallerClass();
        } else {
            callerClass = ReflectionUtils.getCallerClass();
        }
        return new Logger(callerClass.getName(), getFileName());
    }

    private static String getFileName() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd-kk-mm-ss");
        String date = dtf.format(LocalDateTime.now());
        return String.format("asm-%s.log", date);
    }

}

package com.sap.fontus.utils;

import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

class LoggerFormatter extends SimpleFormatter {
    @Override
    public String format(LogRecord record) {
        String sourceClassName = record.getSourceClassName();
        sourceClassName = sourceClassName.substring(sourceClassName.lastIndexOf('.') + 1);
        return String.format("%1$tT %2$s [%3$-7s] %4$s %5$s %n",
                new Date(record.getMillis()),
                getThreadNameById(record.getThreadID()),
                record.getLevel().getName(),
                sourceClassName,
                record.getMessage()
        );
    }

    private static String getThreadNameById(int id) {
        // TODO The instrumentation before was very cpu intensive, maybe replace this with more efficient implementation
        return "unknown";
//        for (Thread t : Thread.getAllStackTraces().keySet()) {
//            if (t.getId() == id) {
//                return t.getName();
//            }
//        }
//        return "Thread-" + id;
    }
}

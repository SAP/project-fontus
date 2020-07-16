package de.tubs.cs.ias.asm_test.utils;

import de.tubs.cs.ias.asm_test.Constants;
import jdk.internal.reflect.Reflection;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public class LogUtils {
    private static final ParentLogger parentlogger;

    static {
        parentlogger = new ParentLogger();
        try {
            FileHandler fileHandler = new FileHandler(getFileName());

            SimpleFormatter formatter = new LoggerFormatter();
            fileHandler.setFormatter(formatter);

            parentlogger.addHandler(fileHandler);
        } catch (IOException e) {
            System.err.println("Could not create log file for instrumentation");
            e.printStackTrace(System.err);
        }

        LogManager.getLogManager().addLogger(parentlogger);
    }

    @SuppressWarnings("Since15")
    public synchronized static ParentLogger getLogger() {
        Class callerClass;
        if (Constants.JAVA_VERSION >= 9) {
            callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                    .getCallerClass();
        } else {
            callerClass = Reflection.getCallerClass();
        }
        Logger logger = new Logger(callerClass.getName());
        logger.setParent(parentlogger);
        return parentlogger;
    }

    private static String getFileName() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd-kk-mm-ss");
        String date = dtf.format(LocalDateTime.now());
        return String.format("asm-%s.log", date);
    }

}

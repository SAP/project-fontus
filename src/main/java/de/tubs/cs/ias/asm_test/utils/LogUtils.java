package de.tubs.cs.ias.asm_test.utils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public class LogUtils {
    private volatile static Logger logger;

    public synchronized static Logger getLogger() {
        if (logger == null) {
            logger = new Logger("InstrumentationLogger");
            try {
                FileHandler fileHandler = new FileHandler(getFileName());

                SimpleFormatter formatter = new LoggerFormatter();
                fileHandler.setFormatter(formatter);

                logger.addHandler(fileHandler);
            } catch (IOException e) {
                System.err.println("Could not create log file for instrumentation");
                e.printStackTrace(System.err);
            }

            LogManager.getLogManager().addLogger(logger);
        }
        return logger;
    }

    private static String getFileName() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd-kk-mm-ss");
        String date = dtf.format(LocalDateTime.now());
        return String.format("asm-%s.log", date);
    }

}

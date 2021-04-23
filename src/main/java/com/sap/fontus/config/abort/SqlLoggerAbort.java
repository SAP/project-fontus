package com.sap.fontus.config.abort;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.fontus.sql_injection.SQLChecker;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASStringable;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.utils.NetworkRequestObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.sap.fontus.utils.Utils.convertStackTrace;

public class SqlLoggerAbort extends Abort{
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void abort(IASTaintAware taintAware, String sink, String category, List<StackTraceElement> stackTrace) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InterruptedException, IOException {
        try {
            NetworkRequestObject request_object = new NetworkRequestObject();
            System.out.println("host : " + request_object.getHeaderByName("host"));
            System.out.println("path : " + request_object.getServletPath());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        IASStringable taintedString = taintAware.toIASString();
        Abort sql_checker_abort = new Abort(sink, category, taintedString.getString(), taintedString.getTaintRanges(), convertStackTrace(stackTrace));

        sendAborts(sql_checker_abort);
    }

    private void sendAborts(Abort sql_checker_abort) throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InterruptedException {
        SQLChecker.logTaintedString(this.objectMapper.writeValueAsString(sql_checker_abort));
    }

    @Override
    public String getName() {
        return "sql_logger";
    }

    public static class Abort {
        private final String sink;
        private final String category;
        private final String payload;
        private final List<IASTaintRange> ranges;
        private final List<String> stackTrace;

        private Abort(String sink, String category, String payload, List<IASTaintRange> ranges, List<String> stackTrace) {
            this.sink = sink;
            this.category = category;
            this.payload = payload;
            this.ranges = ranges;
            this.stackTrace = stackTrace;
        }

        public String getSink() {
            return sink;
        }

        public String getPayload() {
            return payload;
        }

        public List<IASTaintRange> getRanges() {
            return ranges;
        }

        public List<String> getStackTrace() {
            return stackTrace;
        }

        public String getCategory() {
            return category;
        }
    }
}


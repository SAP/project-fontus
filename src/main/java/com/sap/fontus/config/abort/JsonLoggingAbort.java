package com.sap.fontus.config.abort;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.unified.IASString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.sap.fontus.utils.Utils.convertStackTrace;

public class JsonLoggingAbort extends Abort {
    private final List<Abort> previousAborts = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void abort(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {
        IASString taintedString = taintAware.toIASString();
        Abort abort = new Abort(sinkFunction, sinkName, taintedString.getString(), taintedString.getTaintInformationInitialized().getTaintRanges(taintedString.length()), convertStackTrace(stackTrace));
        previousAborts.add(abort);

        saveAborts();
    }

    private void saveAborts() {
        try {
            this.objectMapper.writeValue(Configuration.getConfiguration().getAbortOutputFile(), previousAborts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "json_logging";
    }

    public static class Abort {
        private final String sinkFunction;
        private final String sinkName;
        private final String payload;
        private final IASTaintRanges ranges;
        private final List<String> stackTrace;

        private Abort(String sinkFunction, String sinkName, String payload, IASTaintRanges ranges, List<String> stackTrace) {
            this.sinkFunction = sinkFunction;
            this.sinkName = sinkName;
            this.payload = payload;
            this.ranges = ranges;
            this.stackTrace = stackTrace;
        }

        public String getSinkFunction() {
            return sinkFunction;
        }

        public String getSinkName() {
            return sinkName;
        }

        public String getPayload() {
            return payload;
        }

        public IASTaintRanges getRanges() {
            return ranges;
        }

        public List<String> getStackTrace() {
            return stackTrace;
        }
    }
}

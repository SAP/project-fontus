package de.tubs.cs.ias.asm_test.utils.abort;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JsonLoggingAbort extends Abort {
    private final List<Abort> previousAborts = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void abort(IASTaintAware taintAware, String sink, List<StackTraceElement> stackTrace) {
        IASStringable taintedString = taintAware.toIASString();
        Abort abort = new Abort(sink, taintedString.getString(), taintedString.getTaintRanges(), convertStackTrace(stackTrace));
        previousAborts.add(abort);

        saveAborts();
    }

    private List<String> convertStackTrace(List<StackTraceElement> stackTrace) {
        return stackTrace.stream().map(stackTraceElement -> String.format("%s.%s(%s:%d)", stackTraceElement.getClassName(), stackTraceElement.getMethodName(), stackTraceElement.getFileName(), stackTraceElement.getLineNumber())).collect(Collectors.toList());
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
        return "json_logging_abort";
    }

    public static class Abort {
        private String sink;
        private String payload;
        private List<IASTaintRange> ranges;
        private List<String> stackTrace;

        private Abort(String sink, String payload, List<IASTaintRange> ranges, List<String> stackTrace) {
            this.sink = sink;
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
    }
}

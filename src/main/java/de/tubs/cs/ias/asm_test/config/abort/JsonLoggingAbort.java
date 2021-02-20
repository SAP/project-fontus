package de.tubs.cs.ias.asm_test.config.abort;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.range.IASString;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.tubs.cs.ias.asm_test.utils.Utils.convertStackTrace;

public class JsonLoggingAbort extends Abort {
    private final List<Abort> previousAborts = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void abort(IASTaintAware taintAware, String sink, String category, List<StackTraceElement> stackTrace) {
        IASStringable taintedString = taintAware.toIASString();
        Abort abort = new Abort(sink, category, taintedString.getString(), taintedString.getTaintRanges(), convertStackTrace(stackTrace));
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

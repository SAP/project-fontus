package de.tubs.cs.ias.asm_test.config.abort;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tubs.cs.ias.asm_test.agent.TaintAgent;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.lazybasic.IASString;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.sql_injection.SQLChecker;
import de.tubs.cs.ias.asm_test.utils.ClassUtils;
import de.tubs.cs.ias.asm_test.utils.NetworkRequestObject;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;

//import org.apache.calcite.sql.parser;

import static de.tubs.cs.ias.asm_test.utils.Utils.convertStackTrace;

public class SqlCheckerAbort extends Abort{
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void abort(IASTaintAware taintAware, String sink, String category, List<StackTraceElement> stackTrace) {
        try {
            NetworkRequestObject request_object = new NetworkRequestObject();
            System.out.println("host : " + request_object.getHeaderByName("host"));
            System.out.println("path : " + request_object.getServletPath());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        IASStringable taintedString = taintAware.toIASString();
        SqlAbort sql_checker_abort = new SqlAbort(sink, category, taintedString.getString(), taintedString.getTaintRanges(), convertStackTrace(stackTrace));

        sendAborts(sql_checker_abort);
    }

    private void sendAborts(SqlAbort sql_checker_abort) {
        try {
            //SQLChecker.checkTaintedString(this.objectMapper.writeValueAsString(sql_checker_abort));
            SQLChecker.printCheck(this.objectMapper.writeValueAsString(sql_checker_abort));
        } catch (IOException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "sql_checker";
    }

    public static class SqlAbort {
        private final String sink;
        private final String category;
        private final String payload;
        private final List<IASTaintRange> ranges;
        private final List<String> stackTrace;

        private SqlAbort(String sink, String category, String payload, List<IASTaintRange> ranges, List<String> stackTrace) {
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

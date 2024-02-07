package com.sap.fontus.config.abort;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.shared.IASTaintRanges;

import java.util.List;

public class AbortObject {
    private final String sinkFunction;
    private final String sinkName;
    private final String payload;
    private final IASTaintRanges ranges;
    private final List<String> stackTrace;
    private final List<String> categories;

    public AbortObject(String sinkFunction, String sinkName, String payload, IASTaintRanges ranges, List<String> stackTrace) {
        this.sinkFunction = sinkFunction;
        this.sinkName = sinkName;
        this.payload = payload;
        this.ranges = ranges;
        this.stackTrace = stackTrace;
        this.categories = Configuration.getConfiguration().getSinkConfig().getSinkForName(sinkName).getCategories();
    }

    public String getSinkFunction() {
        return this.sinkFunction;
    }

    public String getSinkName() {
        return this.sinkName;
    }

    public String getPayload() {
        return this.payload;
    }

    public IASTaintRanges getRanges() {
        return this.ranges;
    }

    public List<String> getStackTrace() {
        return this.stackTrace;
    }

    public List<String> getCategories() {
        return this.categories;
    }
}

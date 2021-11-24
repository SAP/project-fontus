package com.sap.fontus.gdpr.handler;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.Sink;
import com.sap.fontus.config.abort.Abort;
import com.sap.fontus.config.abort.MultiAbort;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.*;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.unified.IASString;

import java.util.ArrayList;
import java.util.List;

public class GdprAbort extends Abort {

    public RequiredPurposes getPurposedFromSink(Sink sink) {
        return RequiredPurposeRegistry.getPurposeFromSink(sink);
    }

    public Abort getAbortFromSink(Sink sink) {
        List<Abort> l = new ArrayList<>();
        for (String abortName : sink.getDataProtection().getAborts()) {
            Abort a = Abort.parse(abortName);
            if (a != null) {
                l.add(a);
            }
        }
        return new MultiAbort(l);
    }

    @Override
    public void abort(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {

        // Use the categories from the configuration as "purpose" labels
        Sink sink = Configuration.getConfiguration().getSinkConfig().getSinkForFqn(sinkFunction);
        RequiredPurposes requiredPurposes = getPurposedFromSink(sink);

        // Create a policy
        PurposePolicy policy = new SimplePurposePolicy();

        // Extract taint information
        IASString taintedString = taintAware.toIASString();
        for (IASTaintRange range : taintedString.getTaintInformation().getTaintRanges(taintedString.getString().length())) {
            // Check policy for each range
            if (range.getMetadata() instanceof GdprTaintMetadata) {
                GdprTaintMetadata taintMetadata = (GdprTaintMetadata) range.getMetadata();
                GdprMetadata metadata = taintMetadata.getMetadata();
                if (policy.areRequiredPurposesAllowed(requiredPurposes, metadata.getAllowedPurposes())) {
                    // Block / Sanitize / etc...
                    Abort a = getAbortFromSink(sink);
                    a.abort(taintAware, instance, sinkFunction, sinkName, stackTrace);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "gdpr";
    }
}

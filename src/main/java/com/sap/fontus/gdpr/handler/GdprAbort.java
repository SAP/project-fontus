package com.sap.fontus.gdpr.handler;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.Sink;
import com.sap.fontus.config.abort.Abort;
import com.sap.fontus.config.abort.MultiAbort;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.registry.RequiredPurposeRegistry;
import com.sap.fontus.gdpr.metadata.simple.*;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASTaintRange;
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
    public IASTaintAware abort(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {
        System.out.println("Checking GDPR taint in sink: " + sinkFunction);
        CensorIfDisputed censorIfDisputed = new CensorIfDisputed();
        taintAware = censorIfDisputed.abort(taintAware, instance, sinkFunction, sinkName, stackTrace);
        // Use the categories from the configuration as "purpose" labels
        Sink sink = Configuration.getConfiguration().getSinkConfig().getSinkForFqn(sinkFunction);
        RequiredPurposes requiredPurposes = this.getPurposedFromSink(sink);

        // Create a policy
        PurposePolicy policy = new SimplePurposePolicy();

        // Extract taint information
        IASString taintedString = taintAware.toIASString();
        boolean policyViolation = false;
        for (IASTaintRange range : taintedString.getTaintInformation().getTaintRanges(taintedString.getString().length())) {
            // Check policy for each range
            if (range.getMetadata() instanceof GdprTaintMetadata) {
                GdprTaintMetadata taintMetadata = (GdprTaintMetadata) range.getMetadata();
                GdprMetadata metadata = taintMetadata.getMetadata();
                if (!policy.areRequiredPurposesAllowed(requiredPurposes, metadata.getAllowedPurposes())) {
                    policyViolation = true;
                }
            }
        }
        // Block / Sanitize / etc...
        if (policyViolation) {
            Abort a = this.getAbortFromSink(sink);
            taintAware = a.abort(taintAware, instance, sinkFunction, sinkName, stackTrace);
        }
        return taintAware;
    }

    @Override
    public String getName() {
        return "gdpr";
    }
}

package com.sap.fontus.gdpr.handler;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.Sink;
import com.sap.fontus.config.abort.Abort;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.SimplePurpose;
import com.sap.fontus.gdpr.metadata.simple.SimpleRequiredPurpose;
import com.sap.fontus.gdpr.metadata.simple.SimpleVendor;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.unified.IASString;

import java.util.List;

public class GdprAbort extends Abort {

    public RequiredPurposes getPurposedFromSink(Sink sink) {
        RequiredPurposes requiredPurposes = new RequiredPurposeSet();
        if (sink != null) {
            for (String cat : sink.getCategories()) {
                // Purpose p = new SimplePurpose();
                //Vendor v = new SimpleVendor();
                //RequiredPurpose rp = new SimpleRequiredPurpose(p, v);
            }
        }
        return requiredPurposes;
    }

    @Override
    public void abort(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {

        // Use the categories from the configuration as "purpose" labels
        Sink sink = Configuration.getConfiguration().getSinkConfig().getSinkForFqn(sinkFunction);
        RequiredPurposes requiredPurposes = getPurposedFromSink(sink);


        // Extract taint information
        IASString taintedString = taintAware.toIASString();
        for (IASTaintRange range : taintedString.getTaintInformation().getTaintRanges(taintedString.getString().length())) {
            // Check policy for each range
            if (range.getMetadata() instanceof GdprTaintMetadata) {
                GdprTaintMetadata taintMetadata = (GdprTaintMetadata) range.getMetadata();
                GdprMetadata metadata = taintMetadata.getMetadata();

            }
        }
    }

    @Override
    public String getName() {
        return "gdpr";
    }
}

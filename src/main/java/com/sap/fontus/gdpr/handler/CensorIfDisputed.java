package com.sap.fontus.gdpr.handler;

import com.sap.fontus.config.abort.Abort;
import com.sap.fontus.gdpr.Utils;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.utils.Pair;

import java.util.List;

/**
 * Class that Censors all information that is disputed by the data subject
 */
public class CensorIfDisputed  extends Abort {
    @Override
    public IASTaintAware abort(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {
        Pair<IASTaintAware,Boolean> data = Utils.censorContestedParts(taintAware);
        if(data.y) {
            System.out.printf("Censored '%s' -> '%s' due to contested data%n", taintAware.toIASString().getString(), data.x.toIASString().getString());
        }
        return data.x;
    }

    @Override
    public String getName() {
        return "censorIfDisputed";
    }
}

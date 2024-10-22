package com.sap.fontus.config.abort;

import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.unified.IASString;

import java.util.List;

public class CensoringAbort extends Abort {
    @Override
    public IASTaintAware abort(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {
        if (taintAware.isTainted()) {
            IASString s = taintAware.toIASString();
            if (s != null) {
                StringBuilder sb = new StringBuilder(s.getString());
                for (IASTaintRange range : s.getTaintInformation().getTaintRanges(s.length())) {
                    for (int i = range.getStart(); i < range.getEnd(); i++) {
                        sb.setCharAt(i, '*');
                    }
                }
                taintAware = taintAware.newInstance();
                taintAware.setContent(sb.toString(), s.getTaintInformationCopied());
            }
        }
        return taintAware;
    }

    @Override
    public String getName() {
        return "censor";
    }

    static {
        Abort.add(new CensoringAbort());
    }

}

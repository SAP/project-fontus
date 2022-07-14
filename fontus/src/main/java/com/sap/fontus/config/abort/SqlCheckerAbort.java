package com.sap.fontus.config.abort;

import com.sap.fontus.sanitizer.SQLChecker;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.unified.IASString;

import java.util.List;

import static com.sap.fontus.utils.Utils.convertStackTrace;

public class SqlCheckerAbort extends Abort{

    @Override
    public IASTaintAware abort(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {

        IASString taintedString = taintAware.toIASString();
        AbortObject sql_checker_abort = new AbortObject(sinkFunction, sinkName, taintedString.getString(), taintedString.getTaintInformationInitialized().getTaintRanges(taintedString.length()), convertStackTrace(stackTrace));
        SQLChecker.reportTaintedString(sql_checker_abort);

        return taintAware;
    }

    @Override
    public String getName() {
        return "sql_checker";
    }
}

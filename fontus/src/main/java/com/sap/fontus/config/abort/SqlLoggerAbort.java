package com.sap.fontus.config.abort;

import com.sap.fontus.sanitizer.SQLChecker;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.unified.IASString;

import java.util.List;

import static com.sap.fontus.utils.Utils.convertStackTrace;

public class SqlLoggerAbort extends Abort{

    @Override
    public IASTaintAware abort(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {

        IASString taintedString = taintAware.toIASString();
        AbortObject sqlCheckerAbort = new AbortObject(sinkFunction, sinkName, taintedString.getString(), taintedString.getTaintInformationInitialized().getTaintRanges(taintedString.length()), convertStackTrace(stackTrace));

        SQLChecker.logTaintedString(sqlCheckerAbort);

        return taintAware;
    }

    @Override
    public String getName() {
        return "sql_checker";
    }

    static {
        Abort.add(new SqlLoggerAbort());
    }
}

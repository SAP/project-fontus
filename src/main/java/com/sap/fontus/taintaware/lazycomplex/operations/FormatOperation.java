package com.sap.fontus.taintaware.lazycomplex.operations;

import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.range.IASString;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.lazycomplex.IASOperation;
import com.sap.fontus.taintaware.range.IASFormatter;

import java.util.List;
import java.util.Locale;

public class FormatOperation implements IASOperation {
    private final com.sap.fontus.taintaware.lazycomplex.IASString format;
    private final Object[] args;
    private final Locale locale;

    public FormatOperation(Locale l, com.sap.fontus.taintaware.lazycomplex.IASString format, Object... args) {
        this.locale = l;
        this.format = format;
        this.args = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof IASTaintAware) {
                com.sap.fontus.taintaware.lazycomplex.IASString str = (com.sap.fontus.taintaware.lazycomplex.IASString) ((IASTaintAware) args[i]).toIASString();
                this.args[i] = new IASString(str.getString(), str.getTaintRanges());
            } else if (this.isPrimitiveOrWrapper(args[i])) {
                this.args[i] = args[i];
            } else {
                this.args[i] = args[i] != null ? args[i].toString() : null;
            }
        }
    }

    public boolean isPrimitiveOrWrapper(Object obj) {
        if (obj == null) {
            return false;
        }
        Class<?> type = obj.getClass();
        return type.isPrimitive() || type == Double.class || type == Float.class || type == Long.class ||
                type == Integer.class || type == Short.class || type == Character.class ||
                type == Byte.class || type == Boolean.class;
    }

    @Override
    public List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousTaint) {
        IASString string = new IASString(this.format.getString(), this.format.getTaintRanges());
        return new IASFormatter(this.locale).format(string, this.args).toIASString().getTaintRanges();
    }
}

package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASString;
import de.tubs.cs.ias.asm_test.taintaware.range.IASFormatter;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;

import java.util.List;
import java.util.Locale;

public class FormatOperation implements IASOperation {
    private final IASString format;
    private final Object[] args;
    private final Locale locale;

    public FormatOperation(Locale l, IASString format, Object... args) {
        this.locale = l;
        this.format = format;
        this.args = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof IASTaintAware) {
                IASString str = (IASString) ((IASTaintAware) args[i]).toIASString();
                this.args[i] = new de.tubs.cs.ias.asm_test.taintaware.range.IASString(str.getString(), str.getTaintRanges());
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
        de.tubs.cs.ias.asm_test.taintaware.range.IASString string = new de.tubs.cs.ias.asm_test.taintaware.range.IASString(this.format.getString(), this.format.getTaintRanges());
        return new IASFormatter(this.locale).format(string, this.args).toIASString().getTaintRanges();
    }
}

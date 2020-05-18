package de.tubs.cs.ias.asm_test.transformer;

import de.tubs.cs.ias.asm_test.MethodTaintingUtils;
import de.tubs.cs.ias.asm_test.MethodTaintingVisitor;
import de.tubs.cs.ias.asm_test.config.Sink;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.strategies.InstrumentationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class SinkTransformer implements ParameterTransformation {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Sink sink;
    private final TaintStringConfig config;

    public SinkTransformer(Sink sink, TaintStringConfig configuration) {
        this.sink = sink;
        this.config = configuration;
    }

    @Override
    public void transform(int index, String type, MethodTaintingVisitor visitor) {

        if (this.sink == null) {
            return;
        }

        // Sink checks
        logger.debug("Type: {}", type);
        // Check whether this parameter needs to be checked for taint
        if (this.sink.findParameter(index) != null) {
            if (InstrumentationHelper.getInstance(this.config).canHandleType(type)) {
                logger.info("Adding taint check for sink {}, paramater {} ({})", this.sink.getName(), index, type);
                MethodTaintingUtils.callCheckTaint(visitor.getParent(), this.config);
            } else {
                logger.warn("Tried to check taint for type {} (index {}) in sink {} although it is not taintable!", type, index, this.sink.getName());
            }
        }
    }
}

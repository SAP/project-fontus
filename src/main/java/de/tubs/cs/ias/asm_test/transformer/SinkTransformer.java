package de.tubs.cs.ias.asm_test.transformer;

import de.tubs.cs.ias.asm_test.instrumentation.MethodTaintingUtils;
import de.tubs.cs.ias.asm_test.instrumentation.MethodTaintingVisitor;
import de.tubs.cs.ias.asm_test.config.Sink;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.InstrumentationHelper;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;
import de.tubs.cs.ias.asm_test.utils.LogUtils;
import org.objectweb.asm.Type;

public class SinkTransformer implements ParameterTransformation {
    private static final ParentLogger logger = LogUtils.getLogger();

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
            String instrumentedType = InstrumentationHelper.getInstance(this.config).instrumentQN(type);
            logger.info("Adding taint check for sink {}, paramater {} ({})", this.sink.getName(), index, type);
            String sinkName = String.format("%s.%s%s", this.sink.getFunction().getOwner(), this.sink.getFunction().getName(), this.sink.getFunction().getDescriptor());
            MethodTaintingUtils.callCheckTaintGeneric(visitor.getParent(), instrumentedType, sinkName);
        }
    }
}

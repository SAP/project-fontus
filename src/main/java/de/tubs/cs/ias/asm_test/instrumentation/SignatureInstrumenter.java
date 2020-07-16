package de.tubs.cs.ias.asm_test.instrumentation;

import de.tubs.cs.ias.asm_test.instrumentation.strategies.InstrumentationStrategy;
import de.tubs.cs.ias.asm_test.utils.LogUtils;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

import java.util.List;

public class SignatureInstrumenter {
    private static final ParentLogger logger = LogUtils.getLogger();
    private final List<? extends InstrumentationStrategy> instrumentation;
    private final int api;

    public SignatureInstrumenter(int api, List<? extends InstrumentationStrategy> instrumentation) {
        this.api = api;
        this.instrumentation = instrumentation;
    }

    public String instrumentSignature(String signature) {
        logger.info("Instrumenting signature {}", signature);
        if (signature == null) {
            return null;
        }
        SignatureWriter sw = new SignatureWriter();
        SignatureVisitor sv = new SignatureTaintingVisitor(this.api, this.instrumentation, sw);
        SignatureReader sr = new SignatureReader(signature);
        sr.accept(sv);
        return sw.toString();
    }
}

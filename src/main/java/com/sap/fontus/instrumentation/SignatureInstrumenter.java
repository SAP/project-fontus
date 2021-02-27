package com.sap.fontus.instrumentation;

import com.sap.fontus.instrumentation.strategies.InstrumentationStrategy;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

import java.util.List;

public class SignatureInstrumenter {
    private static final Logger logger = LogUtils.getLogger();
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

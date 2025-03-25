package com.sap.fontus.instrumentation;

import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

public class SignatureInstrumenter {
    private static final Logger logger = LogUtils.getLogger();
    private final int api;
    private final InstrumentationHelper instrumentationHelper;

    public SignatureInstrumenter(int api, InstrumentationHelper instrumentationHelper) {
        this.api = api;
        this.instrumentationHelper = instrumentationHelper;
    }

    public String instrumentSignature(String signature) {
        if (signature == null) {
            return null;
        }
        if(LogUtils.LOGGING_ENABLED) {
            logger.info("Instrumenting signature {}", signature);
        }
        SignatureWriter sw = new SignatureWriter();
        SignatureVisitor sv = new SignatureTaintingVisitor(this.api, this.instrumentationHelper, sw);
        SignatureReader sr = new SignatureReader(signature);
        sr.accept(sv);
        return sw.toString();
    }
}

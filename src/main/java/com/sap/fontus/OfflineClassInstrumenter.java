package com.sap.fontus;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.instrumentation.Instrumenter;
import com.sap.fontus.utils.InstrumentationFactory;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.VerboseLogger;
import com.sap.fontus.utils.offline.OfflineClassResolver;
import org.objectweb.asm.ClassReader;

import java.io.*;

public class OfflineClassInstrumenter {
    private static final Logger logger = LogUtils.getLogger();
    private final Instrumenter instrumenter = new Instrumenter();
    private Configuration configuration;

    public OfflineClassInstrumenter(Configuration configuration) {
        this.configuration = configuration;
    }

    public void instrumentClassFile(File input, File output) throws IOException {
        FileInputStream fi = new FileInputStream(input);
        FileOutputStream fo = new FileOutputStream(output);
        logger.info("Reading class file from: {}", input.getAbsolutePath());
        this.instrumentClassStream(fi, fo);
        logger.info("Writing transformed class file to: {}", output.getAbsolutePath());
    }


    public void instrumentClassStream(InputStream i, OutputStream o) throws IOException {
        byte[] outArray = this.instrumentClassStream(i);
        o.write(outArray);
    }


    public byte[] instrumentClassStream(InputStream i) throws IOException {
        byte[] outArray;
        try {
            outArray = this.instrumenter.instrumentClass(i, InstrumentationFactory.createClassResolver(ClassLoader.getSystemClassLoader()), this.configuration, false);
            VerboseLogger.saveIfVerbose(new ClassReader(outArray).getClassName(), outArray);
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().equals("JSR/RET are not supported with computeFrames option")) {
                outArray = this.instrumenter.instrumentClass(i, InstrumentationFactory.createClassResolver(ClassLoader.getSystemClassLoader()), this.configuration, true);
            } else {
                throw ex;
            }
        }

        return outArray;
    }
}

package com.sap.fontus.taintaware.shared;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.instrumentation.strategies.InstrumentationHelper;
import com.sap.fontus.utils.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class IASInstrumenterInputStream extends ByteArrayInputStream {

    public IASInstrumenterInputStream(InputStream inputStream) {
        super(readAndInstrument(inputStream).getBytes(StandardCharsets.UTF_8));
    }

    private static String readAndInstrument(InputStream is) {
        InstrumentationHelper instrumentationHelper = InstrumentationHelper.getInstance(Configuration.getConfiguration().getTaintStringConfig());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        return br.lines()
                .map(Utils::dotToSlash)
                .map(instrumentationHelper::instrumentQN)
                .map(Utils::slashToDot)
                .collect(Collectors.joining("\n"));
    }
}

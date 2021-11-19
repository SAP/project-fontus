package com.sap.fontus.agent;

import com.sap.fontus.config.ConfigurationLoader;
import com.sap.fontus.config.abort.Abort;
import com.sap.fontus.Constants;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.config.taintloss.TaintlossHandler;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class AgentConfig {
    private static final Logger logger = LogUtils.getLogger();
    private final boolean verbose;
    private final boolean taintPersistence;
    private final List<String> blacklist;
    private final TaintMethod taintMethod;

    public boolean isVerbose() {
        return this.verbose;
    }

    public List<String> getBlacklistedMainClasses() {
        return Collections.unmodifiableList(this.blacklist);
    }

    public TaintMethod getTaintMethod() {
        return this.taintMethod;
    }

    private AgentConfig() {
        this.verbose = false;
        this.blacklist = new ArrayList<>();
        this.taintMethod = TaintMethod.defaultTaintMethod();
        taintPersistence = false;
    }

    private AgentConfig(boolean verbose, List<String> blacklist, TaintMethod taintMethod, boolean taintPersistence) {
        this.verbose = verbose;
        this.taintPersistence = taintPersistence;
        this.blacklist = blacklist;
        this.taintMethod = taintMethod;
    }

    public static Configuration parseConfig(String args) {
        if (args == null) {
            Configuration c = ConfigurationLoader.defaultConfiguration();
            c.setTaintMethod(TaintMethod.defaultTaintMethod());
            c.transformConverters();
            return c;
        }
        try (Scanner sc = new Scanner(args)) {
            sc.useDelimiter(Constants.AGENT_DELIMITER);
            Collection<String> parts = new ArrayList<>();
            while (sc.hasNext()) {
                String part = sc.next();
                parts.add(part);
            }
            return parseParts(parts);
        }
    }

    private static Configuration parseParts(Iterable<String> parts) {
        Configuration c = ConfigurationLoader.defaultConfiguration();
        boolean verbose = false;
        boolean taintPersistence = false;
        boolean welcome = false;
        Boolean loggingEnabled = null;
        TaintMethod taintMethod = TaintMethod.defaultTaintMethod();
        Boolean useCaching = null;
        Integer layerThreshold = null;
        Boolean collectStats = null;
        Abort abort = null;
        TaintlossHandler taintlossHandler = null;

        for (String part : parts) {
            if ("verbose".equals(part)) {
                verbose = true;
            }
            if ("persistence".equals(part)) {
                taintPersistence = true;
            }
            if ("logging_enabled".equals(part)) {
                loggingEnabled = true;
            }
            if ("enable_welcome".equals(part)) {
                welcome = true;
            }
            if (part.startsWith("taintmethod=")) {
                String taintMethodArgName = afterEquals(part);
                taintMethod = TaintMethod.getTaintMethodByArgumentName(taintMethodArgName);
            }
            if (part.startsWith("use_caching=")) {
                String useCachingString = afterEquals(part);
                useCaching = Boolean.parseBoolean(useCachingString);
            }
            if (part.startsWith("layer_threshold=")) {
                String layerThresholdString = afterEquals(part);
                layerThreshold = Integer.parseInt(layerThresholdString);
            }
            if (part.startsWith("collect_stats=")) {
                String collectStatsString = afterEquals(part);
                collectStats = Boolean.parseBoolean(collectStatsString);
            }
            if (part.startsWith("config=")) {
                String filename = afterEquals(part);
                Configuration cmdlineconfig = ConfigurationLoader.loadConfigurationFrom(new File(filename));
                c.append(cmdlineconfig);
            }
            if (part.startsWith("blacklisted_main_classes=")) {
                String filename = afterEquals(part);
                Configuration blacklist = ConfigurationLoader.loadBlacklistFromFile(new File(filename));
                c.append(blacklist);
            }
            if (part.startsWith("abort=")) {
                String abortName = afterEquals(part);
                abort = Abort.parse(abortName);
            }
            if (part.startsWith("taintloss_handler=")) {
                String taintlossHandlerName = afterEquals(part);
                taintlossHandler = TaintlossHandler.parse(taintlossHandlerName);
            }
        }
        if (c == null) {
            c = ConfigurationLoader.defaultConfiguration();
        }
        c.setVerbose(verbose || c.isVerbose());
        c.setTaintPersistence(taintPersistence || c.hasTaintPersistence());
        c.setTaintMethod(taintMethod);
        c.setShowWelcomeMessage(welcome);

        if (useCaching != null) {
            c.setUseCaching(useCaching);
        }
        if (layerThreshold != null) {
            c.setLayerThreshold(layerThreshold);
        }
        if (collectStats != null) {
            c.setCollectStats(collectStats);
        }
        if (loggingEnabled != null) {
            c.setLoggingEnabled(loggingEnabled);
        }
        if (abort != null) {
            c.setAbort(abort);
        }
        if (taintlossHandler != null) {
            c.setTaintlossHandler(taintlossHandler);
        }

        c.transformConverters();
        return c;
    }

    private static List<String> readFromFile(String fileName) {
        File input = new File(fileName);
        if (!input.isFile()) {
            logger.error("Suggested file '{}' does not exist!", fileName);
            return new ArrayList<>(0);
        }
        try {
            return Files.readAllLines(input.toPath());
        } catch (IOException e) {
            logger.error("Exception while reading file: '{}':", fileName, e);
            return new ArrayList<>(0);
        }
    }

    private static String afterEquals(String part) {
        int idx = part.indexOf('=');
        return part.substring(idx + 1);
    }

}

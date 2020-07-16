package de.tubs.cs.ias.asm_test.agent;

import de.tubs.cs.ias.asm_test.config.TaintMethod;

import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.config.ConfigurationLoader;
import de.tubs.cs.ias.asm_test.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;

public class AgentConfig {
    private static final ParentLogger logger = LogUtils.getLogger();
    private final boolean verbose;
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
    }

    private AgentConfig(boolean verbose, List<String> blacklist, TaintMethod taintMethod) {
        this.verbose = verbose;
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
            sc.useDelimiter(";");
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
        TaintMethod taintMethod = TaintMethod.defaultTaintMethod();
        Boolean useCaching = null;
        Integer layerThreshold = null;
        Boolean countRanges = null;

        for (String part : parts) {
            if ("verbose".equals(part)) {
                verbose = true;
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
            if (part.startsWith("count_ranges=")) {
                String countRangesString = afterEquals(part);
                countRanges = Boolean.parseBoolean(countRangesString);
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
        }
        if (c == null) {
            c = ConfigurationLoader.defaultConfiguration();
        }
        c.setVerbose(verbose || c.isVerbose());
        c.setTaintMethod(taintMethod);

        if (useCaching != null) {
            c.setUseCaching(useCaching);
        }
        if (layerThreshold != null) {
            c.setLayerThreshold(layerThreshold);
        }
        if (countRanges != null) {
            c.setCountRanges(countRanges);
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

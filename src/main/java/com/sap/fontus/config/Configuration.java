package com.sap.fontus.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.abort.Abort;
import com.sap.fontus.config.abort.StdErrLoggingAbort;
import com.sap.fontus.instrumentation.BlackListEntry;
import com.sap.fontus.agent.AgentConfig;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@XmlRootElement(name = "configuration")
public class Configuration {
    private static Configuration configuration;

    private static final Logger logger = LogUtils.getLogger();
    @JsonIgnore
    private TaintMethod taintMethod;
    @JsonIgnore
    private TaintStringConfig taintStringConfig;

    private Map<String, List<BlackListEntry>> jdkInheritanceBlacklist = new HashMap<>();

    private boolean useCaching = defaultUseCaching();

    private int layerThreshold = defaultLayerThreshold();

    private boolean collectStats = defaultCollectStats();

    @JsonIgnore
    private Abort abort = defaultAbort();

    private boolean isOfflineInstrumentation = true;

    @XmlElement
    private boolean verbose = false;

    @XmlElement
    private boolean loggingEnabled = false;

    @XmlElement
    private boolean recursiveTainting = false;

    @XmlElement
    private final SourceConfig sourceConfig;

    /**
     * All functions listed here consume Strings that need to be checked first.
     */
    @XmlElement
    private final SinkConfig sinkConfig;
    @JacksonXmlElementWrapper(localName = "converters")
    @XmlElement(name = "converter")
    private final List<FunctionCall> converters;

    @JacksonXmlElementWrapper(localName = "returnGenerics")
    @XmlElement(name = "returnGeneric")
    private final List<ReturnsGeneric> returnGeneric;

    @JacksonXmlElementWrapper(localName = "takeGenerics")
    @XmlElement(name = "takeGeneric")
    private final List<TakesGeneric> takeGeneric;

    @JacksonXmlElementWrapper(localName = "blacklistedMainClasses")
    @XmlElement(name = "class")
    private final List<String> blacklistedMainClasses;

    @JacksonXmlElementWrapper(localName = "excludedPackages")
    @XmlElement(name = "excludedPackages")
    private final List<String> excludedPackages;

    public Configuration() {
        this.verbose = false;
        this.sourceConfig = new SourceConfig();
        this.sinkConfig = new SinkConfig();
        this.converters = new ArrayList<>();
        this.returnGeneric = new ArrayList<>();
        this.takeGeneric = new ArrayList<>();
        this.blacklistedMainClasses = new ArrayList<>();
        this.excludedPackages = new ArrayList<>();
    }

    public Configuration(boolean verbose, SourceConfig sourceConfig, SinkConfig sinkConfig, List<FunctionCall> converters, List<ReturnsGeneric> returnGeneric, List<TakesGeneric> takeGeneric, List<String> blacklistedMainClasses, List<String> excludedPackages) {
        this.verbose = verbose;
        this.sourceConfig = sourceConfig;
        this.sinkConfig = sinkConfig;
        this.converters = converters;
        this.returnGeneric = returnGeneric;
        this.takeGeneric = takeGeneric;
        this.blacklistedMainClasses = blacklistedMainClasses;
        this.excludedPackages = excludedPackages;
    }

    public void append(Configuration other) {
        if (other != null) {
            this.verbose |= other.verbose;
            this.sourceConfig.append(other.sourceConfig);
            this.sinkConfig.append(other.sinkConfig);
            this.converters.addAll(other.converters);
            this.returnGeneric.addAll(other.returnGeneric);
            this.takeGeneric.addAll(other.takeGeneric);
            this.blacklistedMainClasses.addAll(other.blacklistedMainClasses);
            this.excludedPackages.addAll(other.excludedPackages);
        }
    }

    public void setTaintMethod(TaintMethod taintMethod) {
        this.taintMethod = taintMethod;
        this.taintStringConfig = new TaintStringConfig(taintMethod);
    }

    public void transformConverters() {
        List<FunctionCall> converted = this.converters.stream().map(functionCall -> new FunctionCall(functionCall.getOpcode(), functionCall.getOwner(), functionCall.getName(), functionCall.getDescriptor(), functionCall.isInterface())).collect(Collectors.toList());

        this.converters.clear();
        this.converters.addAll(converted);
    }

    public TaintStringConfig getTaintStringConfig() {
        return this.taintStringConfig;
    }

    void appendBlacklist(Collection<String> other) {
        if (this.blacklistedMainClasses != null) {
            this.blacklistedMainClasses.addAll(other);
        }
    }

    public Map<String, List<BlackListEntry>> getJdkInheritanceBlacklist() {
        return jdkInheritanceBlacklist;
    }

    public void setJdkInheritanceBlacklist(Map<String, List<BlackListEntry>> jdkInheritanceBlacklist) {
        this.jdkInheritanceBlacklist = jdkInheritanceBlacklist;
    }

    public boolean isOfflineInstrumentation() {
        return isOfflineInstrumentation;
    }

    public void setOfflineInstrumentation(boolean offlineInstrumentation) {
        isOfflineInstrumentation = offlineInstrumentation;
    }

    public static boolean defaultUseCaching() {
        return true;
    }

    public static int defaultLayerThreshold() {
        return 30;
    }

    public static boolean defaultCollectStats() {
        return false;
    }

    private static Abort defaultAbort() {
        return new StdErrLoggingAbort();
    }

    public void setCollectStats(boolean collectStats) {
        this.collectStats = collectStats;
    }

    public boolean useCaching() {
        return useCaching;
    }

    public void setUseCaching(boolean useCaching) {
        this.useCaching = useCaching;
    }

    public int getLayerThreshold() {
        return layerThreshold;
    }

    public void setLayerThreshold(int layerThreshold) {
        this.layerThreshold = layerThreshold;
    }

    public SourceConfig getSourceConfig() {
        return this.sourceConfig;
    }

    public SinkConfig getSinkConfig() {
        return this.sinkConfig;
    }

    public List<FunctionCall> getConverters() {
        return this.converters;
    }

    public List<ReturnsGeneric> getReturnGeneric() {
        return this.returnGeneric;
    }

    public List<TakesGeneric> getTakeGeneric() {
        return this.takeGeneric;
    }

    public List<String> getBlacklistedMainClasses() {
        return this.blacklistedMainClasses;
    }

    private FunctionCall getConverter(String name) {
        for (FunctionCall fc : this.converters) {
            if (fc.getName().equals(name)) {
                return fc;
            }
        }
        return null;
    }

    public boolean needsParameterConversion(FunctionCall c) {
        for (TakesGeneric tg : this.takeGeneric) {
            if (tg.getFunctionCall().equals(c)) {
                return true;
            }
        }
        return false;
    }

    public FunctionCall getConverterForParameter(FunctionCall c, int index) {
        for (TakesGeneric tg : this.takeGeneric) {
            if (tg.getFunctionCall().equals(c)) {
                Conversion conversion = tg.getConversionAt(index);
                if (conversion != null) {
                    String converterName = conversion.getConverter();
                    FunctionCall converter = this.getConverter(converterName);
                    logger.info("Found converter for {} at index {}: {}", c, index, converter);
                    return converter;
                }
            }
        }
        return null;
    }

    public FunctionCall getConverterForReturnValue(FunctionCall c) {
        for (ReturnsGeneric rg : this.returnGeneric) {
            if (rg.getFunctionCall().equals(c)) {
                String converterName = rg.getConverter();
                FunctionCall converter = this.getConverter(converterName);
                logger.info("Found converter for rv of {}: {}", c, converter);
                return converter;
            }
        }
        return null;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isVerbose() {
        return this.verbose;
    }

    public boolean isClassMainBlacklisted(String owner) {
        return this.blacklistedMainClasses.contains(owner);
    }

    public TaintMethod getTaintMethod() {
        return this.taintMethod;
    }

    public boolean collectStats() {
        return this.collectStats;
    }

    public void setAbort(Abort abort) {
        if (abort == null) {
            throw new IllegalArgumentException(new NullPointerException());
        }
        this.abort = abort;
    }

    public Abort getAbort() {
        return abort;
    }

    public static boolean isInitialized() {
        return Configuration.configuration != null;
    }

    public static Configuration getConfiguration() {
        if (configuration == null) {
            throw new IllegalStateException("Configuration not initialized! This should never happen!");
        }
        return configuration;
    }

    public static boolean isLoggingEnabled() {
        return !isInitialized() || Configuration.getConfiguration().loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public List<String> getExcludedPackages() {
        return excludedPackages;
    }

    public static void parseAgent(String args) {
        Configuration configuration = AgentConfig.parseConfig(args);
        configuration.setOfflineInstrumentation(false);

        setConfiguration(configuration);
    }

    public static void setTestConfig(TaintMethod taintMethod) {
        if (Configuration.configuration == null) {
            parseOffline(taintMethod);
        } else {
            Configuration.getConfiguration().setTaintMethod(taintMethod);
        }
    }

    public static void parseOffline(TaintMethod method) {
        Configuration configuration = new Configuration();
        configuration.setTaintMethod(method);

        String collectStatsString = System.getenv("ASM_COLLECT_STATS");
        if (collectStatsString != null) {
            try {
                boolean collectStats = Boolean.parseBoolean(collectStatsString);
                configuration.setCollectStats(collectStats);
                logger.info("Set collect_stats to {}", collectStats);
            } catch (Exception ex) {
                logger.error("Couldn't parse ASM_COLLECT_STATS environment variable: {}", collectStatsString);
            }
        }

        String useCachingString = System.getenv("ASM_USE_CACHING");
        if (useCachingString != null) {
            try {
                boolean useCaching = Boolean.parseBoolean(useCachingString);
                configuration.setUseCaching(useCaching);
                logger.info("Set use_caching to {}", useCaching);
            } catch (Exception ex) {
                logger.error("Couldn't parse ASM_USE_CACHING environment variable: {}", collectStatsString);
            }
        }

        String layerThresholdString = System.getenv("ASM_LAYER_THRESHOLD");
        if (layerThresholdString != null) {
            try {
                int layerThreshold = Integer.parseInt(layerThresholdString);
                configuration.setLayerThreshold(layerThreshold);
                logger.info("Set layer_threshold to {}", layerThreshold);
            } catch (Exception ex) {
                logger.error("Couldn't parse ASM_LAYER_THRESHOLD environment variable: {}", collectStatsString);
            }
        }

        setConfiguration(configuration);
    }

    public static void setConfiguration(Configuration configuration) {
        if (Configuration.configuration != null) {
            throw new IllegalStateException("Configuration already initialized");
        }
        Configuration.configuration = configuration;
    }

    @JsonIgnore
    public File getAbortOutputFile() {
        return new File("fontus-results.json");
    }

    public void setRecursiveTainting(boolean recursiveTainting) {
        this.recursiveTainting = recursiveTainting;
    }

    public boolean isRecursiveTainting() {
        return recursiveTainting;
    }
}

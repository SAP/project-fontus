package com.sap.fontus.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.abort.Abort;
import com.sap.fontus.config.abort.StdErrLoggingAbort;
import com.sap.fontus.config.taintloss.TaintlossHandler;
import com.sap.fontus.instrumentation.BlackListEntry;
import com.sap.fontus.agent.AgentConfig;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@XmlRootElement(name = "configuration")
public final class Configuration {
    private static Configuration configuration;

    private static final Logger logger = LogUtils.getLogger();
    @JsonIgnore
    private TaintMethod taintMethod;

    private boolean showWelcomeMessage;

    private Map<String, List<BlackListEntry>> jdkInheritanceBlacklist = new HashMap<>();

    private boolean useCaching = defaultUseCaching();

    private int layerThreshold = defaultLayerThreshold();

    private boolean collectStats = defaultCollectStats();

    @JsonIgnore
    private Abort abort = defaultAbort();

    @JsonIgnore
    private TaintlossHandler taintlossHandler;

    @JsonIgnore
    private Collection<String> instumentedClasses;

    @JsonIgnore
    private boolean isOfflineInstrumentation = true;

    @JsonIgnore
    private boolean speculativeInstrumentation = true;

    /**
     * If offline instrumentation is used, it can be parallelized
     */
    @JsonIgnore
    private boolean isParallel = false;

    @XmlElement
    private boolean isHybridMode = false;

    @XmlElement
    private boolean verbose = false;

    @XmlElement
    private boolean persistentCache = false;

    @XmlElement
    private boolean taintPersistence = false;

    @XmlElement
    private boolean loggingEnabled = false;

    @XmlElement
    private boolean recursiveTainting = false;

    @XmlElement
    private final SourceConfig sourceConfig;

    @XmlElement
    private final SinkConfig sinkConfig;

    @JacksonXmlElementWrapper(localName = "vendors")
    @XmlElement(name = "vendor")
    private final List<Vendor> vendors;

    @JacksonXmlElementWrapper(localName = "purposes")
    @XmlElement(name = "purpose")
    private final List<Purpose> purposes;

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

    @JacksonXmlElementWrapper(localName = "excludedClasses")
    @XmlElement(name = "excludedClasses")
    private final List<String> excludedClasses;

    @JacksonXmlElementWrapper(localName = "resourcesToInstrument")
    @XmlElement(name = "resourcesToInstrument")
    private final List<String> resourcesToInstrument;

    @JacksonXmlElementWrapper(localName = "passThroughTaints")
    @XmlElement(name = "passThroughTaint")
    private final List<FunctionCall> passThroughTaints;

    @JacksonXmlElementWrapper(localName = "propagateTaintInFunctions")
    @XmlElement(name = "propagateTaintInFunctions")
    private final List<PropagateTaintInFunction> propagateTaintInFunctions;

    public Configuration() {
        this.verbose = false;
        this.sourceConfig = new SourceConfig();
        this.sinkConfig = new SinkConfig();
        this.purposes = new ArrayList<>();
        this.vendors = new ArrayList<>();
        this.converters = new ArrayList<>();
        this.returnGeneric = new ArrayList<>();
        this.takeGeneric = new ArrayList<>();
        this.blacklistedMainClasses = new ArrayList<>();
        this.excludedPackages = new ArrayList<>();
        this.excludedClasses = new ArrayList<>();
        this.resourcesToInstrument = new ArrayList<>();
        this.passThroughTaints = new ArrayList<>();
        this.propagateTaintInFunctions = new ArrayList<>();
    }

    public Configuration(boolean verbose,
                         SourceConfig sourceConfig,
                         SinkConfig sinkConfig,
                         List<Purpose> purposes,
                         List<Vendor> vendors,
                         List<FunctionCall> converters,
                         List<ReturnsGeneric> returnGeneric,
                         List<TakesGeneric> takeGeneric,
                         List<String> blacklistedMainClasses,
                         List<String> excludedPackages,
                         List<String> excludedClasses,
                         List<String> resourcesToInstrument,
                         List<FunctionCall> passThroughTaints,
                         List<PropagateTaintInFunction> propagateTaintInFunctions) {
        this.verbose = verbose;
        this.sourceConfig = sourceConfig;
        this.sinkConfig = sinkConfig;
        this.purposes = purposes;
        this.vendors = vendors;
        this.converters = converters;
        this.returnGeneric = returnGeneric;
        this.takeGeneric = takeGeneric;
        this.blacklistedMainClasses = blacklistedMainClasses;
        this.excludedPackages = excludedPackages;
        this.excludedClasses = excludedClasses;
        this.resourcesToInstrument = resourcesToInstrument;
        this.passThroughTaints = passThroughTaints;
        this.propagateTaintInFunctions = propagateTaintInFunctions;
    }

    public void append(Configuration other) {
        if (other != null) {
            this.verbose |= other.verbose;
            this.persistentCache |= other.persistentCache;
            this.sourceConfig.append(other.sourceConfig);
            this.sinkConfig.append(other.sinkConfig);
            this.vendors.addAll(other.vendors);
            this.purposes.addAll(other.purposes);
            this.converters.addAll(other.converters);
            this.returnGeneric.addAll(other.returnGeneric);
            this.takeGeneric.addAll(other.takeGeneric);
            this.blacklistedMainClasses.addAll(other.blacklistedMainClasses);
            this.excludedPackages.addAll(other.excludedPackages);
            this.excludedClasses.addAll(other.excludedClasses);
            this.resourcesToInstrument.addAll(other.resourcesToInstrument);
            this.passThroughTaints.addAll(other.passThroughTaints);
            this.propagateTaintInFunctions.addAll(other.propagateTaintInFunctions);
        }
    }

    public void setTaintMethod(TaintMethod taintMethod) {
        this.taintMethod = taintMethod;
    }

    public void transformConverters() {
        List<FunctionCall> converted = this.converters.stream().map(functionCall -> new FunctionCall(functionCall.getOpcode(), functionCall.getOwner(), functionCall.getName(), functionCall.getDescriptor(), functionCall.isInterface())).collect(Collectors.toList());

        this.converters.clear();
        this.converters.addAll(converted);
    }

    void appendBlacklist(Collection<String> other) {
        if (this.blacklistedMainClasses != null) {
            this.blacklistedMainClasses.addAll(other);
        }
    }

    public Map<String, List<BlackListEntry>> getJdkInheritanceBlacklist() {
        return this.jdkInheritanceBlacklist;
    }

    public void setJdkInheritanceBlacklist(Map<String, List<BlackListEntry>> jdkInheritanceBlacklist) {
        this.jdkInheritanceBlacklist = jdkInheritanceBlacklist;
    }

    public boolean isOfflineInstrumentation() {
        return this.isOfflineInstrumentation;
    }

    public void setOfflineInstrumentation(boolean offlineInstrumentation) {
        this.isOfflineInstrumentation = offlineInstrumentation;
    }

    public Collection<String> getInstumentedClasses() {
        return this.instumentedClasses;
    }

    public void setInstumentedClasses(Collection<String> instumentedClasses) {
        this.instumentedClasses = new HashSet<>(instumentedClasses);
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
        return this.useCaching;
    }

    public void setUseCaching(boolean useCaching) {
        this.useCaching = useCaching;
    }

    public boolean usePersistentCache() {
        return this.persistentCache;
    }

    public void setPersistentCache(boolean persistentCache) {
        this.persistentCache = persistentCache;
    }

    public int getLayerThreshold() {
        return this.layerThreshold;
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

    public List<Purpose> getPurposes() { return this.purposes; }

    public List<Vendor> getVendors() { return this.vendors; }

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

    public List<FunctionCall> getPassThroughTaints() {
        return this.passThroughTaints;
    }

    public List<PropagateTaintInFunction> getPropagateTaintInFunctions() {
        return this.propagateTaintInFunctions;
    }

    public int shouldPropagateTaint(int acc, String owner, String name, String methodDescriptor) {
        MethodDeclaration md = new MethodDeclaration(acc, owner, name, methodDescriptor);
        for (PropagateTaintInFunction ptif : this.propagateTaintInFunctions) {
            if (ptif.getMethod().equals(md)) {
                return ptif.getArgIndex();
            }
        }
        return -1;
    }
    public boolean shouldPassThroughTaint(FunctionCall c) {
        for (FunctionCall fc : this.passThroughTaints) {
            if (fc.equals(c)) {
                return true;
            }
        }
        return false;
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
                    if(this.loggingEnabled) {

                        logger.info("Found converter for {} at index {}: {}", c, index, converter);
                    }
                    return converter;
                }
            }
        }
        return null;
    }

    public FunctionCall getConverterForReturnValue(FunctionCall c) {
        return this.getConverterForReturnValue(c, false);
    }

    public FunctionCall getConverterForReturnValue(FunctionCall c, boolean onlyAlwaysApply) {
        for (ReturnsGeneric rg : this.returnGeneric) {
            if (rg.getFunctionCall().equals(c)) {
                if (!(onlyAlwaysApply && !rg.isAlwaysApply())) {
                    String converterName = rg.getConverter();
                    FunctionCall converter = this.getConverter(converterName);
                    if(this.loggingEnabled) {
                        logger.info("Found converter for rv of {}: {}", c, converter);
                    }
                    return converter;
                }
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

    public void setTaintPersistence(boolean taintPersistence) {
        this.taintPersistence = taintPersistence;
    }

    public boolean hasTaintPersistence() {
        return this.taintPersistence;
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
        return this.abort;
    }

    public static boolean isInitialized() {
        return configuration != null;
    }

    public static Configuration getConfiguration() {
        if (configuration == null) {
            throw new IllegalStateException("Configuration not initialized! This should never happen!");
        }
        return configuration;
    }

    public static boolean isLoggingEnabled() {
        return !isInitialized() || getConfiguration().loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public List<String> getExcludedPackages() {
        return this.excludedPackages;
    }
    public List<String> getExcludedClasses() {
        return this.excludedClasses;
    }

    public List<String> getResourcesToInstrument() {
        return this.resourcesToInstrument;
    }

    public boolean isResourceToInstrument(String resource) {
        return this.resourcesToInstrument != null && this.resourcesToInstrument.contains(resource);
    }

    public static void parseAgent(String args) {
        Configuration config = AgentConfig.parseConfig(args);
        config.isOfflineInstrumentation = false;
        setConfiguration(config);
    }

    public static void setTestConfig(TaintMethod taintMethod) {
        if (configuration == null) {
            parseOffline(taintMethod);
        } else {
            getConfiguration().taintMethod = taintMethod;
        }
    }

    public static void parseOffline(TaintMethod method) {
        Configuration config = new Configuration();
        config.taintMethod = method;

        String collectStatsString = System.getenv("ASM_COLLECT_STATS");
        if (collectStatsString != null) {
            try {
                boolean collectStats = Boolean.parseBoolean(collectStatsString);
                config.collectStats = collectStats;
                logger.info("Set collect_stats to {}", collectStats);
            } catch (Exception ex) {
                logger.error("Couldn't parse ASM_COLLECT_STATS environment variable: {}", collectStatsString);
            }
        }

        String useCachingString = System.getenv("ASM_USE_CACHING");
        if (useCachingString != null) {
            try {
                boolean useCaching = Boolean.parseBoolean(useCachingString);
                config.useCaching = useCaching;
                logger.info("Set use_caching to {}", useCaching);
            } catch (Exception ex) {
                logger.error("Couldn't parse ASM_USE_CACHING environment variable: {}", collectStatsString);
            }
        }

        String layerThresholdString = System.getenv("ASM_LAYER_THRESHOLD");
        if (layerThresholdString != null) {
            try {
                int layerThreshold = Integer.parseInt(layerThresholdString);
                config.layerThreshold = layerThreshold;
                logger.info("Set layer_threshold to {}", layerThreshold);
            } catch (Exception ex) {
                logger.error("Couldn't parse ASM_LAYER_THRESHOLD environment variable: {}", collectStatsString);
            }
        }

        setConfiguration(config);
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
        return this.recursiveTainting;
    }

    public boolean handleTaintloss() {
        return this.taintlossHandler != null;
    }

    public TaintlossHandler getTaintlossHandler() {
        return this.taintlossHandler;
    }

    public void setTaintlossHandler(TaintlossHandler taintlossHandler) {
        this.taintlossHandler = taintlossHandler;
    }

    public boolean isShowWelcomeMessage() {
        return this.showWelcomeMessage;
    }

    public void setShowWelcomeMessage(boolean showWelcomeMessage) {
        this.showWelcomeMessage = showWelcomeMessage;
    }

    public boolean isHybridMode() {
        return this.isHybridMode;
    }

    public void setHybridMode(boolean hybridMode) {
        this.isHybridMode = hybridMode;
    }

    public boolean isParallel() {
        return this.isParallel;
    }

    public void setParallel(boolean parallel) {
        this.isParallel = parallel;
    }

    @JsonIgnore
    private boolean isSpeculativeDeactive() {
        return configuration.isOfflineInstrumentation
                || !configuration.speculativeInstrumentation
                || configuration.isHybridMode;
    }

    @JsonIgnore
    public boolean isSpeculativeActive() {
        return !this.isSpeculativeDeactive();
    }

    public void addExcludedClass(String clazzName) {
        if(!isClass(clazzName)) {
            System.out.printf("WARN: Trying to add %s as an excluded class. Classes should have the first letter capitalized and should not end with slashes!%n", clazzName);
            return;
        }
        this.excludedClasses.add(clazzName);
    }

    public void addExcludedPackage(String pkgName) {
        if(!isPackage(pkgName)) {
            System.out.printf("WARN: Trying to add %s as an excluded package. Packages should end with slashes!%n", pkgName);
            return;
        }
        this.excludedClasses.add(pkgName);
    }

    private static boolean isClass(String name) {
        if(isPackage(name)) {
            return false;
        }
        String[] parts = name.split("/");
        String clazzName = parts[parts.length-1];
        return clazzName.substring(0, 1).equals(clazzName.substring(0, 1).toUpperCase(Locale.ROOT));
    }

    private static boolean isPackage(String name) {
        return name.endsWith("/");
    }

    public String summary() {
        return "Configuration: " +
                this.sourceConfig.getSources().size() + " sources and " +
                this.sinkConfig.getSinks().size() + " sinks.";
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "taintMethod=" + this.taintMethod +
                ", showWelcomeMessage=" + this.showWelcomeMessage +
                ", jdkInheritanceBlacklist=" + this.jdkInheritanceBlacklist +
                ", useCaching=" + this.useCaching +
                ", layerThreshold=" + this.layerThreshold +
                ", collectStats=" + this.collectStats +
                ", abort=" + this.abort +
                ", taintlossHandler=" + this.taintlossHandler +
                ", isOfflineInstrumentation=" + this.isOfflineInstrumentation +
                ", verbose=" + this.verbose +
                ", taintPersistence=" + this.taintPersistence +
                ", loggingEnabled=" + this.loggingEnabled +
                ", recursiveTainting=" + this.recursiveTainting +
                ", sourceConfig=" + this.sourceConfig +
                ", sinkConfig=" + this.sinkConfig +
                ", vendors=" + this.vendors +
                ", purposes=" + this.purposes +
                ", converters=" + this.converters +
                ", returnGeneric=" + this.returnGeneric +
                ", takeGeneric=" + this.takeGeneric +
                ", blacklistedMainClasses=" + this.blacklistedMainClasses +
                ", excludedPackages=" + this.excludedPackages +
                ", excludedClasses=" + this.excludedClasses +
                ", resourcesToInstrument=" + this.resourcesToInstrument +
                ", passThroughTaints=" + this.passThroughTaints +
                ", propagateTaintInFunctions=" + this.propagateTaintInFunctions +
                '}';
    }

    public boolean isSpeculativeInstrumentation() {
        return this.speculativeInstrumentation;
    }

    public void setSpeculativeInstrumentation(boolean speculativeInstrumentation) {
        this.speculativeInstrumentation = speculativeInstrumentation;
    }

    public boolean validate() {
        for(FunctionCall fc : this.converters) {
            try {
                Method m = FunctionCall.toMethod(fc);
            } catch(ExceptionInInitializerError ex) {
                System.out.printf("Converter '%s' is invalid due to: %s%n", fc.getName(), ex.getCause().getMessage());
            } catch (ClassNotFoundException | NoSuchMethodException ex) {
                System.out.printf("Converter '%s' is invalid due to: %s%n", fc.getName(), ex.getMessage());
            }
        }
        return true;
    }
}

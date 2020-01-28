package de.tubs.cs.ias.asm_test.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import de.tubs.cs.ias.asm_test.FunctionCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlRootElement(name = "configuration")
public class Configuration {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public Configuration() {
	    this.verbose = false;
        this.sources = new ArrayList<FunctionCall>();
        this.sinkConfig = new SinkConfig();
        this.converters = new ArrayList<FunctionCall>();
        this.returnGeneric = new ArrayList<ReturnsGeneric>();
        this.takeGeneric = new ArrayList<TakesGeneric>();
	    this.blacklistedMainClasses = new ArrayList<String>();
    }
   
    public Configuration(boolean verbose, List<FunctionCall> sources, SinkConfig sinkConfig, List<FunctionCall> converters, List<ReturnsGeneric> returnGeneric, List<TakesGeneric> takeGeneric, List<String> blacklistedMainClasses) {
	    this.verbose = verbose;
        this.sources = sources;
        this.sinkConfig = sinkConfig;
        this.converters = converters;
        this.returnGeneric = returnGeneric;
        this.takeGeneric = takeGeneric;
        this.blacklistedMainClasses = blacklistedMainClasses;
    }

    public void append(Configuration other) {
        if (other != null) {
            this.verbose |= other.verbose;
            this.sources.addAll(other.sources);
            this.sinkConfig.append(other.sinkConfig);
            this.converters.addAll(other.converters);
            this.returnGeneric.addAll(other.returnGeneric);
            this.takeGeneric.addAll(other.takeGeneric);
            this.blacklistedMainClasses.addAll(other.blacklistedMainClasses);
        }
    }

    public void appendBlacklist(List<String> other) {
        if (blacklistedMainClasses != null) {
            this.blacklistedMainClasses.addAll(other);
        }
    }

    public List<FunctionCall> getSources() {
        return Collections.unmodifiableList(this.sources);
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
        for(FunctionCall fc : this.converters) {
            if(fc.getName().equals(name)) {
                return fc;
            }
        }
        return null;
    }

    public boolean needsParameterConversion(FunctionCall c) {
        for(TakesGeneric tg : this.takeGeneric) {
            if (tg.getFunctionCall().equals(c)) {
                return true;
            }
        }
        return false;
    }

    public FunctionCall getConverterForParameter(FunctionCall c, int index) {
        for(TakesGeneric tg : this.takeGeneric) {
            if(tg.getFunctionCall().equals(c) && tg.getIndex() == index) {
                String converterName = tg.getConverter();
                FunctionCall converter = this.getConverter(converterName);
                logger.info("Found converter for {} at index {}: {}", c, index, converter);
                return converter;
            }
        }
        return null;
    }

    public FunctionCall getConverterForReturnValue(FunctionCall c) {
        for(ReturnsGeneric rg : this.returnGeneric) {
            if(rg.getFunctionCall().equals(c)) {
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

    @XmlElement
    private boolean verbose = false;
    /**
     * All functions listed here return Strings that should be marked as tainted.
     */
    @JacksonXmlElementWrapper(localName = "sources") 
    @XmlElement(name = "source")
    private final List<FunctionCall> sources;
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
}

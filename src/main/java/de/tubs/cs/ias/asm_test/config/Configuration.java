package de.tubs.cs.ias.asm_test.config;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import de.tubs.cs.ias.asm_test.FunctionCall;
import de.tubs.cs.ias.asm_test.agent.AgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlRootElement(name = "configuration")
public class Configuration {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final Configuration instance = readBundledXmlConfiguration();

    public static Configuration readXmlConfiguration(InputStream stream) {
        ObjectMapper objectMapper = new XmlMapper();
        objectMapper.registerModule(new JaxbAnnotationModule());

        try {
            return objectMapper.readValue(stream, Configuration.class);
        } catch (JsonParseException | JsonMappingException e) {
            logger.error("Malformed configuration resource file, aborting!");
            // TODO: ugly exception, find more fitting one!
            throw new IllegalStateException("Missing configuration file\nAborting!", e);
        } catch (IOException e) {
            logger.error("Can't find the configuration resource file, aborting!");
            // TODO: ugly exception, find more fitting one!
            throw new IllegalStateException("Missing configuration file\nAborting!", e);
        }
    }

    private static Configuration readBundledXmlConfiguration() {
	    return readXmlConfiguration(Configuration.class.getClassLoader().getResourceAsStream("configuration.xml"));
    }

    public static Configuration readJsonConfiguration(InputStream stream) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(stream, Configuration.class);
        } catch (JsonParseException | JsonMappingException e) {
            logger.error("Malformed configuration resource file, aborting!");
            // TODO: ugly exception, find more fitting one!
            throw new IllegalStateException("Missing configuration file\nAborting!", e);
        } catch (IOException e) {
            logger.error("Can't find the configuration resource file, aborting!");
            // TODO: ugly exception, find more fitting one!
            throw new IllegalStateException("Missing configuration file\nAborting!", e);
        }
    }

    public Configuration() {
	this.verbose = false;
        this.sources = new ArrayList<FunctionCall>();
        this.sinkConfig = new SinkConfig();
        this.converters = new ArrayList<FunctionCall>();
        this.returnGeneric = new ArrayList<ReturnsGeneric>();
        this.takeGeneric = new ArrayList<TakesGeneric>();
	    this.mainMethodBlackList = new ArrayList<String>();
    }

    public void mergeAgentConfig(AgentConfig agentConfig) {
        this.verbose = agentConfig.isVerbose();
        this.mainMethodBlackList.addAll(agentConfig.getBlacklistedMainClasses());
    }
    
    public Configuration(boolean verbose, List<FunctionCall> sources, SinkConfig sinkConfig, List<FunctionCall> converters, List<ReturnsGeneric> returnGeneric, List<TakesGeneric> takeGeneric, List<String> mainMethodBlackList) {
	this.verbose = verbose;
        this.sources = sources;
        this.sinkConfig = sinkConfig;
        this.converters = converters;
        this.returnGeneric = returnGeneric;
        this.takeGeneric = takeGeneric;
        this.mainMethodBlackList = mainMethodBlackList;
    }

    public void append(Configuration other) {
        this.sources.addAll(other.sources);
        this.sinkConfig.append(other.sinkConfig);
        this.converters.addAll(other.converters);
        this.returnGeneric.addAll(other.returnGeneric);
        this.takeGeneric.addAll(other.takeGeneric);
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

    public List<String> getMainMethodBlackList() {
        return this.mainMethodBlackList;
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

    public boolean isVerbose() {
        return this.verbose;
    }

    public boolean isClassMainBlacklisted(String owner) {
        return this.mainMethodBlackList.contains(owner);
    }

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

    @JacksonXmlElementWrapper(localName = "mainMethodBlacklist")
    @XmlElement(name = "method")
    private final List<String> mainMethodBlackList;
}

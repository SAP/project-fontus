package de.tubs.cs.ias.asm_test.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import de.tubs.cs.ias.asm_test.FunctionCall;
import de.tubs.cs.ias.asm_test.agent.AgentConfig;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement(name = "configuration")
public class Configuration {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final Configuration instance = readConfiguration();

    private static Configuration readConfiguration() {
        ObjectMapper objectMapper = new XmlMapper();

        objectMapper.registerModule(new JaxbAnnotationModule());

        try (InputStream inputStream = Configuration.class
                .getClassLoader().getResourceAsStream("configuration.xml")) {
            return objectMapper.readValue(inputStream, Configuration.class);
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

    public void mergeAgentConfig(AgentConfig agentConfig) {
        this.verbose = agentConfig.isVerbose();
        this.mainMethodBlackList = agentConfig.getBlacklistedMainClasses();
        this.taintMethod = agentConfig.getTaintMethod();
        this.taintStringConfig = new TaintStringConfig(this.taintMethod);
        this.refactorFunctionCalls();
    }

    private void refactorFunctionCalls() {
        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("subpath", this.taintMethod.getSubPath());
        StringSubstitutor sub = new StringSubstitutor(replacements);

        List<FunctionCall> functions = this.converters.getFunction();

        List<FunctionCall> transformedFunctions = functions.stream().map(functionCall -> {
            String oldOwner = functionCall.getOwner();

            String newOwner = sub.replace(oldOwner);
            return new FunctionCall(functionCall.getOpcode(), newOwner, functionCall.getName(), functionCall.getDescriptor(), functionCall.isInterface());
        }).collect(Collectors.toList());

        this.converters = new Converters(transformedFunctions);
    }

    @JsonCreator
    public Configuration(@JsonProperty("sources") Sources sources, @JsonProperty("sinks") Sinks sinks, @JsonProperty("converters") Converters converters, @JsonProperty("returnGeneric") ReturnGeneric returnGeneric, @JsonProperty("takeGeneric") TakeGeneric takeGeneric) {
        this.sources = sources;
        this.sinks = sinks;
        this.converters = converters;
        this.returnGeneric = returnGeneric;
        this.takeGeneric = takeGeneric;
    }

    public Sources getSources() {
        return this.sources;
    }

    public Sinks getSinks() {
        return this.sinks;
    }

    public Converters getConverters() {
        return this.converters;
    }

    public ReturnGeneric getReturnGeneric() {
        return this.returnGeneric;
    }

    public TakeGeneric getTakeGeneric() {
        return this.takeGeneric;
    }

    private FunctionCall getConverter(String name) {
        for (FunctionCall fc : this.converters.getFunction()) {
            if (fc.getName().equals(name)) {
                return fc;
            }
        }
        return null;
    }

    public boolean needsParameterConversion(FunctionCall c) {
        for (TakesGeneric tg : this.takeGeneric.getFunction()) {
            if (tg.getFunctionCall().equals(c)) {
                return true;
            }
        }
        return false;
    }

    public FunctionCall getConverterForParameter(FunctionCall c, int index) {
        for (TakesGeneric tg : this.takeGeneric.getFunction()) {
            if (tg.getFunctionCall().equals(c) && tg.getIndex() == index) {
                String converterName = tg.getConverter();
                FunctionCall converter = this.getConverter(converterName);
                logger.info("Found converter for {} at index {}: {}", c, index, converter);
                return converter;
            }
        }
        return null;
    }

    public FunctionCall getConverterForReturnValue(FunctionCall c) {
        for (ReturnsGeneric rg : this.returnGeneric.getFunction()) {
            if (rg.getFunctionCall().equals(c)) {
                String converterName = rg.getConverter();
                FunctionCall converter = this.getConverter(converterName);
                logger.info("Found converter for rv of {}: {}", c, converter);
                return converter;
            }
        }
        return null;
    }


    /**
     * All functions listed here return Strings that should be marked as tainted.
     */
    @JacksonXmlElementWrapper(useWrapping = false)
    private final Sources sources;
    /**
     * All functions listed here consume Strings that need to be checked first.
     */
    @JacksonXmlElementWrapper(useWrapping = false)
    private final Sinks sinks;

    @JacksonXmlElementWrapper(useWrapping = false)
    private Converters converters;

    @JacksonXmlElementWrapper(useWrapping = false)
    private final ReturnGeneric returnGeneric;

    @JacksonXmlElementWrapper(useWrapping = false)
    private final TakeGeneric takeGeneric;

    public boolean isVerbose() {
        return this.verbose;
    }

    public boolean isClassMainBlacklisted(String owner) {
        return this.mainMethodBlackList.contains(owner);
    }


    private boolean verbose = false;
    private Collection<String> mainMethodBlackList = new ArrayList<>(0);
    private TaintMethod taintMethod;

    public TaintStringConfig getTaintStringConfig() {
        return taintStringConfig;
    }

    private TaintStringConfig taintStringConfig;

    public TaintMethod getTaintMethod() {
        return this.taintMethod;
    }
}

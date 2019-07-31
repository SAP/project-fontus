package de.tubs.cs.ias.asm_test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

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

    @JsonCreator
    public Configuration(@JsonProperty("sources") Sources sources, @JsonProperty("sinks") Sinks sinks) {
        this.sources = sources;
        this.sinks = sinks;
    }

    Sources getSources() {
        return this.sources;
    }

    Sinks getSinks() {
        return this.sinks;
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
}

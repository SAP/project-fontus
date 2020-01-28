package de.tubs.cs.ias.asm_test.config;

import de.tubs.cs.ias.asm_test.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

public class ConfigurationLoader {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

    public static Configuration defaultConfiguration() {
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

    public static Configuration loadAndMergeConfiguration(File f) {
        Configuration c = defaultConfiguration();
        c.append(loadConfigurationFrom(f));
        return c;
    }

    public static Configuration loadConfigurationFrom(File f) {
        Configuration c = null;
        try {
            if (f != null && f.exists() && f.isFile()) {
                FileInputStream fi = new FileInputStream(f);
                if (f.getName().endsWith(Constants.JSON_FILE_SUFFIX)) {
                    c = readJsonConfiguration(fi);
                } else if (f.getName().endsWith(Constants.XML_FILE_SUFFIX)) {
                    c = readXmlConfiguration(fi);
                } else {
                    logger.error("File {} ending not recognised!", f.getName());
                }
            } else {
                logger.error("File {} does not exist!", f.getName());
            }
        } catch (Exception e) {
            logger.error("Error opening file {}!", f.getName());
            logger.error("Exception opening configuration file", e);
        }
        return c;
    }

}
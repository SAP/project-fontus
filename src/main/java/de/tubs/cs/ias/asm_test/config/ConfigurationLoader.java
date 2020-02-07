package de.tubs.cs.ias.asm_test.config;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import de.tubs.cs.ias.asm_test.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationLoader {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static Configuration readXmlConfiguration(InputStream stream) {
        ObjectMapper objectMapper = new XmlMapper();
        objectMapper.registerModule(new JaxbAnnotationModule());
        return readFromStream(objectMapper, stream);
    }

    private static Configuration readFromStream(ObjectMapper mapper, InputStream stream) {
        try {
            return mapper.readValue(stream, Configuration.class);
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
        return readXmlConfiguration(Configuration.class.getClassLoader().getResourceAsStream(Constants.CONFIGURATION_XML_FILENAME));
    }

    public static Configuration readJsonConfiguration(InputStream stream) {
        ObjectMapper objectMapper = new ObjectMapper();
        return readFromStream(objectMapper, stream);
    }

    public static Configuration loadAndMergeConfiguration(File f) {
        Configuration c = defaultConfiguration();
        c.append(loadConfigurationFrom(f));
        return c;
    }

    public static Configuration loadConfigurationFrom(File f) {
        Configuration c = null;
        if (f == null) {
            logger.error("Null file input!");
        } else {
            try {
                if (f.exists() && f.isFile()) {
                    FileInputStream fi = new FileInputStream(f);
                    if (f.getName().endsWith(Constants.JSON_FILE_SUFFIX)) {
                        c = readJsonConfiguration(fi);
                    } else if (f.getName().endsWith(Constants.XML_FILE_SUFFIX)) {
                        c = readXmlConfiguration(fi);
                    } else {
                        logger.error("File {} ending not recognised!", f.getAbsolutePath());
                    }
                } else {
                    logger.error("File {} does not exist!", f.getAbsolutePath());
                }
            } catch (Exception e) {
                logger.error("Error opening file {}!", f.getAbsolutePath());
                logger.error("Exception opening configuration file", e);
            }
        }
        return c;
    }

    public static Configuration loadBlacklistFromFile(File f) {
        Configuration c = new Configuration();
        c.appendBlacklist(readFromFile(f));
        return c;
    }

    private static List<String> readFromFile(File input) {
        if (!input.isFile()) {
            logger.error("Suggested file '{}' does not exist!", input.getAbsolutePath());
            return new ArrayList<>(0);
        }
        try {
            return Files.readAllLines(input.toPath());
        } catch (IOException e) {
            logger.error("Exception while reading file: '{}':", input.getAbsolutePath(), e);
            return new ArrayList<>(0);
        }
    }

}

package de.tubs.cs.ias.asm_test.config;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.instrumentation.BlackListEntry;
import de.tubs.cs.ias.asm_test.utils.LogUtils;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;
import org.objectweb.asm.Opcodes;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationLoader {
    private static final ParentLogger logger = LogUtils.getLogger();

    public static Configuration readXmlConfiguration(InputStream stream) {
        System.out.println("w1");
        ObjectMapper objectMapper = new XmlMapper(new WstxInputFactory(), new WstxOutputFactory());
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
        System.out.println("w2");
        Configuration configuration = readXmlConfiguration(Configuration.class.getClassLoader().getResourceAsStream(Constants.CONFIGURATION_XML_FILENAME));
        configuration.setJdkInheritanceBlacklist(defaultJdkInheritanceBlacklistEntries());
        return configuration;
    }

    public static Map<String, List<BlackListEntry>> defaultJdkInheritanceBlacklistEntries() {
        Map<String, List<BlackListEntry>> map = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Configuration.class.getClassLoader().getResourceAsStream(Constants.JDK_INHERITANCE_BLACKLIST_FILENAME)));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String[] parts = line.split(",");
                String owner = parts[0];
                String name = parts[1];
                String descriptor = parts[2];
                List<BlackListEntry> methodList = map.computeIfAbsent(owner, (key) -> new ArrayList<>());
                methodList.add(new BlackListEntry(name, descriptor, Opcodes.ACC_PUBLIC));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return map;
    }

    public static Configuration readJsonConfiguration(InputStream stream) {
        ObjectMapper objectMapper = new ObjectMapper();
        return readFromStream(objectMapper, stream);
    }

    public static Configuration loadAndMergeConfiguration(File f, TaintMethod taintMethod) {
        Configuration c = defaultConfiguration();
        c.append(loadConfigurationFrom(f));
        c.setTaintMethod(taintMethod);
        c.transformConverters();
        return c;
    }

    public static Configuration loadConfigurationFrom(File f) {
        System.out.println("w4");
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
                        System.out.println(c.toString());
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

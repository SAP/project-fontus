package com.sap.fontus;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.ConfigurationLoader;
import com.sap.fontus.config.DataProtection;
import com.sap.fontus.config.TaintMethod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

@SuppressWarnings({"ClassIndependentOfModule", "ClassOnlyUsedInOneModule", "DuplicateStringLiteralInspection"})
class SourceSinkConfigTests {

    private static final String config_path = Constants.CONFIGURATION_XML_FILENAME;
    private static final String config_json = "configuration.json";
    private static final String config_with_handler_path = "configuration_with_handler.xml";
    private static final String config_with_data_protection_path = "configuration_with_data_protection.xml";
    private static final String config_black = "blacklist.json";

    @SuppressWarnings("SameParameterValue")
    private Configuration getConfiguration(String name) {
        return ConfigurationLoader.readXmlConfiguration(this.getClass().getResourceAsStream(name));
    }

    private Configuration getJsonConfiguration(String name) {
        return ConfigurationLoader.readJsonConfiguration(this.getClass().getResourceAsStream(name));
    }

    @BeforeAll
    static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    @Test
    void testBundledConfig() {
        assertNotNull(ConfigurationLoader.defaultConfiguration(), "Config shall not be null");
    }

    @Test
    void testLoadConfig() {
        Configuration config = this.getConfiguration(config_path);
        assertNotNull(config, "Config shall not be null");

        assertEquals(2, config.getSourceConfig().getSources().size(), "Shall parse the correct number of Sources");
        assertEquals(1, config.getSinkConfig().getSinks().size(), "Shall parse the correct number of Sinks");
        assertEquals(2, config.getConverters().size(), "Shall parse the correct number of converters");
        assertEquals(1, config.getReturnGeneric().size(), "Shall parse the correct number of returnGeneric entries");
        assertEquals(1, config.getTakeGeneric().size(), "Shall parse the correct number of takeGeneric entries");

        assertEquals("println", config.getSinkConfig().getSinks().get(0).getName(), "Println is the first defined sink!");
        assertEquals(1, config.getSinkConfig().getSinks().get(0).getParameters().size(), "Println (the first defined sink) has only 1 parameter.");
    }

    @Test
    void testAddConfig() {
        Configuration config = this.getConfiguration(config_path);
        Configuration config2 = this.getConfiguration(config_path);

        config.append(config2);

        assertTrue(config.isVerbose(), "Shall retrieve the correct value for verbose!");
        assertEquals(4, config.getSourceConfig().getSources().size(), "Shall parse the correct number of Sources");
        assertEquals(2, config.getSinkConfig().getSinks().size(), "Shall parse the correct number of Sinks");
        assertEquals(4, config.getConverters().size(), "Shall parse the correct number of converters");
        assertEquals(2, config.getReturnGeneric().size(), "Shall parse the correct number of returnGeneric entries");
        assertEquals(2, config.getTakeGeneric().size(), "Shall parse the correct number of takeGeneric entries");
        assertEquals(6, config.getBlacklistedMainClasses().size(), "Shall parse the correct number of blacklisted main classes");
    }

    @Test
    void testLoadConfigWithSourceHandler() {
        Configuration config = this.getConfiguration(config_with_handler_path);
        assertNotNull(config, "Config shall not be null");

        assertEquals(2, config.getSourceConfig().getSources().size(), "Shall parse the correct number of Sources");
        assertEquals(1, config.getSinkConfig().getSinks().size(), "Shall parse the correct number of Sinks");
        assertEquals(2, config.getConverters().size(), "Shall parse the correct number of converters");
        assertEquals(1, config.getReturnGeneric().size(), "Shall parse the correct number of returnGeneric entries");
        assertEquals(1, config.getTakeGeneric().size(), "Shall parse the correct number of takeGeneric entries");

        // Check the custom taint handler has been read properly
        assertEquals("next", config.getSourceConfig().getSources().get(0).getName(), "next is the first defined source");
        assertNotNull(config.getSourceConfig().getSources().get(0).getTaintHandler());
        assertFalse(config.getSourceConfig().getSources().get(0).getTaintHandler().isEmpty());

        assertEquals("com/sap/fontus/taintaware/unified/TaintHandler", config.getSourceConfig().getSources().get(0).getTaintHandler().getOwner());

    }

    @Test
    void testLoadConfigWithDataProtection() {
        Configuration config = this.getConfiguration(config_with_data_protection_path);
        assertNotNull(config, "Config shall not be null");

        assertEquals(2, config.getSourceConfig().getSources().size(), "Shall parse the correct number of Sources");
        assertEquals(1, config.getSinkConfig().getSinks().size(), "Shall parse the correct number of Sinks");
        assertEquals(2, config.getConverters().size(), "Shall parse the correct number of converters");
        assertEquals(1, config.getReturnGeneric().size(), "Shall parse the correct number of returnGeneric entries");
        assertEquals(1, config.getTakeGeneric().size(), "Shall parse the correct number of takeGeneric entries");

        assertEquals(2, config.getVendors().size());
        assertEquals("sap", config.getVendors().get(1).getName());
        assertEquals(4, config.getPurposes().size());
        assertEquals("logging", config.getPurposes().get(0).getName());

        DataProtection dp = config.getSinkConfig().getSinks().get(0).getDataProtection();
        assertEquals(1, dp.getAborts().size());
        assertEquals("throw", dp.getAborts().get(0));
        assertEquals(1, dp.getPurposes().size());
        assertEquals("processing", dp.getPurposes().get(0));
        assertEquals(1, dp.getVendors().size());
        assertEquals("acme", dp.getVendors().get(0));
    }

    @Test
    void testVerbose() {
        Configuration config = new Configuration();
        Configuration config2 = new Configuration();

        config2.setVerbose(true);
        config.append(config2);

        assertTrue(config.isVerbose(), "Shall retrieve the correct value for verbose!");
    }

    @Test
    void testWriteReadJsonConfig() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Configuration config = this.getConfiguration(config_path);
        String configAsJson = objectMapper.writeValueAsString(config);
        assertNotNull(configAsJson, "Config shall not be null");
        Configuration config2 = objectMapper.readValue(configAsJson, Configuration.class);
        assertNotNull(config2, "Config shall not be null");

        assertEquals(2, config.getSourceConfig().getSources().size(), "Shall parse the correct number of Sources");
        assertEquals(1, config.getSinkConfig().getSinks().size(), "Shall parse the correct number of Sinks");
        assertEquals(2, config.getConverters().size(), "Shall parse the correct number of converters");
        assertEquals(1, config.getReturnGeneric().size(), "Shall parse the correct number of returnGeneric entries");
        assertEquals(1, config.getTakeGeneric().size(), "Shall parse the correct number of takeGeneric entries");
        assertEquals(3, config.getBlacklistedMainClasses().size(), "Shall parse the correct number of blacklisted main classes");
    }

    @Test
    void testReadJsonConfig() {
        Configuration config = this.getJsonConfiguration(config_json);
        assertNotNull(config, "Config shall not be null");

        assertTrue(config.isVerbose(), "Shall retrieve the correct value for verbose!");
        assertEquals(2, config.getSourceConfig().getSources().size(), "Shall parse the correct number of Sources");
        assertEquals(1, config.getSinkConfig().getSinks().size(), "Shall parse the correct number of Sinks");
        assertEquals(2, config.getConverters().size(), "Shall parse the correct number of converters");
        assertEquals(1, config.getReturnGeneric().size(), "Shall parse the correct number of returnGeneric entries");
        assertEquals(1, config.getTakeGeneric().size(), "Shall parse the correct number of takeGeneric entries");
        assertEquals(4, config.getBlacklistedMainClasses().size(), "Shall parse the correct number of blacklisted main classes");
    }

    @Test
    void testReadBlacklist() {
        Configuration config = this.getJsonConfiguration(config_black);
        assertNotNull(config, "Config shall not be null");

        assertFalse(config.isVerbose(), "Shall retrieve the correct value for verbose!");
        assertEquals(0, config.getSourceConfig().getSources().size(), "Shall parse the correct number of Sources");
        assertEquals(0, config.getSinkConfig().getSinks().size(), "Shall parse the correct number of Sinks");
        assertEquals(0, config.getConverters().size(), "Shall parse the correct number of converters");
        assertEquals(0, config.getReturnGeneric().size(), "Shall parse the correct number of returnGeneric entries");
        assertEquals(0, config.getTakeGeneric().size(), "Shall parse the correct number of takeGeneric entries");
        assertEquals(3, config.getBlacklistedMainClasses().size(), "Shall parse the correct number of blacklisted main classes");
    }

    @Test
    void testMergeBlacklist() {
        Configuration config = this.getJsonConfiguration(config_black);
        assertNotNull(config, "Config shall not be null");

        config.append(ConfigurationLoader.defaultConfiguration());

        assertEquals(3, config.getBlacklistedMainClasses().size(), "Shall parse the correct number of blacklisted main classes");
    }

    @Test
    void parseConfigurationFileLoad() throws URISyntaxException {
        URL url = this.getClass().getResource(config_json);
        Configuration config = ConfigurationLoader.loadConfigurationFrom(new File(url.toURI()));

        assertNotNull(config, "Config shall not be null");

        assertTrue(config.isVerbose(), "Shall retrieve the correct value for verbose!");
        assertEquals(2, config.getSourceConfig().getSources().size(), "Shall parse the correct number of Sources");
        assertEquals(1, config.getSinkConfig().getSinks().size(), "Shall parse the correct number of Sinks");
        assertEquals(2, config.getConverters().size(), "Shall parse the correct number of converters");
        assertEquals(1, config.getReturnGeneric().size(), "Shall parse the correct number of returnGeneric entries");
        assertEquals(1, config.getTakeGeneric().size(), "Shall parse the correct number of takeGeneric entries");
        assertEquals(4, config.getBlacklistedMainClasses().size(), "Shall parse the correct number of blacklisted main classes");
    }
}

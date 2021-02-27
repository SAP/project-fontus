package com.sap.fontus;

import com.sap.fontus.agent.AgentConfig;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({ "DuplicateStringLiteralInspection", "SpellCheckingInspection", "ClassIndependentOfModule",
        "ClassOnlyUsedInOneModule" })
class AgentConfigTests {
    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.defaultTaintMethod());
    }
    @Test
    void parseNull() {
        Configuration cfg = AgentConfig.parseConfig(null);
        assertFalse(cfg.isVerbose(), "parsing null should result in verbose == false");
        assertEquals(new ArrayList<>(0), cfg.getBlacklistedMainClasses(), "parsing null should result in Empty List");
    }

    @Test
    void parseVerbose() {
        Configuration cfg = AgentConfig.parseConfig("verbose");
        assertTrue(cfg.isVerbose(), "parsing 'verbose' should result in verbose == true");
        assertEquals(new ArrayList<>(0), cfg.getBlacklistedMainClasses(),
                "parsing 'verbose' should result in Empty List");
    }

    @Test
    void parseRubbish() {
        Configuration cfg = AgentConfig.parseConfig("sup,son");
        assertFalse(cfg.isVerbose(), "parsing rubbish should result in verbose == false");
        assertEquals(new ArrayList<>(0), cfg.getBlacklistedMainClasses(),
                "parsing rubbish should result in Empty List");
    }

    @Test
    void parseConfig() throws URISyntaxException {
        URL url = AgentConfigTests.class.getResource("blacklist.json");
        String fname = Paths.get(url.toURI()).toString();
        Configuration cfg = AgentConfig.parseConfig("config=" + fname);
        assertFalse(cfg.isVerbose(), "parsing list only should result in verbose == false");
        assertEquals(3, cfg.getBlacklistedMainClasses().size(), "Should retrieve the correct number of blacklisted classes.");
        assertEquals("montypythonsflyingclass", cfg.getBlacklistedMainClasses().get(2), "Should retrieve the correct blacklisted classes.");
    }

    @Test
    void parseBlacklist() throws URISyntaxException {
        URL url = AgentConfigTests.class.getResource("TestList");
        String fname = Paths.get(url.toURI()).toString();
        Configuration cfg = AgentConfig.parseConfig("blacklisted_main_classes=" + fname);
        assertFalse(cfg.isVerbose(), "parsing list only should result in verbose == false");
        assertEquals(2, cfg.getBlacklistedMainClasses().size(), "Should retrieve the correct number of blacklisted classes.");
        assertEquals("world", cfg.getBlacklistedMainClasses().get(1), "Should retrieve the correct blacklisted classes.");
    }

    @Test
    void parseConfigAndBlacklist() throws URISyntaxException {
        URL url = AgentConfigTests.class.getResource("TestList");
        String fname = Paths.get(url.toURI()).toString();
        URL curl = AgentConfigTests.class.getResource(Constants.CONFIGURATION_XML_FILENAME);
        String cfname = Paths.get(curl.toURI()).toString();
    
        Configuration cfg = AgentConfig.parseConfig("blacklisted_main_classes=" + fname + ",config=" + cfname);

        assertTrue(cfg.isVerbose(), "parsing list only should result in verbose == true");
        assertEquals(5, cfg.getBlacklistedMainClasses().size(), "Should retrieve the correct number of blacklisted classes.");
    }

    @Test
    void parseCombined() throws URISyntaxException {
        URL url = AgentConfigTests.class.getResource("blacklist.json");
        String fname = Paths.get(url.toURI()).toString();
        Configuration cfg = AgentConfig.parseConfig("config=" + fname + ",verbose");
        assertTrue(cfg.isVerbose(), "parsing combined should result in verbose == true");
        assertEquals(3, cfg.getBlacklistedMainClasses().size(), "Should retrieve the correct number of blacklisted classes.");
        assertEquals("montypythonsflyingclass", cfg.getBlacklistedMainClasses().get(2), "Should retrieve the correct blacklisted classes.");
    }
}

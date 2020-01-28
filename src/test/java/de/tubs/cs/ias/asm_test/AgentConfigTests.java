package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.agent.AgentConfig;
import de.tubs.cs.ias.asm_test.config.Configuration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({ "DuplicateStringLiteralInspection", "SpellCheckingInspection", "ClassIndependentOfModule",
        "ClassOnlyUsedInOneModule" })
public class AgentConfigTests {
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
        Configuration cfg = AgentConfig.parseConfig("sup;son");
        assertFalse(cfg.isVerbose(), "parsing rubbish should result in verbose == false");
        assertEquals(new ArrayList<>(0), cfg.getBlacklistedMainClasses(),
                "parsing rubbish should result in Empty List");
    }

    @Test
    void parseConfig() throws IOException, URISyntaxException {
        URL url = AgentConfigTests.class.getResource("blacklist.json");
        String fname = Paths.get(url.toURI()).toString();
        Configuration cfg = AgentConfig.parseConfig("config=" + fname);
        assertFalse(cfg.isVerbose(), "parsing list only should result in verbose == false");
        assertEquals(3, cfg.getBlacklistedMainClasses().size());
        assertEquals("montypythonsflyingclass", cfg.getBlacklistedMainClasses().get(2));
    }

    @Test
    void parseBlacklist() throws IOException, URISyntaxException {
        URL url = AgentConfigTests.class.getResource("TestList");
        String fname = Paths.get(url.toURI()).toString();
        Configuration cfg = AgentConfig.parseConfig("blacklisted_main_classes=" + fname);
        assertFalse(cfg.isVerbose(), "parsing list only should result in verbose == false");
        assertEquals(2, cfg.getBlacklistedMainClasses().size());
        assertEquals("world", cfg.getBlacklistedMainClasses().get(1));
    }

    @Test
    void parseConfigAndBlacklist() throws IOException, URISyntaxException {
        URL url = AgentConfigTests.class.getResource("TestList");
        String fname = Paths.get(url.toURI()).toString();
        URL curl = AgentConfigTests.class.getResource("configuration.xml");
        String cfname = Paths.get(curl.toURI()).toString();
    
        Configuration cfg = AgentConfig.parseConfig("blacklisted_main_classes=" + fname + ";config=" + cfname);

        assertTrue(cfg.isVerbose(), "parsing list only should result in verbose == true");
        assertEquals(5, cfg.getBlacklistedMainClasses().size());
    }

    @Test
    void parseCombined() throws IOException, URISyntaxException {
        URL url = AgentConfigTests.class.getResource("blacklist.json");
        String fname = Paths.get(url.toURI()).toString();
        Configuration cfg = AgentConfig.parseConfig("config=" + fname + ";verbose");
        assertTrue(cfg.isVerbose(), "parsing combined should result in verbose == true");
        assertEquals(3, cfg.getBlacklistedMainClasses().size());
        assertEquals("montypythonsflyingclass", cfg.getBlacklistedMainClasses().get(2));
    }
}

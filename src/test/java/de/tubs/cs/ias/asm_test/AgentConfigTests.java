package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.agent.AgentConfig;
import de.tubs.cs.ias.asm_test.config.Configuration;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"DuplicateStringLiteralInspection", "SpellCheckingInspection", "ClassIndependentOfModule", "ClassOnlyUsedInOneModule"})
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
        assertEquals(new ArrayList<>(0), cfg.getBlacklistedMainClasses(), "parsing 'verbose' should result in Empty List");
    }
    @Test
    void parseRubbish() {
        Configuration cfg = AgentConfig.parseConfig("sup;son");
        assertFalse(cfg.isVerbose(), "parsing rubbish should result in verbose == false");
        assertEquals(new ArrayList<>(0), cfg.getBlacklistedMainClasses(), "parsing rubbish should result in Empty List");
    }

    @Test
    void parseBlacklist() throws IOException {
        URL url = AgentConfigTests.class.getResource("/TestList");
        String fname = url.getPath();
        Configuration cfg = AgentConfig.parseConfig("blacklisted_main_classes=" + fname);
        File input = new File(fname);

        List<String> actual = Files.readAllLines(input.toPath());
        assertIterableEquals(cfg.getBlacklistedMainClasses(), actual , "Should be able to resolve list from resources");
        assertFalse(cfg.isVerbose(), "parsing list only should result in verbose == false");

    }

    @Test
    void parseCombined() throws IOException {
        URL url = AgentConfigTests.class.getResource("/TestList");
        String fname = url.getPath();
        Configuration cfg = AgentConfig.parseConfig("blacklisted_main_classes=" + fname + ";verbose");
        File input = new File(fname);

        List<String> actual = Files.readAllLines(input.toPath());
        assertIterableEquals(cfg.getBlacklistedMainClasses(), actual , "Should be able to resolve list from resources");
        assertTrue(cfg.isVerbose(), "parsing combined should result in verbose == true");
    }
}

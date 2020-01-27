package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.agent.AgentConfig;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"DuplicateStringLiteralInspection", "SpellCheckingInspection", "ClassIndependentOfModule", "ClassOnlyUsedInOneModule"})
public class AgentConfigTests {
    @Test
    void parseNull() {
        AgentConfig cfg = AgentConfig.parseConfig(null);
        assertFalse(cfg.isVerbose(), "parsing null should result in verbose == false");
        assertEquals(new ArrayList<>(0), cfg.getBlacklistedMainClasses(), "parsing null should result in Empty List");
    }

    @Test
    void parseVerbose() {
        AgentConfig cfg = AgentConfig.parseConfig("verbose");
        assertTrue(cfg.isVerbose(), "parsing 'verbose' should result in verbose == true");
        assertEquals(new ArrayList<>(0), cfg.getBlacklistedMainClasses(), "parsing 'verbose' should result in Empty List");
    }
    @Test
    void parseRubbish() {
        AgentConfig cfg = AgentConfig.parseConfig("sup;son");
        assertFalse(cfg.isVerbose(), "parsing rubbish should result in verbose == true");
        assertEquals(new ArrayList<>(0), cfg.getBlacklistedMainClasses(), "parsing rubbish should result in Empty List");
    }

    @Test
    void parseBlacklist() throws IOException {
        AgentConfig cfg = AgentConfig.parseConfig("blacklisted_main_classes=/etc/lsb-release");
        File input = new File("/etc/lsb-release");

        List<String> actual = Files.readAllLines(input.toPath());
        assertIterableEquals(cfg.getBlacklistedMainClasses(), actual , "Should be able to resolve list from resources");
        assertFalse(cfg.isVerbose(), "parsing list only should result in verbose == false");

    }

    @Test
    void parseCombined() throws IOException {
        AgentConfig cfg = AgentConfig.parseConfig("blacklisted_main_classes=/etc/lsb-release;verbose");
        File input = new File("/etc/lsb-release");

        List<String> actual = Files.readAllLines(input.toPath());
        assertIterableEquals(cfg.getBlacklistedMainClasses(), actual , "Should be able to resolve list from resources");
        assertTrue(cfg.isVerbose(), "parsing combined only should result in verbose == true");
    }
}

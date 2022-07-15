package com.sap.fontus.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationTests {
    private Configuration getConfiguration(String name) {
        return ConfigurationLoader.readXmlConfiguration(this.getClass().getResourceAsStream(name));
    }
    private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private static final PrintStream originalOut = System.out;
    private static final PrintStream originalErr = System.err;

    @BeforeEach
    void clearStreams() {
        outContent.reset();
        errContent.reset();
    }
    @BeforeAll
    static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterAll
    static void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testPassThroughTaints() {
        Configuration config = this.getConfiguration("configuration_passThroughTaints.xml");
        assertEquals(config.getPassThroughTaints().size(), 2);
    }

    @Test
    void testExcludedPackages() {
        Configuration config = this.getConfiguration("configuration_excluded_packages.xml");
        assertEquals(config.getExcludedPackages().size(), 4);
    }

    @Test
    void testExcludedClasses() {
        Configuration config = this.getConfiguration("configuration_excluded_classes.xml");
        assertEquals(config.getExcludedClasses().size(), 1);
    }

    @Test
    void testExclusions() {
        Configuration config = this.getConfiguration("configuration_exclusions.xml");
        assertEquals(config.getExcludedClasses().size(), 1);
        assertEquals(config.getExcludedPackages().size(), 4);
    }

    @Test
    void testAddingExcludedClass() {
        Configuration config = this.getConfiguration("configuration_exclusions.xml");
        assertEquals(config.getExcludedClasses().size(), 1);
        assertEquals(config.getExcludedPackages().size(), 4);
        config.addExcludedClass("a/b/c/D");
        assertEquals(config.getExcludedClasses().size(), 2);
    }

    @Test
    void testAddingClassAsPackage() {
        Configuration config = this.getConfiguration("configuration_exclusions.xml");
        assertEquals(config.getExcludedClasses().size(), 1);
        assertEquals(config.getExcludedPackages().size(), 4);
        config.addExcludedPackage("a/b/c/D");
        assertEquals(config.getExcludedPackages().size(), 4);
        assertEquals("WARN: Trying to add a/b/c/D as an excluded package. Packages should end with slashes!\n", outContent.toString());
    }

    @Test
    void testSourceWithAllowedCallers() {
        Configuration config = this.getConfiguration("configuration_source_allowed_callers.xml");
        List<Source> sources = config.getSourceConfig().getSources();
        assertEquals(sources.size(), 2);
        Source first = config.getSourceConfig().getSourceWithName("next");
        assertEquals(2, first.getAllowedCallers().size());
        assertEquals(2, first.getPassLocals().size());
        Source second = config.getSourceConfig().getSourceWithName("nextLine");
        assertEquals(0, second.getAllowedCallers().size());
    }

    @Test
    void testAddingPackageWithoutSlashes() {
        Configuration config = this.getConfiguration("configuration_exclusions.xml");
        assertEquals(config.getExcludedClasses().size(), 1);
        assertEquals(config.getExcludedPackages().size(), 4);
        config.addExcludedPackage("a/b/c");
        assertEquals(config.getExcludedPackages().size(), 4);
        assertEquals("WARN: Trying to add a/b/c as an excluded package. Packages should end with slashes!\n", outContent.toString());
    }

    @Test
    void testAddingPackageWithoutSlashAsClass() {
        Configuration config = this.getConfiguration("configuration_exclusions.xml");
        assertEquals(config.getExcludedClasses().size(), 1);
        assertEquals(config.getExcludedPackages().size(), 4);
        config.addExcludedClass("a/b/d");
        assertEquals(config.getExcludedClasses().size(), 1);
        assertEquals("WARN: Trying to add a/b/d as an excluded class. Classes should have the first letter capitalized and should not end with slashes!\n", outContent.toString());
    }
}

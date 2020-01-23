package de.tubs.cs.ias.asm_test;

import org.junit.jupiter.api.Test;

import de.tubs.cs.ias.asm_test.config.Configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings({"ClassIndependentOfModule", "ClassOnlyUsedInOneModule"})
public class SourceSinkConfigTests {


    private static String config_path = "configuration.xml";

    private Configuration getConfiguration(String name) {
	return Configuration.readXmlConfiguration(this.getClass().getResourceAsStream(name));
    }

    @Test
    public void testLoadConfig() {
	Configuration config = this.getConfiguration(config_path);
	assertNotNull(config);

	assertEquals(2, config.getSources().size());
	assertEquals(1, config.getSinks().size());
	assertEquals(2, config.getConverters().size());
	assertEquals(1, config.getReturnGeneric().size());
	assertEquals(1, config.getTakeGeneric().size());
    }
}

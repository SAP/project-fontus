package de.tubs.cs.ias.asm_test;

import org.junit.jupiter.api.Test;

import de.tubs.cs.ias.asm_test.config.Configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

@SuppressWarnings({ "ClassIndependentOfModule", "ClassOnlyUsedInOneModule" })
public class SourceSinkConfigTests {

    private static String config_path = "configuration.xml";
    private static String config_json = "configuration.json";

    private Configuration getConfiguration(String name) {
        return Configuration.readXmlConfiguration(this.getClass().getResourceAsStream(name));
    }

    private Configuration getJsonConfiguration(String name) {
        return Configuration.readJsonConfiguration(this.getClass().getResourceAsStream(name));
    }

    @Test
    public void testBundledConfig() {
        assertNotNull(Configuration.instance);
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

        assertEquals("println", config.getSinks().get(0).getName());
        assertEquals(1, config.getSinks().get(0).getParameters().size());
    }

    @Test
    public void testAddConfig() {
        Configuration config = this.getConfiguration(config_path);
        Configuration config2 = this.getConfiguration(config_path);

        config.append(config2);

        assertEquals(4, config.getSources().size());
        assertEquals(2, config.getSinks().size());
        assertEquals(4, config.getConverters().size());
        assertEquals(2, config.getReturnGeneric().size());
        assertEquals(2, config.getTakeGeneric().size());
    }

    @Test
    public void testWriteReadJsonConfig() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Configuration config = this.getConfiguration(config_path);
        String configAsJson = objectMapper.writeValueAsString(config);
        assertNotNull(configAsJson);
        Configuration config2 = objectMapper.readValue(configAsJson, Configuration.class);
        assertNotNull(config2);

        assertEquals(2, config.getSources().size());
        assertEquals(1, config.getSinks().size());
        assertEquals(2, config.getConverters().size());
        assertEquals(1, config.getReturnGeneric().size());
        assertEquals(1, config.getTakeGeneric().size());
    }

    @Test
    public void testReadJsonConfig() throws JsonProcessingException {
        Configuration config = this.getJsonConfiguration(config_json);              
        assertNotNull(config);
    }
}

package de.tubs.cs.ias.asm_test.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.config.ConfigurationLoader;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.*;

public class AgentConfig {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static Configuration parseConfig(String args) {
        if (args == null) {
            return ConfigurationLoader.defaultConfiguration();
        }
        try (Scanner sc = new Scanner(args)) {
            sc.useDelimiter(";");
            Collection<String> parts = new ArrayList<>();
            while (sc.hasNext()) {
                String part = sc.next();
                parts.add(part);
            }
            return parseParts(parts);
        }
    }

    private static Configuration parseParts(Iterable<String> parts) {
        Configuration c = ConfigurationLoader.defaultConfiguration();
        boolean verbose = false;
        for (String part : parts) {
            if ("verbose".equals(part)) {
                verbose = true;
            }
            if (part.startsWith("config=")) {
                String filename = afterEquals(part);
                Configuration cmdlineconfig = ConfigurationLoader.loadConfigurationFrom(new File(filename));
                c.append(cmdlineconfig);
            }
            if (part.startsWith("blacklisted_main_classes=")) {
                String filename = afterEquals(part);
                Configuration blacklist = ConfigurationLoader.loadBlacklistFromFile(new File(filename));
                c.append(blacklist);
            }
        }
        if (c == null) {
            c = ConfigurationLoader.defaultConfiguration();
        }
        c.setVerbose(verbose || c.isVerbose());

        return c;
    }

    private static String afterEquals(String part) {
        int idx = part.indexOf('=');
        return part.substring(idx + 1);
    }

}

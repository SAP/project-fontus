package de.tubs.cs.ias.asm_test.agent;

import de.tubs.cs.ias.asm_test.config.TaintMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.*;

public class AgentConfig {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final boolean verbose;
    private final List<String> blacklist;
    private final TaintMethod taintMethod;

    public boolean isVerbose() {
        return this.verbose;
    }

    public List<String> getBlacklistedMainClasses() {
        return Collections.unmodifiableList(this.blacklist);
    }

    public TaintMethod getTaintMethod() {
        return this.taintMethod;
    }

    private AgentConfig() {
        this.verbose = false;
        this.blacklist = new ArrayList<>();
        this.taintMethod = TaintMethod.defaultTaintMethod();
    }

    private AgentConfig(boolean verbose, List<String> blacklist, TaintMethod taintMethod) {
        this.verbose = verbose;
        this.blacklist = blacklist;
        this.taintMethod = taintMethod;
    }

    public static AgentConfig parseConfig(String args) {
        if (args == null) {
            return new AgentConfig();
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

    private static AgentConfig parseParts(Iterable<String> parts) {
        boolean verbose = false;
        List<String> blacklist = new ArrayList<>();
        TaintMethod taintMethod = TaintMethod.defaultTaintMethod();
        for(String part : parts) {
            if("verbose".equals(part)) {
                verbose = true;
            }
            if(part.startsWith("taintmethod=")) {
                String taintMethodArgName = afterEquals(part);
                taintMethod = TaintMethod.getTaintMethodByArgumentName(taintMethodArgName);
            }
            if(part.startsWith("blacklisted_main_classes=")) {
                String filename = afterEquals(part);
                blacklist = readFromFile(filename);
            }
        }
        return new AgentConfig(verbose, blacklist, taintMethod);
    }

    private static List<String> readFromFile(String fileName) {
        File input = new File(fileName);
        if (!input.isFile()) {
            logger.error("Suggested file '{}' does not exist!", fileName);
            return new ArrayList<>(0);
        }
        try {
            return Files.readAllLines(input.toPath());
        } catch (IOException e) {
            logger.error("Exception while reading file: '{}':", fileName, e);
            return new ArrayList<>(0);
        }
    }

    private static String afterEquals(String part) {
        int idx = part.indexOf('=');
        return part.substring(idx + 1);
    }

}

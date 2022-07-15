package com.sap.fontus.asm.resolver;

import com.sap.fontus.agent.InstrumentationConfiguration;
import com.sap.fontus.utils.Utils;

import java.io.*;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class OfflineClassResolver implements IClassResolver {
    private final AgentClassResolver agentClassResolver;
    private Map<String, byte[]> classes;

    public OfflineClassResolver(AgentClassResolver resolver) {
        this.agentClassResolver = resolver;
    }

    private synchronized void initialize() {
        if (this.isInitialized()) {
            return;
        }

        InstrumentationConfiguration configuration = InstrumentationConfiguration.getInstance();
        File input = configuration.getInput();

        Map<String, byte[]> classData = new ConcurrentHashMap<>();

        this.walkInput(input, classData);

        this.classes = Collections.unmodifiableMap(classData);
    }

    private boolean isInitialized() {
        return this.classes != null;
    }

    @Override
    public Optional<byte[]> resolve(String className) {
        if (!this.isInitialized()) {
            this.initialize();
        }

        if (className.startsWith("[L") && className.endsWith(";")) {
            className = className.substring(2, className.length() - 1);
        }
        className = Utils.dotToSlash(className);

        byte[] bytes = this.classes.get(className);

        if (bytes != null) {
            return Optional.of(bytes);
        }

        return this.agentClassResolver.resolve(className);
    }

    private void walkInput(File input, Map<String, byte[]> classes) {
        if (input == null) {
            return;
        }
        if (input.exists()) {
            if (input.isDirectory()) {
                File[] inputs = input.listFiles();
                if (inputs != null) {
                    for (File file : inputs) {
                        this.walkInput(file, classes);
                    }
                }
            } else {
                JarClassResolver jarClassResolver = new JarClassResolver();

                try {
                    jarClassResolver.loadClassesFrom(new FileInputStream(input)).forEach(classes::putIfAbsent);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

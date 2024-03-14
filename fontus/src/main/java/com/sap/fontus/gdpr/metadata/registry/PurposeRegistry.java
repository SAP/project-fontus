package com.sap.fontus.gdpr.metadata.registry;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.gdpr.metadata.Purpose;
import com.sap.fontus.gdpr.metadata.simple.SimplePurpose;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PurposeRegistry {

    private static PurposeRegistry instance;
    private final Map<String, Purpose> purposes;
    private int counter = 0;

    public PurposeRegistry() {
        this.purposes = new ConcurrentHashMap<>(32);
    }
    private static boolean isPopulated;

    private synchronized void populateFromConfiguration(Configuration c) {
        if (!isPopulated) {
            for (com.sap.fontus.config.Purpose p : c.getPurposes()) {
                this.getOrRegisterObject(p.getName(), p.getDescription(), p.getLegal());
            }
            isPopulated = true;
        }
    }

    public Purpose getOrRegisterObject(String name, String description, String legal) {
        return this.purposes.computeIfAbsent(name, (ignored) -> {
            this.counter++;
            return new SimplePurpose(this.counter, name, description, legal);
        });
    }

    public Purpose getOrRegisterObject(String name) {
        return this.purposes.computeIfAbsent(name, (ignored) -> {
            this.counter++;
            return new SimplePurpose(this.counter, name);
        });
    }

    public Purpose get(String name) {
        return this.purposes.get(name);
    }

    protected Purpose getNewObject(String name, int id) {
        return new SimplePurpose(id, name);
    }

    public static synchronized PurposeRegistry getInstance() {
        if (instance == null) {
            instance = new PurposeRegistry();
            if (Configuration.isInitialized()) {
                instance.populateFromConfiguration(Configuration.getConfiguration());
            }
        }
        return instance;
    }
}


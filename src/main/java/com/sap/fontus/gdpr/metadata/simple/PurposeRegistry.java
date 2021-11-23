package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.gdpr.metadata.Purpose;
import com.sap.fontus.utils.GenericRegistry;

public class PurposeRegistry extends GenericRegistry<Purpose> {

    private static PurposeRegistry instance;
    private static boolean isPopulated;

    private synchronized void populateFromConfiguration(Configuration c) {
        if (!isPopulated) {
            for (com.sap.fontus.config.Purpose p : c.getPurposes()) {
                this.getOrRegisterObject(p.getName(), p.getDescription(), p.getLegal());
            }
            isPopulated = true;
        }
    }

    public synchronized Purpose getOrRegisterObject(String name, String description, String legal) {
        for (Purpose o : objects) {
            if (o.getName().equals(name)) {
                return o;
            }
        }
        this.counter++;
        Purpose o = new SimplePurpose(this.counter, name, description, legal);
        this.objects.add(o);
        return o;
    }

    @Override
    protected Purpose getNewObject(String name, int id) {
        return new SimplePurpose(id, name);
    }

    public static synchronized PurposeRegistry getInstance() {
        if (instance == null) {
            instance = new PurposeRegistry();
            instance.populateFromConfiguration(Configuration.getConfiguration());
        }
        return instance;
    }
}


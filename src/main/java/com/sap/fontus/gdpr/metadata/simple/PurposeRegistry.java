package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.Purpose;
import com.sap.fontus.utils.GenericRegistry;

public class PurposeRegistry extends GenericRegistry<Purpose> {

    private static PurposeRegistry instance;

    public synchronized Purpose getOrRegisterTaintSource(String name, String description, String legal) {
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
        }
        return instance;
    }
}


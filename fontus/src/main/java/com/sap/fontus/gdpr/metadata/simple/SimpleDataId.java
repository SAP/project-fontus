package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.DataId;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class SimpleDataId implements DataId {

    private final UUID uuid;

    public SimpleDataId(UUID uuid) {
        this.uuid = uuid;
    }

    public SimpleDataId(String uuid) {
        this.uuid = UUID.fromString(uuid);
    }

    public SimpleDataId() {
        this.uuid = new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong());
    }

    public UUID getUUID() {
        return this.uuid;
    }

}

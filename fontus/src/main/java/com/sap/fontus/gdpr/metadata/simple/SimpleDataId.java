package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.DataId;

import java.util.UUID;

public class SimpleDataId implements DataId {

    private UUID uuid;

    public SimpleDataId(UUID uuid) {
        this.uuid = uuid;
    }

    public SimpleDataId(String uuid) {
        this.uuid = UUID.fromString(uuid);
    }

    public SimpleDataId() {
        uuid = UUID.randomUUID();
    }

    public UUID getUUID() {
        return uuid;
    }

}

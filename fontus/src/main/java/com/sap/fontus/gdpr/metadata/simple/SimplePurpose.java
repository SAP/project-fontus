package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.PurposeBase;

public class SimplePurpose extends PurposeBase {

    private int id;
    private String name;
    private String description;
    private String legal;

    public SimplePurpose() {
        this.id = -1;
        this.name = "DEFAULT";
        this.description = "DEFAULT";
        this.legal = "DEFAULT";
    }

    public SimplePurpose(int id, String name, String description, String legal) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.legal = legal;
    }

    public SimplePurpose(int id, String name) {
        this.id = id;
        this.name = name;
        this.description = "";
        this.legal = "";
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getLegalDescription() {
        return this.legal;
    }

    public void setDescription(String description) { this.description = description; }

    public void setLegalDescription(String legal) { this.legal = legal; }


}

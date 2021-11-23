package com.sap.fontus.config;

import javax.xml.bind.annotation.XmlElement;

public class Vendor {

    @XmlElement
    private final String name;

    public Vendor(String name) {
        this.name = name;
    }

    public Vendor() {
        this.name = new String();
    }

    public String getName() {
        return this.name;
    }

}

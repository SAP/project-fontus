package com.sap.fontus.config;

import javax.xml.bind.annotation.XmlElement;

public class Vendor {

    @XmlElement
    private final String name;

    public Vendor(String name) {
        this.name = name;
    }

    public Vendor() {
        this.name = "";
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "Vendor{" +
                "name='" + this.name + '\'' +
                '}';
    }
}

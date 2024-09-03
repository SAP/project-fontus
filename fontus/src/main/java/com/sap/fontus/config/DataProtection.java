package com.sap.fontus.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DataProtection {

    @JacksonXmlElementWrapper(localName = "vendors")
    @XmlElement(name = "vendor")
    private final List<String> vendors;

    @JacksonXmlElementWrapper(localName = "purposes")
    @XmlElement(name = "purpose")
    private final List<String> purposes;

    @JacksonXmlElementWrapper(localName = "aborts")
    @XmlElement(name = "abort")
    private final List<String> aborts;

    public DataProtection() {
        this.vendors = new ArrayList<>();
        this.purposes = new ArrayList<>();
        this.aborts = new ArrayList<>();
    }

    public DataProtection(List<String> vendors, List<String> purposes, List<String> aborts) {
        this.vendors = vendors.stream().map(String::trim).toList();
        this.purposes = purposes.stream().map(String::trim).toList();
        this.aborts = aborts.stream().map(String::trim).toList();
    }

    public List<String> getVendors() {
        return Collections.unmodifiableList(this.vendors);
    }

    public List<String> getPurposes() {
        return Collections.unmodifiableList(this.purposes);
    }

    public List<String> getAborts() {
        return Collections.unmodifiableList(this.aborts);
    }

    @Override
    public String toString() {
        return "DataProtection{" +
                "vendors=" + this.vendors +
                ", purposes=" + this.purposes +
                ", aborts=" + this.aborts +
                '}';
    }
}

package com.sap.fontus.config;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement
public class MethodDeclaration {
    @XmlElement
    private final int access;
    @XmlElement
    private final String owner;
    @XmlElement
    private final String name;

    @XmlElement
    private final String descriptor;

    public MethodDeclaration() {
        this.access = -1;
        this.owner = null;
        this.name = null;
        this.descriptor = null;
    }

    public MethodDeclaration(int access, String owner, String name, String descriptor) {
        this.access = access;
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
    }

    public int getAccess() {
        return this.access;
    }

    public String getOwner() {
        return this.owner;
    }

    public String getName() {
        return this.name;
    }

    public String getDescriptor() {
        return this.descriptor;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        MethodDeclaration that = (MethodDeclaration) obj;
        return this.access == that.access && Objects.equals(this.owner, that.owner) && Objects.equals(this.name, that.name) && Objects.equals(this.descriptor, that.descriptor);
    }

    @Override
    public String toString() {
        return String.format("MethodDeclaration{access=%d, owner='%s', name='%s', descriptor='%s'}", this.access, this.owner, this.name, this.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.access, this.owner, this.name, this.descriptor);
    }
}

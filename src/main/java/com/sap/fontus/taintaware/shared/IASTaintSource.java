package com.sap.fontus.taintaware.shared;

import com.sap.fontus.utils.NamedObject;

import java.io.*;
import java.util.*;

/**
 * Created by d059349 on 15.07.17.
 */
public class IASTaintSource implements NamedObject, Externalizable {

    private String name;
    private int id;

    public IASTaintSource() {
        this.name = "DEFAULT";
        this.id = -1;
    }

    public IASTaintSource(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other.getClass() != this.getClass()) return false;

        IASTaintSource source = (IASTaintSource) other;

        if (!name.equals(source.name)) {
            return false;
        }
        if (id != source.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "IASTaintSource{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}';
    }

    public int getId() {
        return this.id;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(this.name);
        out.writeInt(this.id);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.name = in.readUTF();
        this.id = in.readInt();
    }
}

package com.sap.fontus.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "position")
public class Position {

    @XmlElement
    private String className;

    @XmlElement
    private String methodName;

    @XmlElement
    private int javaSourcePosition;

    public Position() {
        this.className = "";
        this.methodName = "";
        this.javaSourcePosition = 0;
    }

    public Position(String className, String methodName, int javaSourcePosition) {
        this.className = className;
        this.methodName = methodName;
        this.javaSourcePosition = javaSourcePosition;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getJavaSourcePosition() {
        return this.javaSourcePosition;
    }

    public void setJavaSourcePosition(int javaSourcePosition) {
        this.javaSourcePosition = javaSourcePosition;
    }

    @Override
    public String toString() {
        return "Position{" +
                "class=" + this.className +
                ", method=" + this.methodName +
                ", position=" + this.javaSourcePosition +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Position)) {
            return false;
        }
        Position position = (Position) o;
        return this.javaSourcePosition == position.javaSourcePosition && this.className.equals(position.className) && this.methodName.equals(position.methodName);
    }
}
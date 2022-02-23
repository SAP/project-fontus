package com.sap.fontus.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

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
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getJavaSourcePosition() {
        return javaSourcePosition;
    }

    public void setJavaSourcePosition(int javaSourcePosition) {
        this.javaSourcePosition = javaSourcePosition;
    }

    @Override
    public String toString() {
        return "Position{" +
                "class=" + className +
                ", method=" + methodName +
                ", position=" + javaSourcePosition +
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
        return javaSourcePosition == position.javaSourcePosition &&
                Objects.equals(className, position.className) &&
                Objects.equals(methodName, position.methodName);
    }
}
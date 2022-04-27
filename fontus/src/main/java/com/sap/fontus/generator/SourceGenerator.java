package com.sap.fontus.generator;

import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.Source;
import com.sap.fontus.utils.Utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SourceGenerator extends AbstractGenerator {
    private String className;

    public SourceGenerator(String className) {
        this.className = Utils.slashToDot(className);
    }

    public List<Source> readSources(boolean isObject) {
        List<Source> sources = new ArrayList<>();
        try {
            Class cls = Class.forName(this.className);
            Method[] methods = cls.getMethods();
            for (Method method : methods) {
                if (String.class.isAssignableFrom(method.getReturnType()) || (isObject && method.getReturnType().isAssignableFrom(Object.class))) {
                    Source source = new Source(generateName(method), FunctionCall.fromMethod(method));
                    sources.add(source);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return sources;
    }
}

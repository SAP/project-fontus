package com.sap.fontus.generator;

import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.Sink;
import com.sap.fontus.config.SinkParameter;
import com.sap.fontus.utils.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SinkGenerator extends AbstractGenerator {
    private final String className;
    private final List<String> categories;
    private final List<String> vendors;
    private final List<String> purposes;

    public SinkGenerator(String className, List<String> categories, List<String> vendors, List<String> purposes) {
        this.className = Utils.slashToDot(className);
        this.categories = categories;
        this.vendors = vendors;
        this.purposes = purposes;
    }

    public List<Sink> readSinks(boolean isObject) {
        List<Sink> sinks = new ArrayList<>();
        try {
            Class<?> cls = Class.forName(this.className);
            Method[] methods = cls.getMethods();
            for (Method method : methods) {
                List<SinkParameter> sinkParameters = extractParameters(isObject, method.getParameterTypes());
                if (!sinkParameters.isEmpty()) {
                    Sink sink = new Sink(generateName(method), FunctionCall.fromMethod(method), sinkParameters, this.categories, this.vendors, this.purposes);
                    sinks.add(sink);
                }
            }
            Constructor<?>[] constructors = cls.getConstructors();
            for (Constructor<?> constructor : constructors) {
                List<SinkParameter> sinkParameters = extractParameters(isObject, constructor.getParameterTypes());
                if (!sinkParameters.isEmpty()) {
                    Sink sink = new Sink(generateName(constructor), FunctionCall.fromConstructor(constructor), sinkParameters, this.categories, this.vendors, this.purposes);
                    sinks.add(sink);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return sinks;
    }

    private List<SinkParameter> extractParameters(boolean isObject, Class[] parameterTypes) {
        Class[] parameters = parameterTypes;
        List<SinkParameter> sinkParameters = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            Class parameter = parameters[i];
            if (String.class.isAssignableFrom(parameter) || (isObject && parameter.isAssignableFrom(Object.class))) {
                sinkParameters.add(new SinkParameter(i));
            }
        }
        return sinkParameters;
    }
}

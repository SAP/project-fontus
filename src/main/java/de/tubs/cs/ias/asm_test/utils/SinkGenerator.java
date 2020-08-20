package de.tubs.cs.ias.asm_test.utils;

import de.tubs.cs.ias.asm_test.asm.FunctionCall;
import de.tubs.cs.ias.asm_test.config.Sink;
import de.tubs.cs.ias.asm_test.config.SinkParameter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SinkGenerator {
    private String className;

    public SinkGenerator(String className) {
        this.className = Utils.fixup(className);
    }

    public List<Sink> readSinks(boolean isObject) {
        List<Sink> sinks = new ArrayList<>();
        try {
            Class cls = Class.forName(this.className);
            Method[] methods = cls.getMethods();
            for (Method method : methods) {
                Class[] parameters = method.getParameterTypes();
                List<SinkParameter> sinkParameters = new ArrayList<>();
                for (int i = 0; i < parameters.length; i++) {
                    Class parameter = parameters[i];
                    if (String.class.isAssignableFrom(parameter) || (isObject && parameter.isAssignableFrom(Object.class))) {
                        sinkParameters.add(new SinkParameter(i));
                    }
                }
                if (!sinkParameters.isEmpty()) {
                    Sink sink = new Sink(method.getName() + method.hashCode(), FunctionCall.fromMethod(method), sinkParameters);
                    sinks.add(sink);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return sinks;
    }
}

package com.sap.fontus.asm;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sap.fontus.utils.Utils;
import org.objectweb.asm.Type;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class Descriptor {
    private static final Pattern PRIMITIVE_DATA_TYPES = Pattern.compile("[ZBCSIFDJ]");
    private final List<String> parameters;
    private final String returnType;
    private final String descriptor;

    private Descriptor(List<String> parameters, String returnType) {
        this.parameters = parameters;
        this.returnType = returnType;
        this.descriptor = this.toDescriptor();
    }

    public Descriptor(String[] parameters, String returnType) {
        this.parameters = Arrays.asList(parameters);
        this.returnType = returnType;
        this.descriptor = this.toDescriptor();
    }

    public Descriptor(Type returnType, Type... arguments) {
        // Stopped using streams because it took 7% of total performance
        // this.parameters = Arrays.stream(arguments).map(Type::getDescriptor).collect(Collectors.toList());
        this.parameters = convertTypeArrayToStringList(arguments);
        this.returnType = returnType.getDescriptor();
        this.descriptor = this.toDescriptor();
    }

    private static List<String> convertTypeArrayToStringList(Type[] arguments) {
        if (arguments == null) {
            return new ArrayList<>(0);
        }
        List<String> argumentStrings = new ArrayList<>(arguments.length);
        for (Type t : arguments) {
            argumentStrings.add(t.getDescriptor());
        }
        return argumentStrings;
    }

    public static String replaceSuffix(String s, String from, String to) {
        if (s.endsWith(from)) {
            int lIdx = s.lastIndexOf(from);
            assert lIdx != -1;
            return s.substring(0, lIdx) + to;
        }
        return s;
    }

    public Descriptor replaceTypeSlow(String from, String to) {
        // Stopped using streams because it took 7% of total performance
        // List<String> replaced = this.parameters.stream().map(str -> replaceSuffix(str, from, to)).collect(Collectors.toList());
        // This method still takes ~3% of performance
        List<String> replaced = new ArrayList<>(this.parameters.size());
        for (String param : this.parameters) {
            replaced.add(replaceSuffix(param, from, to));
        }
        String ret = replaceSuffix(this.returnType, from, to);
        return new Descriptor(replaced, ret);
    }

    public Descriptor replaceType(String from, String to) {
        if (!this.descriptor.contains(from)) {
            return this;
        }
        return parseDescriptor(this.descriptor.replace(from, to));
    }

    public int parameterCount() {
        return this.parameters.size();
    }

    public int getParameterTotalSize() {
        // Sum up the total size of the parameters
        return this.getParameters().stream().mapToInt((param) -> Type.getType(param).getSize()).sum();
    }

    public Stack<String> getParameterStack() {
        Stack<String> pStack = new Stack<>();
        pStack.addAll(this.parameters);
        return pStack;
    }

    public List<String> getParameters() {
        return Collections.unmodifiableList(this.parameters);
    }

    public String getReturnType() {
        return this.returnType;
    }

    public String toDescriptor() {
        String params = String.join("", this.parameters);
        // Terrible performance of String.format therefore manual building of string
        // return String.format("(%s)%s", params, this.returnType);
        return "(" + params + ")" + this.returnType;
    }

    public Type toAsmMethodType() {
        Type[] parameterTypes = this.parameters.stream().map(Type::getType).toArray(Type[]::new);
        return Type.getMethodType(Type.getType(this.returnType), parameterTypes);
    }

    @Override
    public String toString() {
        String params = String.join(",", this.parameters);
        return String.format("Descriptor{parameters=[%s], returnType='%s'}", params, this.returnType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Descriptor that = (Descriptor) obj;
        return this.parameters.equals(that.parameters) &&
                this.returnType.equals(that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.parameters, this.returnType);
    }

    // Cache class name conversions as this is called a lot at runtime
    private static final Cache<String, String> classNameCache = Caffeine.newBuilder().build();

    /**
     * @param className Class name in the format "java.lang.Object"
     * @return Descriptor name in the format "Ljava/lang/Object;"
     */
    public static String classNameToDescriptorName(final String className) {
        return classNameCache.get(className, (s) -> {
            if ("int".equals(s)) {
                return "I";
            } else if ("byte".equals(s)) {
                return "B";
            } else if ("char".equals(s)) {
                return "C";
            } else if ("double".equals(s)) {
                return "D";
            } else if ("float".equals(s)) {
                return "F";
            } else if ("long".equals(s)) {
                return "J";
            } else if ("short".equals(s)) {
                return "S";
            } else if ("boolean".equals(s)) {
                return "Z";
            } else if ("void".equals(s)) {
                return "V";
            } else if (s.startsWith("[") && !s.endsWith(";")) {
                // Primitive array
                return s;
            } else {
                String converted = s;
                if (!converted.startsWith("[")) {
                    converted = "L" + converted + ";";
                }
                return Utils.dotToSlash(converted);
            }
        });
    }

    /**
     * Parses a textual Descriptor and disassembles it into its types
     */
    public static Descriptor parseDescriptor(String descriptor) {
        Type typeDescriptor = Type.getMethodType(descriptor);
        return new Descriptor(typeDescriptor.getReturnType(), typeDescriptor.getArgumentTypes());
    }

    public static Descriptor parseMethod(Method m) {
        List<String> parameters = new ArrayList<>(m.getParameterCount());
        for (Class<?> param : m.getParameterTypes()) {
            parameters.add(classNameToDescriptorName(param.getName()));
        }
        String returnType = classNameToDescriptorName(m.getReturnType().getName());
        return new Descriptor(parameters, returnType);
    }

    public static Descriptor parseExecutable(Executable m) {
        if (m instanceof Method) {
            return parseMethod((Method) m);
        } else {
            List<String> parameters = new ArrayList<>(m.getParameterCount());
            for (Class<?> param : m.getParameterTypes()) {
                parameters.add(classNameToDescriptorName(param.getName()));
            }
            return new Descriptor(parameters, "V");
        }
    }

}

package com.sap.fontus.asm;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sap.fontus.utils.Utils;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class Descriptor {
    private static final Pattern PRIMITIVE_DATA_TYPES = Pattern.compile("[ZBCSIFDJ]");
    private final String[] parameters;
    private final String returnType;
    private final String descriptor;

    public Descriptor(String[] parameters, String returnType) {
        this.parameters = parameters;
        this.returnType = returnType;
        this.descriptor = this.toDescriptor();
    }

    public Descriptor(Type returnType, Type... arguments) {
        // Stopped using streams because it took 7% of total performance
        // this.parameters = Arrays.stream(arguments).map(Type::getDescriptor).collect(Collectors.toList());
        this.parameters = convertTypeArrayToStringArray(arguments);
        this.returnType = returnType.getDescriptor();
        this.descriptor = this.toDescriptor();
    }

    private static String[] convertTypeArrayToStringArray(Type[] arguments) {
        if (arguments == null) {
            return new String[0];
        }
        String[] argumentStrings = new String[arguments.length];
        int i = 0;
        for (Type t : arguments) {
            argumentStrings[i++] = t.getDescriptor();
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
        String[] replaced = new String[this.parameters.length];
        int i = 0;
        for (String param : this.parameters) {
            replaced[i++] = replaceSuffix(param, from, to);
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
        return this.parameters.length;
    }

    public int getParameterTotalSize() {
        // Sum up the total size of the parameters
        int size = 0;
        for(String param : this.parameters) {
            size += Type.getType(param).getSize();
        }
        return size;
    }

    public Deque<String> getParameterStack() {
        ArrayDeque<String> stack = new ArrayDeque<>();
        for(String p : this.parameters) {
            stack.push(p);
        }
        return stack;
    }
    public String[] getParameters() {
        return this.parameters;
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
        Type[] parameterTypes = new Type[this.parameters.length];
        int i = 0;
        for(String param : this.parameters) {
            parameterTypes[i] = Type.getType(param);
        }
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
        return Arrays.equals(this.parameters, that.parameters) &&
                this.returnType.equals(that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(this.parameters), this.returnType);
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
        Type method = Type.getType(m);
        return new Descriptor(method.getReturnType(), method.getArgumentTypes());
    }

    public static Descriptor parseConstructor(Constructor<?> c) {
        Type method = Type.getType(c);
        return new Descriptor(method.getReturnType(), method.getArgumentTypes());
    }


    public static Descriptor parseExecutable(Executable exe) {
        if (exe instanceof Method m) {
            return parseMethod(m);
        } else if (exe instanceof Constructor<?> c) {
            return parseConstructor(c);

        } else {
            // TODO: Dead code?
            String[] parameters = new String[exe.getParameterCount()];
            int i = 0;
            for (Class<?> param : exe.getParameterTypes()) {
                parameters[i++] = classNameToDescriptorName(param.getName());
            }
            return new Descriptor(parameters, "V");
        }
    }

}

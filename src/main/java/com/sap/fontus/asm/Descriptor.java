package com.sap.fontus.asm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sap.fontus.utils.Utils;
import org.objectweb.asm.Type;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Descriptor {
    private static final Pattern PRIMITIVE_DATA_TYPES = Pattern.compile("[ZBCSIFDJ]");
    private final List<String> parameters;
    private final String returnType;

    private Descriptor(List<String> parameters, String returnType) {
        this.parameters = parameters;
        this.returnType = returnType;
    }

    public Descriptor(String[] parameters, String returnType) {
        this.parameters = Arrays.asList(parameters);
        this.returnType = returnType;
    }

    public Descriptor(Type returnType, Type... arguments) {
        // Stopped using streams because it took 7% of total performance
        // this.parameters = Arrays.stream(arguments).map(Type::getDescriptor).collect(Collectors.toList());
        this.parameters = convertTypeArrayToStringList(arguments);
        this.returnType = returnType.getDescriptor();
    }

    private List<String> convertTypeArrayToStringList(Type[] arguments) {
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

    public Descriptor replaceType(String from, String to) {
        // Stopped using streams because it took 7% of total performance
        // List<String> replaced = this.parameters.stream().map(str -> replaceSuffix(str, from, to)).collect(Collectors.toList());
        List<String> replaced = new ArrayList<>(this.parameters.size());
        for (String param : this.parameters) {
            replaced.add(replaceSuffix(param, from, to));
        }
        String ret = replaceSuffix(this.returnType, from, to);
        return new Descriptor(replaced, ret);
    }

    public int parameterCount() {
        return this.parameters.size();
    }

    public int getParameterTotalSize() {
        // Sum up the total size of the parameters
        return getParameters().stream().mapToInt((param) -> Type.getType(param).getSize()).sum();
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
        return String.format("(%s)%s", params, this.returnType);
    }

    public Type toAsmMethodType() {
        Type[] parameterTypes = this.parameters.stream().map(Type::getType).toArray(Type[]::new);
        return Type.getMethodType(Type.getType(returnType), parameterTypes);
    }

    @Override
    public String toString() {
        String params = String.join(",", this.parameters);
        return String.format("Descriptor{parameters=[%s], returnType='%s'}", params, this.returnType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        Descriptor that = (Descriptor) obj;
        return this.parameters.equals(that.parameters) &&
                this.returnType.equals(that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.parameters, this.returnType);
    }

//    /**
//     * Checks whether the parameter list contains String like Parameters that need conversion before calling.
//     *
//     * @return Whether on of the parameters is a String like type
//     */
//    boolean hasStringLikeParameters() {
//        for (String p : this.getParameters()) {
//            if (InstrumentationHelper.canHandleType(p)) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * @param className Class name in the format "java.lang.Object"
     * @return Descriptor name in the format "Ljava/lang/Object;"
     */
    public static String classNameToDescriptorName(final String className) {
        if ("int".equals(className)) {
            return "I";
        } else if ("byte".equals(className)) {
            return "B";
        } else if ("char".equals(className)) {
            return "C";
        } else if ("double".equals(className)) {
            return "D";
        } else if ("float".equals(className)) {
            return "F";
        } else if ("long".equals(className)) {
            return "J";
        } else if ("short".equals(className)) {
            return "S";
        } else if ("boolean".equals(className)) {
            return "Z";
        } else if ("void".equals(className)) {
            return "V";
        } else if (className.startsWith("[") && !className.endsWith(";")) {
            // Primitive array
            return className;
        } else {
            String converted = className;
            if (!converted.startsWith("[")) {
                converted = "L" + converted + ";";
            }
            return Utils.dotToSlash(converted);
        }
    }

    /**
     * Parses a textual Descriptor and disassembles it into its types
     * TODO: maybe remove the ';'s?
     * TODO: throw exception on invalid descriptor
     * TODO: think of a nicer structure, this is really messy
     */
    public static Descriptor parseDescriptor(String descriptor) {
        Type typeDescriptor = Type.getMethodType(descriptor);
        return new Descriptor(typeDescriptor.getReturnType(), typeDescriptor.getArgumentTypes());
//        ArrayList<String> out;
//        StringBuilder returnType;
//        try (Scanner sc = new Scanner(descriptor)) {
//            sc.useDelimiter("");
//            String opening = sc.next();
//            assert "(".equals(opening);
//            out = new ArrayList<>();
//            String next = sc.next();
//            StringBuilder buffer = new StringBuilder();
//            boolean inType = false;
//            while (!")".equals(next)) {
//                buffer.append(next);
//                Matcher primitivesMatcher = PRIMITIVE_DATA_TYPES.matcher(next);
//                if (!inType && primitivesMatcher.matches()) {
//                    out.add(buffer.toString());
//                    buffer = new StringBuilder();
//                } else if (!"[".equals(next)) {
//                    inType = true;
//                    if (";".equals(next)) {
//                        out.add(buffer.toString());
//                        buffer = new StringBuilder();
//                        inType = false;
//                    }
//                }
//
//                next = sc.next();
//            }
//
//            returnType = new StringBuilder();
//            while (sc.hasNext()) {
//                returnType.append(sc.next());
//            }
//        }
//
//        return new Descriptor(out, returnType.toString());
    }

    public static Descriptor parseMethod(Method m) {
        List<String> parameters = new ArrayList<>(m.getParameterCount());
        for (Class param : m.getParameterTypes()) {
            parameters.add(Descriptor.classNameToDescriptorName(param.getName()));
        }
        String returnType = classNameToDescriptorName(m.getReturnType().getName());
        return new Descriptor(parameters, returnType);
    }

    public static Descriptor parseExecutable(Executable m) {
        if (m instanceof Method) {
            return parseMethod((Method) m);
        } else {
            List<String> parameters = new ArrayList<>(m.getParameterCount());
            for (Class param : m.getParameterTypes()) {
                parameters.add(Descriptor.classNameToDescriptorName(param.getName()));
            }
            return new Descriptor(parameters, "V");
        }
    }

}

package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
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

    Descriptor(String[] parameters, String returnType) {
        this.parameters = Arrays.asList(parameters);
        this.returnType = returnType;
    }

    // This is kinda ugly, as a single String parameter would be applicable to the var args overload below..
    // The java spec tries to resolve overloaded methods without taking var args into account first however,
    // so a single String argument will result in this constructor being invoked.
    // See: https://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.12.2
    Descriptor(String returnType) {
        this.parameters = Collections.emptyList();
        this.returnType = returnType;
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    Descriptor(String... args) {
        List<String> arguments = Arrays.asList(args);
        int lastIndex = arguments.size() - 1;
        this.returnType = arguments.get(lastIndex);
        this.parameters = arguments.subList(0, lastIndex);
    }

    private static String replaceSuffix(String s, String from, String to) {
        if (s.endsWith(from)) {
            int lIdx = s.lastIndexOf(from);
            assert lIdx != -1;
            return s.substring(0, lIdx) + to;
        }
        return s;
    }

    public Descriptor replaceType(String from, String to) {
        List<String> replaced = this.parameters.stream().map(str -> replaceSuffix(str, from, to)).collect(Collectors.toList());
        String ret = replaceSuffix(this.returnType, from, to);
        return new Descriptor(replaced, ret);
    }

    public int parameterCount() {
        return this.parameters.size();
    }

    public Stack<String> getParameterStack() {
        Stack<String> pStack = new Stack<>();
        pStack.addAll(this.parameters);
        return pStack;
    }

    List<String> getParameters() {
        return Collections.unmodifiableList(this.parameters);
    }

    public String getReturnType() {
        return this.returnType;
    }

    public String toDescriptor() {
        String params = String.join("", this.parameters);
        return String.format("(%s)%s", params, this.returnType);
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
        } else {
            String trimmedClassName = className;
            int arrayDimensions = 0;
            for (; trimmedClassName.endsWith("[]"); arrayDimensions++) {
                trimmedClassName = trimmedClassName.substring(0, trimmedClassName.indexOf('['));
            }

            StringBuilder descriptor = new StringBuilder();

            if (isPrimitiveQN(trimmedClassName)) {
                descriptor.append(classNameToDescriptorName(trimmedClassName));
            } else {
                descriptor.append(String.format("L%s;", Utils.fixupReverse(trimmedClassName)));
            }

            for (int i = 0; i < arrayDimensions; i++) {
                descriptor.insert(0, "[");
            }

            return descriptor.toString();
        }
    }

    private static boolean isPrimitiveQN(String qn) {
        switch (qn) {
            case "int":
            case "byte":
            case "char":
            case "double":
            case "float":
            case "long":
            case "short":
            case "boolean":
            case "void":
                return true;
            default:
                return false;
        }
    }

    private static boolean isPrimitiveDescriptorName(String descriptorName) {
        switch (descriptorName) {
            case "I":
            case "B":
            case "C":
            case "D":
            case "F":
            case "S":
            case "J":
            case "Z":
            case "V":
                return true;
            default:
                return false;
        }
    }

    /**
     * @param descriptorName Descriptor name in the format "Ljava/lang/Object;"
     * @return Class name in the format "java/lang/Object"
     */
    public static String descriptorNameToQN(String descriptorName) {
        switch (descriptorName) {
            case "I":
                return "int";
            case "B":
                return "byte";
            case "C":
                return "char";
            case "D":
                return "double";
            case "F":
                return "float";
            case "S":
                return "short";
            case "J":
                return "long";
            case "Z":
                return "boolean";
            case "V":
                throw new IllegalArgumentException("Descriptor name V cannot be cast to a qualified name");
            default:
                int arrayCount = 0;
                for (; descriptorName.startsWith("["); arrayCount++) {
                    descriptorName = descriptorName.substring(1);
                }

                if (isPrimitiveDescriptorName(descriptorName)) {
                    descriptorName = descriptorNameToQN(descriptorName);
                } else {
                    // Remove "L" [...] ";"
                    descriptorName = descriptorName.substring(1, descriptorName.length() - 1);
                }

                StringBuilder descriptorNameBuilder = new StringBuilder(descriptorName);
                for (int i = 0; i < arrayCount; i++) {
                    descriptorNameBuilder.append("[]");
                }
                descriptorName = descriptorNameBuilder.toString();
                return descriptorName;
        }
    }

    /**
     * Parses a textual Descriptor and disassembles it into its types
     * TODO: maybe remove the ';'s?
     * TODO: throw exception on invalid descriptor
     * TODO: think of a nicer structure, this is really messy
     */
    public static Descriptor parseDescriptor(String descriptor) {
        ArrayList<String> out;
        StringBuilder returnType;
        try (Scanner sc = new Scanner(descriptor)) {
            sc.useDelimiter("");
            String opening = sc.next();
            assert "(".equals(opening);
            out = new ArrayList<>();
            String next = sc.next();
            StringBuilder buffer = new StringBuilder();
            boolean inType = false;
            while (!")".equals(next)) {
                buffer.append(next);
                Matcher primitivesMatcher = PRIMITIVE_DATA_TYPES.matcher(next);
                if (!inType && primitivesMatcher.matches()) {
                    out.add(buffer.toString());
                    buffer = new StringBuilder();
                } else if (!"[".equals(next)) {
                    inType = true;
                    if (";".equals(next)) {
                        out.add(buffer.toString());
                        buffer = new StringBuilder();
                        inType = false;
                    }
                }

                next = sc.next();
            }

            returnType = new StringBuilder();
            while (sc.hasNext()) {
                returnType.append(sc.next());
            }
        }

        return new Descriptor(out, returnType.toString());
    }

    public static String getSignature(Method m) {
        // Hacky but better than generating it by hand
        try {
            Method getGenericSignature = Method.class.getDeclaredMethod("getGenericSignature");
            getGenericSignature.setAccessible(true);
            return (String) getGenericSignature.invoke(m);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("Cannot generate signature, because Method.getGenericSignature is not available");
        }
    }

    public static Descriptor parseMethod(Method m) {
        return Descriptor
                .parseDescriptor(
                        org.objectweb.asm.commons.Method
                                .getMethod(m)
                                .getDescriptor()
                );
    }

}

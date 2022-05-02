package com.sap.fontus.utils;

import com.sap.fontus.Constants;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

public class MethodUtils {

    public static boolean isToString(String name, String methodDescriptor) {
        return "()Ljava/lang/String;".equals(methodDescriptor) && Constants.ToString.equals(name);
    }

    public static int tagToOpcode(int h_opcode) {
        switch (h_opcode) {
            case Opcodes.H_GETFIELD:
                return Opcodes.GETFIELD;
            case Opcodes.H_GETSTATIC:
                return Opcodes.GETSTATIC;
            case Opcodes.H_INVOKEINTERFACE:
                return Opcodes.INVOKEINTERFACE;
            case Opcodes.H_INVOKESPECIAL:
            case Opcodes.H_NEWINVOKESPECIAL:
                return Opcodes.INVOKESPECIAL;
            case Opcodes.H_INVOKESTATIC:
                return Opcodes.INVOKESTATIC;
            case Opcodes.H_INVOKEVIRTUAL:
                return Opcodes.INVOKEVIRTUAL;
            case Opcodes.H_PUTFIELD:
                return Opcodes.PUTFIELD;
            case Opcodes.H_PUTSTATIC:
                return Opcodes.PUTSTATIC;
            default:
                return h_opcode;
        }
    }

    public static String[] getExceptionTypes(Method method) {
        AnnotatedType[] annotatedExceptionTypes = method.getAnnotatedExceptionTypes();
        String[] exceptions = new String[annotatedExceptionTypes.length];
        int i = 0;
        for (AnnotatedType ex : annotatedExceptionTypes) {
            exceptions[i] = Utils.dotToSlash(ex.getType().getTypeName());
            i++;
        }
        return exceptions;
    }

    public static Optional<String> getSignature(Method m) {
        // Hacky but better than generating it by hand
        try {
            Method getGenericSignature = Method.class.getDeclaredMethod("getGenericSignature");
            getGenericSignature.setAccessible(true);
            return Optional.ofNullable((String) getGenericSignature.invoke(m));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("Cannot generate signature, because Method.getGenericSignature is not available");
        }
//        ClassTraverser classTraverser = new ClassTraverser(new CombinedExcludedLookup());
//        classTraverser.readMethods(Type.getType(m.getDeclaringClass()), new ClassResolver(m.getDeclaringClass().getClassLoader()));
//        List<com.sap.fontus.instrumentation.Method> methodList = classTraverser.getMethods();
//        return methodList.stream()
//                .filter((method) -> method.getName().equals(m.getName()) && Descriptor.parseMethod(m).equals(method.getParsedDescriptor()))
//                .map(com.sap.fontus.instrumentation.Method::getSignature)
//                .filter(Objects::nonNull)
//                .findFirst();
    }

    public static boolean hasGenericInformation(Method m) {
        return getSignature(m).isPresent();
//        return !Arrays.equals(m.getGenericParameterTypes(), m.getParameterTypes()) || !Objects.equals(m.getGenericReturnType(), m.getReturnType()) || !Arrays.equals(m.getGenericExceptionTypes(), m.getExceptionTypes());
//        try {
//            Method hasGenericInformation = Method.class.getDeclaredMethod("hasGenericInformation");
//            hasGenericInformation.setAccessible(true);
//            return (boolean) hasGenericInformation.invoke(m);
//        } catch (Exception ex) {
//            throw new UnsupportedOperationException("Cannot evaluate if method is generic signature, because Method.hasGenericInformation is not available");
//        }
    }

    public static boolean isPublicOrProtected(com.sap.fontus.instrumentation.Method m) {
        return Modifier.isPublic(m.getAccess()) || Modifier.isProtected(m.getAccess());
    }

    public static boolean isPublicOrProtectedNotStatic(com.sap.fontus.instrumentation.Method m) {
        return (Modifier.isPublic(m.getAccess()) || Modifier.isProtected(m.getAccess())) && !Modifier.isStatic(m.getAccess());
    }
}

package de.tubs.cs.ias.asm_test.utils;

import de.tubs.cs.ias.asm_test.asm.ClassResolver;
import de.tubs.cs.ias.asm_test.asm.TypeHierarchyReaderWithLoaderSupport;
import org.mutabilitydetector.asm.typehierarchy.TypeHierarchy;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class ClassUtils {

    /**
     * Returns for the given class all instance methods which are public or protected
     * This includes the inherited ones (unlike getDeclaredMethods), but excludes the Object-class methods
     *
     * @param classToDiscover Class to discover
     * @param methods         List, where the discovered methods will be added (duplicates will not be stored, can be prefilled)
     */
    public static void getAllMethods(String classToDiscover, ClassResolver resolver, List<Method> methods) {
        TypeHierarchyReaderWithLoaderSupport typeHierarchyReader = new TypeHierarchyReaderWithLoaderSupport(resolver);
        for (Type cls = Type.getObjectType(classToDiscover); cls != null; cls = typeHierarchyReader.getSuperClass(cls)) {
            if (JdkClassesLookupTable.getInstance().isJdkClass(cls.getInternalName())) {
                try {
                    Class clazz = Class.forName(cls.getClassName());
                    Method[] declaredMethods = clazz.getDeclaredMethods();
                    for (Method declaredMethod : declaredMethods) {
                        addMethodIfNotContained(declaredMethod, methods);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This methods add all methods if the passed interface list to the method list, if the method isn't already contained or the interface is already implemented by the super type
     * For determination if contained see {@link ClassUtils#addMethodIfNotContained(Method, List)}
     *
     * @param superName                 Super class of the interface implementing one
     * @param directInheritedInterfaces Array with interface names as QN
     * @param methods                   List to add methods (may already contain methods)
     */
    public static void addNotContainedJdkInterfaceMethods(String superName, String[] directInheritedInterfaces, List<Method> methods, ClassResolver resolver) {
        if (directInheritedInterfaces == null || directInheritedInterfaces.length == 0) {
            return;
        }
        TypeHierarchyReaderWithLoaderSupport typeHierarchyReader = new TypeHierarchyReaderWithLoaderSupport(resolver);

        Set<String> jdkOnly = new HashSet<>();
        discoverAllJdkInterfaces(Arrays.asList(directInheritedInterfaces), jdkOnly, typeHierarchyReader);

        Set<Type> superInterfaces = new HashSet<>();
        for (Type cls = Type.getObjectType(superName); cls != null; cls = typeHierarchyReader.getSuperClass(cls)) {
            TypeHierarchy hierarchy = typeHierarchyReader.hierarchyOf(cls);
            superInterfaces.addAll(hierarchy.getInterfaces());
        }

        Set<String> jdkSuperInterfaces = new HashSet<>();
        discoverAllJdkInterfaces(superInterfaces.stream().map(Type::getInternalName).collect(Collectors.toList()), jdkSuperInterfaces, typeHierarchyReader);

        List<String> interfaces = new ArrayList<>();
        for (String directImplI : jdkOnly) {
            boolean isContainedInSuper = false;
            for (String superI : jdkSuperInterfaces) {
                if (superI.equals(directImplI)) {
                    isContainedInSuper = true;
                    break;
                }
            }
            if (!isContainedInSuper) {
                interfaces.add(directImplI);
            }
        }

        for (String interfaceName : interfaces) {
            if (JdkClassesLookupTable.getInstance().isJdkClass(interfaceName) || isAnnotation(interfaceName)) {
                try {
                    Class cls = Class.forName(Utils.fixup(interfaceName));
                    for (Method m : cls.getMethods()) {
                        addMethodIfNotContained(m, methods);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void discoverAllJdkInterfaces(List<String> interfacesToLookThrough, Set<String> result, TypeHierarchyReaderWithLoaderSupport typeHierarchyReader) {
        for (String interfaceName : interfacesToLookThrough) {
            if (JdkClassesLookupTable.getInstance().isJdkClass(interfaceName)) {
                result.add(interfaceName);
            } else if (isAnnotation(interfaceName)) {
                result.add(interfaceName);
                List<String> superInterfaces = typeHierarchyReader.hierarchyOf(Type.getObjectType(interfaceName)).getInterfaces().stream().map(Type::getInternalName).collect(Collectors.toList());
                discoverAllJdkInterfaces(superInterfaces, result, typeHierarchyReader);
            } else {
                List<String> superInterfaces = typeHierarchyReader.hierarchyOf(Type.getObjectType(interfaceName)).getInterfaces().stream().map(Type::getInternalName).collect(Collectors.toList());
                discoverAllJdkInterfaces(superInterfaces, result, typeHierarchyReader);
            }
        }
    }

    /**
     * Adds the passed method to the list, if it's not already contained.
     * The passed method must be an overridable method (public or protected and not never static)
     * <p>
     * If a method is already contained is determined by the method name and descriptor (declaring class is NOT considered)
     */
    private static void addMethodIfNotContained(Method methodToAdd, List<Method> methods) {
        if (isPublicOrProtectedNotStatic(methodToAdd)) {
            boolean alreadyContained = methods.stream().anyMatch(methodInMethods -> {
                boolean nameEquals = methodToAdd.getName().equals(methodInMethods.getName());
                boolean correctVisibility = isPublicOrProtectedNotStatic(methodInMethods);
                boolean signaturEquals = Arrays.equals(methodToAdd.getParameterTypes(), methodInMethods.getParameterTypes());
                return nameEquals && correctVisibility && signaturEquals;
            });

            if (!alreadyContained) {
                methods.add(methodToAdd);
            }
        }
    }

    public static boolean isPublicOrProtectedNotStatic(Method m) {
        return (Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers())) && !Modifier.isStatic(m.getModifiers());
    }

    public static boolean hasGenericInformation(Method m) {
        try {
            Method hasGenericInformation = Method.class.getDeclaredMethod("hasGenericInformation");
            hasGenericInformation.setAccessible(true);
            return (boolean) hasGenericInformation.invoke(m);
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Cannot evaluate if method is generic signature, because Method.hasGenericInformation is not available");
        }
    }

    public static boolean isAnnotation(String internalName) {
        int access = 0;
        try {
            access = new ClassReader(internalName).getAccess();
        } catch (IOException e) {
            System.err.println("Could not resolve class " + internalName + " for isAnnotation checking");
        }
        return (access & Opcodes.ACC_ANNOTATION) > 0;
    }
}

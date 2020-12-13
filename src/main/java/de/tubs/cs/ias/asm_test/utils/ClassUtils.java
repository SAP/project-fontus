package de.tubs.cs.ias.asm_test.utils;

import de.tubs.cs.ias.asm_test.asm.*;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.instrumentation.InstrumentationState;
import org.mutabilitydetector.asm.typehierarchy.TypeHierarchy;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class ClassUtils {
    private static final Method findLoadedClass;

    static {
        Method findLoadedClass1 = null;
        try {
            findLoadedClass1 = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        findLoadedClass = findLoadedClass1;
        findLoadedClass.setAccessible(true);
    }

    /**
     * Returns for the given class all JDK-inherited instance methods which are public or protected
     * This includes the inherited ones (unlike getDeclaredMethods), but excludes the Object-class methods
     *
     * @param classToDiscover Class to discover
     * @param methods         List, where the discovered methods will be added (duplicates will not be stored, can be prefilled)
     */
    public static void getAllJdkMethods(String classToDiscover, ClassResolver resolver, List<de.tubs.cs.ias.asm_test.instrumentation.Method> methods) {
        TypeHierarchyReaderWithLoaderSupport typeHierarchyReader = new TypeHierarchyReaderWithLoaderSupport(resolver);
        for (Type cls = Type.getObjectType(classToDiscover); cls != null; cls = typeHierarchyReader.getSuperClass(cls)) {
            if (JdkClassesLookupTable.getInstance().isJdkClass(cls.getInternalName())) {
                try {
                    Class<?> clazz = Class.forName(cls.getClassName());
                    Method[] declaredMethods = clazz.getDeclaredMethods();
                    for (Method declaredMethod : declaredMethods) {
                        addMethodIfNotContained(de.tubs.cs.ias.asm_test.instrumentation.Method.from(declaredMethod), methods);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This methods add all methods if the passed interface list to the method list, if the method isn't already contained or the interface is already implemented by the super type
     * For determination if contained see {@link ClassUtils#addMethodIfNotContained(de.tubs.cs.ias.asm_test.instrumentation.Method, List)}
     *
     * @param superName                 Super class of the interface implementing one
     * @param directInheritedInterfaces Array with interface names as QN
     * @param methods                   List to add methods (may already contain methods)
     */
    public static void addNotContainedJdkInterfaceMethods(String superName, String[] directInheritedInterfaces, List<de.tubs.cs.ias.asm_test.instrumentation.Method> methods, ClassResolver resolver, ClassLoader loader) {
        if (directInheritedInterfaces == null || directInheritedInterfaces.length == 0) {
            return;
        }
        TypeHierarchyReaderWithLoaderSupport typeHierarchyReader = new TypeHierarchyReaderWithLoaderSupport(resolver);

        Set<String> jdkOnly = new HashSet<>();
        discoverAllJdkInterfaces(Arrays.asList(directInheritedInterfaces), jdkOnly, typeHierarchyReader, resolver);

        Set<Type> superInterfaces = new HashSet<>();
        for (Type cls = Type.getObjectType(superName); cls != null; cls = typeHierarchyReader.getSuperClass(cls)) {
            TypeHierarchy hierarchy = typeHierarchyReader.hierarchyOf(cls);
            superInterfaces.addAll(hierarchy.getInterfaces());
        }

        Set<String> jdkSuperInterfaces = new HashSet<>();
        discoverAllJdkInterfaces(superInterfaces.stream().map(Type::getInternalName).collect(Collectors.toList()), jdkSuperInterfaces, typeHierarchyReader, resolver);

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
            if (JdkClassesLookupTable.getInstance().isJdkClass(interfaceName) || isAnnotation(interfaceName, resolver)) {
                Class<?> cls = null;
                List<de.tubs.cs.ias.asm_test.instrumentation.Method> intfMethods = new ArrayList<>();
                try {
                    cls = Class.forName(Utils.slashToDot(interfaceName));
                } catch (ClassNotFoundException e) {
                    try {
                        cls = (Class<?>) findLoadedClass.invoke(Thread.currentThread().getContextClassLoader(), Utils.slashToDot(interfaceName));
                    } catch (IllegalAccessException | InvocationTargetException illegalAccessException) {
                        illegalAccessException.printStackTrace();
                    }
                }

                if (cls != null) {
                    for (Method m : cls.getMethods()) {
                        de.tubs.cs.ias.asm_test.instrumentation.Method method = de.tubs.cs.ias.asm_test.instrumentation.Method.from(m);
                        intfMethods.add(method);
                    }
                } else {
                    try {
                        ClassVisitor cv = new NopVisitor(Opcodes.ASM7) {
                            @Override
                            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                                de.tubs.cs.ias.asm_test.instrumentation.Method method = new de.tubs.cs.ias.asm_test.instrumentation.Method(access, interfaceName, name, descriptor, signature, exceptions, true);
                                intfMethods.add(method);
                                return super.visitMethod(access, name, descriptor, signature, exceptions);
                            }
                        };
                        ClassReader cr;
                        cr = new ClassReaderWithLoaderSupport(resolver, interfaceName);
                        cr.accept(cv, ClassReader.SKIP_FRAMES);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                for (de.tubs.cs.ias.asm_test.instrumentation.Method method : intfMethods) {
                    if (!isImplementedBySuperClass(superName, method, loader)) {
                        addMethodIfNotContained(method, methods);
                    }
                }
            }
        }
    }

    public static InputStream getClassInputStream(String internalName, ClassLoader loader) {
        String resourceName = internalName + ".class";
        InputStream resource = ClassLoader.getSystemResourceAsStream(resourceName);
        if (resource == null) {
            resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
            if (resource == null) {
                if (loader != null) {
                    resource = loader.getResourceAsStream(resourceName);
                }
            }
        }
        if (resource != null) {
            return resource;
        }
        throw new RuntimeException("Resource for " + internalName + "couldn't be found");
    }

    private static boolean isImplementedBySuperClass(String superName, de.tubs.cs.ias.asm_test.instrumentation.Method m, ClassLoader loader) {
        try {
            if (JdkClassesLookupTable.getInstance().isJdkClass(superName)) {
                return false;
            }
            // TODO What if super class is not loadable
            ClassReader classReader = new ClassReader(getClassInputStream(superName, loader));
            MethodChecker methodChecker = new MethodChecker(m);
            classReader.accept(methodChecker, 0);
            if (!methodChecker.superImplements) {
                return isImplementedBySuperClass(classReader.getSuperName(), m, loader);
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void discoverAllJdkInterfaces(List<String> interfacesToLookThrough, Set<String> result, TypeHierarchyReaderWithLoaderSupport typeHierarchyReader, ClassResolver resolver) {
        for (String interfaceName : interfacesToLookThrough) {
            if (JdkClassesLookupTable.getInstance().isJdkClass(interfaceName)) {
                result.add(interfaceName);
            } else if (isAnnotation(interfaceName, resolver)) {
                result.add(interfaceName);
                List<String> superInterfaces = typeHierarchyReader.hierarchyOf(Type.getObjectType(interfaceName)).getInterfaces().stream().map(Type::getInternalName).collect(Collectors.toList());
                discoverAllJdkInterfaces(superInterfaces, result, typeHierarchyReader, resolver);
            } else {
                List<String> superInterfaces = typeHierarchyReader.hierarchyOf(Type.getObjectType(interfaceName)).getInterfaces().stream().map(Type::getInternalName).collect(Collectors.toList());
                discoverAllJdkInterfaces(superInterfaces, result, typeHierarchyReader, resolver);
            }
        }
    }

    /**
     * Adds the passed method to the list, if it's not already contained.
     * The passed method must be an overridable method (public or protected and not never static)
     * <p>
     * If a method is already contained is determined by the method name and descriptor (declaring class is NOT considered)
     */
    private static void addMethodIfNotContained(de.tubs.cs.ias.asm_test.instrumentation.Method methodToAdd, List<de.tubs.cs.ias.asm_test.instrumentation.Method> methods) {
        if (isPublicOrProtectedNotStatic(methodToAdd)) {
            boolean alreadyContained = methods.stream().anyMatch(methodInMethods -> {
                boolean nameEquals = methodToAdd.getName().equals(methodInMethods.getName());
                boolean correctVisibility = isPublicOrProtectedNotStatic(methodInMethods);
                boolean signatureEquals = Arrays.equals(methodToAdd.getParameterTypes(), methodInMethods.getParameterTypes());
                return nameEquals && correctVisibility && signatureEquals;
            });

            if (!alreadyContained) {
                methods.add(methodToAdd);
            }
        }
    }

    public static boolean isPublicOrProtectedNotStatic(de.tubs.cs.ias.asm_test.instrumentation.Method m) {
        return (Modifier.isPublic(m.getAccess()) || Modifier.isProtected(m.getAccess())) && !Modifier.isStatic(m.getAccess());
    }

    public static boolean isAnnotation(String internalName, ClassResolver resolver) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class<?> cls = (Class<?>) findLoadedClass.invoke(classLoader, Utils.slashToDot(internalName));
            if (cls != null) {
                return cls.isAnnotation();
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        try {
            int access = new ClassReader(internalName).getAccess();
            return (access & Opcodes.ACC_ANNOTATION) > 0;
        } catch (IOException e) {
            if (Configuration.isLoggingEnabled()) {
                System.err.println("Could not resolve class " + internalName + " for isAnnotation checking");
            }
        }
        return InstrumentationState.getInstance().isAnnotation(internalName, resolver);
    }

    public static boolean isInterface(String internalName) {
        return ClassUtils.isInterface(internalName, null);
    }

    public static boolean isInterface(int access) {
        return ((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE);
    }

    public static boolean isInterface(byte[] bytes) {
        return ClassUtils.isInterface(new ClassReader(bytes).getAccess());
    }

    public static boolean isInterface(String internalName, ClassLoader loader) {
        try {
            return ClassUtils.isInterface(new ClassReader(getClassInputStream(internalName, loader)).getAccess());
        } catch (IOException e) {
            if (Configuration.isLoggingEnabled()) {
                System.err.println("Could not resolve class " + internalName + " for isInterface checking");
            }
        }
        return false;
    }

    public static class MethodChecker extends ClassVisitor {
        private final de.tubs.cs.ias.asm_test.instrumentation.Method method;
        private boolean superImplements = false;

        public MethodChecker(de.tubs.cs.ias.asm_test.instrumentation.Method method) {
            super(Opcodes.ASM7);
            this.method = method;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if (name.equals(this.method.getName()) && this.method.getDescriptor().equals(descriptor) && (access & Opcodes.ACC_ABSTRACT) == 0) {
                superImplements = true;
            }
            return null;
        }
    }
}

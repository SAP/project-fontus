package com.sap.fontus.utils;

import com.sap.fontus.asm.ClassReaderWithLoaderSupport;
import com.sap.fontus.asm.ClassResolver;
import com.sap.fontus.asm.NopVisitor;
import com.sap.fontus.asm.TypeHierarchyReaderWithLoaderSupport;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import com.sap.fontus.instrumentation.Method;
import org.mutabilitydetector.asm.typehierarchy.TypeHierarchy;
import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ClassTraverser {
    private static final Logger logger = LogUtils.getLogger();

    private final CombinedExcludedLookup combinedExcludedLookup;
    private final Set<Method> methodList = new HashSet<>();

    public ClassTraverser(CombinedExcludedLookup combinedExcludedLookup) {
        this.combinedExcludedLookup = combinedExcludedLookup;
    }

    public static List<Field> getAllFields(Class<?> origClass) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> cls = origClass; cls != null; cls = cls.getSuperclass()) {
            for (Field f : cls.getDeclaredFields()) {
                if (!fields.contains(f)) {
                    fields.add(f);
                }
            }
        }
        return fields;
    }

    public void readMethods(Type cls, ClassResolver resolver) {
        if (combinedExcludedLookup.isJdkClass(cls.getInternalName())) {
            Class<?> clazz = ClassUtils.findLoadedClass(cls.getInternalName());

            java.lang.reflect.Method[] methods = clazz.getMethods();
            for (java.lang.reflect.Method declaredMethod : methods) {
                addMethodIfNotContained(Method.from(declaredMethod));
            }
        } else {
            try {
                ClassVisitor cv = new NopVisitor(Opcodes.ASM9) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                        Method method = new Method(access, cls.getInternalName(), name, descriptor, signature, exceptions, false);
                        addMethodIfNotContained(method);
                        return super.visitMethod(access, name, descriptor, signature, exceptions);
                    }
                };
                ClassReader cr = new ClassReaderWithLoaderSupport(resolver, cls.getClassName());
                cr.accept(cv, ClassReader.SKIP_FRAMES);

                String superName = cr.getSuperName();
                if (superName != null) {
                    Type superType = Type.getObjectType(superName);
                    this.readMethods(superType, resolver);
                }
                String[] interfaces = cr.getInterfaces();
                if (interfaces != null) {
                    for (String intf : interfaces) {
                        Type interfaceType = Type.getObjectType(intf);
                        this.readMethods(interfaceType, resolver);
                    }
                }
            } catch (IOException e) {
                logger.error("Could not load class " + cls.getInternalName() + " for ClassTraverser.readMethods");
            }
        }
    }

    /**
     * Returns for the given class all JDK-inherited instance methods which are public or protected
     * This includes the inherited ones (unlike getDeclaredMethods), but excludes the Object-class methods
     *
     * @param classToDiscover Class to discover
     */
    public void readAllJdkMethods(String classToDiscover, ClassResolver resolver) {
        TypeHierarchyReaderWithLoaderSupport typeHierarchyReader = new TypeHierarchyReaderWithLoaderSupport(resolver);
        for (Type cls = Type.getObjectType(classToDiscover); cls != null; cls = typeHierarchyReader.getSuperClass(cls)) {
            if (this.combinedExcludedLookup.isPackageExcludedOrJdk(cls.getInternalName())) {
                Class<?> clazz = ClassUtils.findLoadedClass(cls.getInternalName());

                if (clazz != null) {
                    java.lang.reflect.Method[] declaredMethods = clazz.getDeclaredMethods();
                    for (java.lang.reflect.Method declaredMethod : declaredMethods) {
                        addMethodIfNotContained(Method.from(declaredMethod));
                    }
                } else {
                    try {
                        final String clsName = cls.getInternalName();
                        ClassVisitor cv = new NopVisitor(Opcodes.ASM9) {
                            @Override
                            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                                Method method = new Method(access, clsName, name, descriptor, signature, exceptions, false);
                                addMethodIfNotContained(method);
                                return super.visitMethod(access, name, descriptor, signature, exceptions);
                            }
                        };
                        ClassReader cr = new ClassReaderWithLoaderSupport(resolver, cls.getClassName());
                        cr.accept(cv, ClassReader.SKIP_FRAMES);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Adds the passed method to the list, if it's not already contained.
     * The passed method must be an overridable method (public or protected and not never static)
     * <p>
     * If a method is already contained is determined by the method name and descriptor (declaring class is NOT considered)
     */
    private void addMethodIfNotContained(Method methodToAdd) {
        if (MethodUtils.isPublicOrProtectedNotStatic(methodToAdd)) {
            boolean alreadyContained = methodList.stream().anyMatch(methodInMethods -> {
                boolean nameEquals = methodToAdd.getName().equals(methodInMethods.getName());
                boolean correctVisibility = MethodUtils.isPublicOrProtectedNotStatic(methodInMethods);
                boolean signatureEquals = Arrays.equals(methodToAdd.getParameterTypes(), methodInMethods.getParameterTypes());
                return nameEquals && correctVisibility && signatureEquals;
            });

            if (!alreadyContained) {
                methodList.add(methodToAdd);
            }
        }
    }

    private boolean isImplementedBySuperClass(String superName, Method m, ClassLoader loader) {
        try {
            if (this.combinedExcludedLookup.isPackageExcludedOrJdk(superName)) {
                return false;
            }
            // TODO What if super class is not loadable
            ClassReader classReader = new ClassReader(ClassUtils.getClassInputStream(superName, loader));
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


    private void discoverAllJdkInterfaces(Collection<String> interfacesToLookThrough, Set<String> result, TypeHierarchyReaderWithLoaderSupport typeHierarchyReader) {
        for (String interfaceName : interfacesToLookThrough) {
            if (this.combinedExcludedLookup.isPackageExcludedOrJdk(interfaceName) || this.combinedExcludedLookup.isAnnotation(interfaceName)) {
                result.add(interfaceName);
            }
            // Always recurse through super interfaces (even for JDK classes)
            // It can happen that a JDK abstract superclass implements a JDK interface
            try {
                List<String> superInterfaces = typeHierarchyReader.hierarchyOf(Type.getObjectType(interfaceName)).getInterfaces().stream().map(Type::getInternalName).collect(Collectors.toList());
                discoverAllJdkInterfaces(superInterfaces, result, typeHierarchyReader);
            } catch(Exception e) {
                // Class might be an optional dependency, continuing
                logger.debug("Skipped recursing further into {} due to Exception", interfaceName);
                Utils.logException(e);
            }
        }
    }

    /**
     * This method adds all methods if the passed interface list to the method list, if the method isn't already contained or the interface is already implemented by the super type
     * For determination if contained see {@link ClassTraverser#addMethodIfNotContained(Method)}
     *
     * @param superName                 Super class of the interface implementing one
     * @param directInheritedInterfaces Array with interface names as QN
     */
    public void addNotContainedJdkInterfaceMethods(String className, String superName, String[] directInheritedInterfaces, ClassResolver resolver, ClassLoader loader) {
        if (directInheritedInterfaces == null || directInheritedInterfaces.length == 0) {
            return;
        }
        TypeHierarchyReaderWithLoaderSupport typeHierarchyReader = new TypeHierarchyReaderWithLoaderSupport(resolver);

        // Find all JDK interfaces directly implemented by the class
        Set<String> jdkOnly = new HashSet<>();
        Set<String> directInheritedSet = new HashSet<String>(Arrays.asList(directInheritedInterfaces));
        discoverAllJdkInterfaces(directInheritedSet, jdkOnly, typeHierarchyReader);

        // Find all interfaces implemented by the super class
        Set<Type> superInterfaces = new HashSet<>();
        try {
        for (Type cls = Type.getObjectType(superName); cls != null; cls = typeHierarchyReader.getSuperClass(cls)) {
            try {
                TypeHierarchy hierarchy = typeHierarchyReader.hierarchyOf(cls);
                superInterfaces.addAll(hierarchy.getInterfaces());
            } catch(Exception e) {
                // Class might be an optional dependency, continuing
                logger.debug("Skipped recursing further into {} due to Exception", cls.getClassName());
                Utils.logException(e);
            }
        }
        } catch(Exception e) {
            // Class might be an optional dependency, continuing
            logger.debug("Skipped superclass extraction for into {} due to Exception", className);
            Utils.logException(e);
        }
        // JDK Interfaces implemented by the super class
        Set<String> jdkSuperInterfaces = new HashSet<>();
        discoverAllJdkInterfaces(superInterfaces.stream().map(Type::getInternalName).collect(Collectors.toList()), jdkSuperInterfaces, typeHierarchyReader);

        // Combine directly implemented and super class interfaces
        // Do not try to filter as we might have an abstract superclass which implements an interface but does not explicitly implement each method
        Set<String> interfaces = new HashSet<>(jdkOnly);
        interfaces.addAll(jdkSuperInterfaces);

        //System.out.printf("Class: %s, Super: %s, Direct interfaces %s SuperInterfaces: %s JdkSuperInterfaces: %s Interfaces: %s%n",
        //                  className, superName, directInheritedSet, superInterfaces, jdkSuperInterfaces, interfaces);

        for (String interfaceName : interfaces) {
            if (this.combinedExcludedLookup.isPackageExcludedOrJdk(interfaceName) || this.combinedExcludedLookup.isAnnotation(interfaceName)) {
                Class<?> cls = ClassUtils.findLoadedClass(interfaceName, loader);
                List<Method> intfMethods = new ArrayList<>();

                if (cls != null) {
                    for (java.lang.reflect.Method m : cls.getMethods()) {
                        Method method = Method.from(m);
                        intfMethods.add(method);
                    }
                } else {
                    try {
                        ClassVisitor cv = new NopVisitor(Opcodes.ASM9) {
                            @Override
                            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                                Method method = new Method(access, interfaceName, name, descriptor, signature, exceptions, true);
                                intfMethods.add(method);
                                return super.visitMethod(access, name, descriptor, signature, exceptions);
                            }
                        };
                        ClassReader cr;
                        Queue<String> superInterfaceNames = new ArrayDeque<>();
                        superInterfaceNames.add(interfaceName);
                        for (String intfName = superInterfaceNames.poll(); intfName != null; intfName = superInterfaceNames.poll()) {
                            cr = new ClassReaderWithLoaderSupport(resolver, intfName);
                            superInterfaceNames.addAll(Arrays.asList(cr.getInterfaces()));
                            cr.accept(cv, ClassReader.SKIP_FRAMES);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                for (Method method : intfMethods) {
                    if (!isImplementedBySuperClass(superName, method, loader)) {
                        addMethodIfNotContained(method);
                    }
                }
            }
        }
    }

    public Set<Method> getMethods() {
        return Collections.unmodifiableSet(this.methodList);
    }

    public static class MethodChecker extends ClassVisitor {
        private final Method method;
        private boolean implementsInterface = false;
        private boolean superImplements = false;

        public MethodChecker(Method method) {
            super(Opcodes.ASM9);
            this.method = method;
        }

        @Override
        public void visit(
                int version,
                final int access,
                final String name,
                final String signature,
                final String superName,
                final String[] interfaces) {
            // Check whether the superclass actually implements the method owners interface
            this.implementsInterface = Arrays.asList(interfaces).contains(this.method.getOwner());
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            // Only flag this method if the superclass implements the interface we want
            // There are some cases where the method is implemented but *not* as part of an interface!
            // (I'm looking at you org.apache.xerces.dom.CharacterDataImpl!)
            if (implementsInterface && name.equals(this.method.getName()) && this.method.getDescriptor().equals(descriptor) && (access & Opcodes.ACC_ABSTRACT) == 0) {
                superImplements = true;
            }
            return null;
        }
    }
}

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

    private final CombinedExcludedLookup combinedExcludedLookup;
    private final List<Method> methodList = new ArrayList<>();

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
                ClassVisitor cv = new NopVisitor(Opcodes.ASM7) {
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
                e.printStackTrace();
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
                        ClassVisitor cv = new NopVisitor(Opcodes.ASM7) {
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


    private void discoverAllJdkInterfaces(List<String> interfacesToLookThrough, Set<String> result, TypeHierarchyReaderWithLoaderSupport typeHierarchyReader) {
        for (String interfaceName : interfacesToLookThrough) {
            if (this.combinedExcludedLookup.isPackageExcludedOrJdk(interfaceName)) {
                result.add(interfaceName);
            } else if (this.combinedExcludedLookup.isAnnotation(interfaceName)) {
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
     * This methods add all methods if the passed interface list to the method list, if the method isn't already contained or the interface is already implemented by the super type
     * For determination if contained see {@link ClassTraverser#addMethodIfNotContained(Method)}
     *
     * @param superName                 Super class of the interface implementing one
     * @param directInheritedInterfaces Array with interface names as QN
     */
    public void addNotContainedJdkInterfaceMethods(String superName, String[] directInheritedInterfaces, ClassResolver resolver, ClassLoader loader) {
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
            if (this.combinedExcludedLookup.isPackageExcludedOrJdk(interfaceName) || this.combinedExcludedLookup.isAnnotation(interfaceName)) {
                Class<?> cls = ClassUtils.findLoadedClass(interfaceName);
                List<Method> intfMethods = new ArrayList<>();

                if (cls != null) {
                    for (java.lang.reflect.Method m : cls.getMethods()) {
                        Method method = Method.from(m);
                        intfMethods.add(method);
                    }
                } else {
                    try {
                        ClassVisitor cv = new NopVisitor(Opcodes.ASM7) {
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

    public List<Method> getMethods() {
        return Collections.unmodifiableList(this.methodList);
    }

    public static class MethodChecker extends ClassVisitor {
        private final Method method;
        private boolean superImplements = false;

        public MethodChecker(Method method) {
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

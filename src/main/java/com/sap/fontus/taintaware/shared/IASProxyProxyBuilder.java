package com.sap.fontus.taintaware.shared;

import com.sap.fontus.Constants;
import com.sap.fontus.utils.Utils;
import org.objectweb.asm.*;

import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;

public class IASProxyProxyBuilder {
    private static volatile int counter = 0;
    private static final String PROXY_BASE_PACKAGE = Constants.PACKAGE_NEW + ".internal";
    public static final String HANDLER_FIELD_NAME = "h";
    private final String name;
    private final Class<?>[] interfaces;
    private final ClassWriter classWriter;
    //    private final Module module;
    private final ClassLoader classLoader;

    public String getName() {
        return name;
    }

    private IASProxyProxyBuilder(String name, Class<?>[] interfaces, ClassWriter classWriter, ClassLoader classLoader) {
        this.classWriter = classWriter;
        this.name = name;
        this.interfaces = interfaces;
        this.classLoader = classLoader;
//        this.module = findModule();
    }

    private static String calculatePackage(Class<?>[] interfaces) {
        String pkg = null;
        for (Class<?> intf : interfaces) {
            if (!Modifier.isPublic(intf.getModifiers())) {
                String packageName = intf.getPackageName();
                if (pkg == null) {
                    pkg = packageName;
                } else {
                    if (!pkg.equals(packageName)) {
                        throw new IllegalArgumentException("Package-private interfaces are not all in the same package!");
                    }
                }
            }
        }

        if (pkg == null) {
            return PROXY_BASE_PACKAGE;
        } else {
            return pkg;
        }
    }

    private Module findModule() {
        // TODO better module handling (exported and not exported interfaces)
        Module module = null;
        for (Class<?> intf : this.interfaces) {
            if (module == null) {
                module = intf.getModule();
            } else {
                if (!module.equals(intf.getModule())) {
                    throw new IllegalArgumentException("Provided interfaces from different modules");
                }
            }
        }

        return module;

//        if (!cachedModules.containsKey(this.classLoader)) {
//            String name = PROXY_CLASS_BASE_NAME + ".proxy" + packageCounter.incrementAndGet();
//            ModuleDescriptor moduleDescriptor = ModuleDescriptor
//                    .newModule(PROXY_CLASS_BASE_NAME, Set.of(ModuleDescriptor.Modifier.SYNTHETIC))
//                    .packages(Set.of(PROXY_CLASS_BASE_NAME))
//                    .build();
//            Module m = JLA.defineModule(this.classLoader, moduleDescriptor, null);
//
//            JLA.addReads(m, IASProxyProxy.class.getModule());
//            JLA.addExports(m, name, Object.class.getModule());
//            cachedModules.put(classLoader, m);
//        }
//        return cachedModules.get(this.classLoader);
    }

    public byte[] build() {
        generateClassHeader();

        generateInvocationHandlerField();

        generateConstructor();

        List<ProxyMethod> methods = generateProxyMethods();

        generateMethodFields(methods);

        generateMethods(methods);

        generateStaticInitializer(methods);

        return this.classWriter.toByteArray();
    }

    private void generateInvocationHandlerField() {
        FieldVisitor fv = this.classWriter.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, HANDLER_FIELD_NAME, Type.getType(InvocationHandler.class).getDescriptor(), null, null);
        fv.visitEnd();
    }

    public static IASProxyProxyBuilder newBuilder(Class<?>[] interfaces, ClassLoader classLoader) {
        // This is necessary because otherwise the Bootstrap Classloader is used to load application classes in visitMaxs
        // See https://stackoverflow.com/questions/26573945/classnotfoundexception-at-asm-objectwriter-getcommonsuperclass
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected ClassLoader getClassLoader() {
                return classLoader;
            }
        };
        String name = newProxyName(interfaces);

        return new IASProxyProxyBuilder(Utils.dotToSlash(name), interfaces, classWriter, classLoader);
    }

    public void generateProxyMethod(ProxyMethod proxyMethod) {
        Method method = proxyMethod.getMethod();
        String methodFieldName = proxyMethod.getMethodFieldName();
        List<Class<?>> exceptions = proxyMethod.getAllExceptions();
        Label startLabel = new Label();
        Label endLabel = new Label();
        Label annotatedExceptionsLabel = new Label();
        Label unknownExceptionLabel = new Label();

        String[] exceptionNames = new String[exceptions.size()];
        for (int i = 0; i < exceptions.size(); i++) {
            exceptionNames[i] = Utils.getInternalName(exceptions.get(i));
        }

        MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        Type[] args = Type.getArgumentTypes(method);

        int modifiers = method.getModifiers();
        modifiers &= ~Opcodes.ACC_ABSTRACT;
        MethodVisitor mv = classWriter.visitMethod(modifiers, method.getName(), methodType.toMethodDescriptorString(), null, exceptionNames);
        mv.visitCode();

        if (exceptions.size() > 0) {
            for (Class<?> ex : exceptions) {
                mv.visitTryCatchBlock(startLabel, endLabel, annotatedExceptionsLabel,
                        Utils.getInternalName(ex));
            }

            mv.visitTryCatchBlock(startLabel, endLabel, unknownExceptionLabel,
                    Utils.getInternalName(Throwable.class));
        }

        mv.visitLabel(startLabel);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, this.name, HANDLER_FIELD_NAME, Type.getType(InvocationHandler.class).getDescriptor());

        mv.visitVarInsn(Opcodes.ALOAD, 0);

        mv.visitFieldInsn(Opcodes.GETSTATIC, this.name, methodFieldName, Type.getType(Method.class).getDescriptor());

        if (method.getParameterCount() > 0) {
            mv.visitLdcInsn(method.getParameterCount());
            mv.visitTypeInsn(Opcodes.ANEWARRAY, Utils.getInternalName(Object.class));

            for (int i = 0; i < args.length; i++) {
                mv.visitInsn(Opcodes.DUP);
                mv.visitLdcInsn(i);
                loadAndWrapParameter(mv, method.getParameterTypes()[i], i + 1);
                mv.visitInsn(Opcodes.AASTORE);
            }
        } else {
            mv.visitInsn(Opcodes.ACONST_NULL);
        }

        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, Utils.getInternalName(InvocationHandler.class), "invoke", MethodType.methodType(Object.class, new Class[]{Object.class, Method.class, Object[].class}).toMethodDescriptorString(), true);

        if (method.getReturnType() == void.class) {
            mv.visitInsn(Opcodes.RETURN);
        } else {
            unwrapParameter(mv, method.getReturnType());
            mv.visitInsn(Type.getType(method.getReturnType()).getOpcode(Opcodes.IRETURN));
        }

        mv.visitLabel(endLabel);

        mv.visitLabel(annotatedExceptionsLabel);
        mv.visitInsn(Opcodes.ATHROW);

        mv.visitLabel(unknownExceptionLabel);
        mv.visitTypeInsn(Opcodes.NEW, Utils.getInternalName(UndeclaredThrowableException.class));
        mv.visitInsn(Opcodes.DUP);
        mv.visitInsn(Opcodes.DUP2_X1);
        mv.visitInsn(Opcodes.POP2);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Utils.getInternalName(UndeclaredThrowableException.class), Constants.Init, MethodType.methodType(void.class, Throwable.class).toMethodDescriptorString(), false);
        mv.visitInsn(Opcodes.ATHROW);

        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private void unwrapParameter(MethodVisitor mv, Class<?> type) {
        if (type.isPrimitive()) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Utils.getInternalName(primitiveToWrapper(type)));
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Utils.getInternalName(primitiveToWrapper(type)), type.getCanonicalName() + "Value", MethodType.methodType(type).toMethodDescriptorString(), false);
        } else {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Utils.getInternalName(type));
        }
    }

    private void loadAndWrapParameter(MethodVisitor mv, Class<?> arg, int registerIndex) {
        if (arg.isPrimitive()) {
            int opcode = Type.getType(arg).getOpcode(Opcodes.ILOAD);
            Class<?> wrapper = primitiveToWrapper(arg);
            mv.visitVarInsn(opcode, registerIndex);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Utils.getInternalName(wrapper), "valueOf", MethodType.methodType(wrapper, new Class[]{arg}).toMethodDescriptorString(), false);
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, registerIndex);
        }
    }

    private Class<?> primitiveToWrapper(Class<?> cls) {
        if (cls == byte.class) {
            return Byte.class;
        } else if (cls == short.class) {
            return Short.class;
        } else if (cls == int.class) {
            return Integer.class;
        } else if (cls == long.class) {
            return Long.class;
        } else if (cls == boolean.class) {
            return Boolean.class;
        } else if (cls == float.class) {
            return Float.class;
        } else if (cls == double.class) {
            return Double.class;
        } else if (cls == char.class) {
            return Character.class;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void generateConstructor() {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, Constants.Init, MethodType.methodType(void.class, InvocationHandler.class).toMethodDescriptorString(), null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Utils.getInternalName(Object.class), Constants.Init, MethodType.methodType(void.class).toMethodDescriptorString(), false);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, this.name, HANDLER_FIELD_NAME, Type.getType(InvocationHandler.class).getDescriptor());
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private void generateClassHeader() {
        String[] interfaceNames = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            interfaceNames[i] = Utils.getInternalName(interfaces[i]);
        }
//        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, Utils.dotToSlash(this.name), null, Utils.getInternalName(IASProxyProxy.class), interfaceNames);
        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, Utils.dotToSlash(this.name), null, Utils.getInternalName(Object.class), interfaceNames);
    }

    private void generateMethodFields(List<ProxyMethod> methods) {
        for (ProxyMethod proxyMethod : methods) {
            String methodFieldName = proxyMethod.getMethodFieldName();

            FieldVisitor fv = this.classWriter.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, methodFieldName, Type.getType(Method.class).getDescriptor(), null, null);
            fv.visitEnd();
        }
    }

    private void generateStaticInitializer(List<ProxyMethod> methods) {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.INVOKESTATIC, Constants.ClInit, "()V", null, null);

        for (ProxyMethod proxyMethod : methods) {
            Method method = proxyMethod.getMethod();
            String methodFieldName = proxyMethod.getMethodFieldName();

            mv.visitLdcInsn(Type.getType(method.getDeclaringClass()));
            mv.visitLdcInsn(method.getName());

            mv.visitLdcInsn(method.getParameterCount());
            mv.visitTypeInsn(Opcodes.ANEWARRAY, Utils.getInternalName(Class.class));
            for (int i = 0; i < method.getParameterCount(); i++) {
                Class<?> param = method.getParameterTypes()[i];
                mv.visitInsn(Opcodes.DUP);
                mv.visitLdcInsn(i);
                if (param.isPrimitive()) {
                    mv.visitFieldInsn(Opcodes.GETSTATIC, Utils.getInternalName(primitiveToWrapper(param)), "TYPE", Type.getType(Class.class).getDescriptor());
                } else {
                    mv.visitLdcInsn(Type.getType(param));
                }
                mv.visitInsn(Opcodes.AASTORE);
            }

            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Utils.getInternalName(Class.class), "getMethod", MethodType.methodType(Method.class, String.class, Class[].class).toMethodDescriptorString(), false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, this.name, methodFieldName, Type.getType(Method.class).getDescriptor());
        }
        mv.visitInsn(Opcodes.RETURN);

        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private void generateMethods(List<ProxyMethod> methods) {
        for (ProxyMethod m : methods) {
            generateProxyMethod(m);
        }
    }

    private List<ProxyMethod> generateProxyMethods() {
        int counter = 0;
        List<ProxyMethod> methods = new ArrayList<>();
        for (Class<?> intf : interfaces) {
            for (Method m : intf.getMethods()) {
                if (!Modifier.isStatic(m.getModifiers())) {
                    ProxyMethod newProxyMethod = new ProxyMethod(m, "m" + counter);

                    boolean isAlreadyContained = false;
                    for (ProxyMethod pm : methods) {
                        if (pm.getNameWithDescriptor().equals(newProxyMethod.getNameWithDescriptor())) {
                            isAlreadyContained = true;
                            pm.addExceptions(m.getExceptionTypes());
                            break;
                        }
                    }

                    if (!isAlreadyContained) {
                        methods.add(newProxyMethod);
                        counter++;
                    }
                }
            }
        }
        return methods;
    }

    private static class ProxyMethod {
        private final Method method;
        private final String methodFieldName;
        private final List<Class<?>> exceptionTypes = new ArrayList<>();

        public Method getMethod() {
            return method;
        }

        public String getMethodFieldName() {
            return methodFieldName;
        }

        public ProxyMethod(Method method, String methodFieldName) {
            this.method = method;
            this.methodFieldName = methodFieldName;
            this.addExceptions(method.getExceptionTypes());
        }

        public String getNameWithDescriptor() {
            return this.method.getName() + org.objectweb.asm.commons.Method.getMethod(this.method).getDescriptor();
        }

        public void addExceptions(Class<?>[] exceptionTypes) {
            for (Class<?> ex : exceptionTypes) {
                boolean replaced = false;
                for (int i = 0; i < this.exceptionTypes.size(); i++) {
                    Class<?> origEx = this.exceptionTypes.get(i);
                    if (origEx.isAssignableFrom(ex)) {
                        this.exceptionTypes.set(i, ex);
                        replaced = true;
                        break;
                    }
                }

                if (!replaced) {
                    this.exceptionTypes.add(ex);
                }
            }
        }

        public List<Class<?>> getAllExceptions() {
            return new ArrayList<>(this.exceptionTypes);
        }
    }

    private static synchronized String newProxyName(Class<?>[] interfaces) {
        String packageName = calculatePackage(interfaces);
        String name = packageName + ".$Proxy" + counter;
        counter++;
        return name;
    }
}

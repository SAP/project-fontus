package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.utils.Utils;
import org.objectweb.asm.*;

import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

public class IASProxyProxyBuilder {
    public static final String HANDLER_FIELD_NAME = "h";
    private final String name;
    private final Class<?>[] interfaces;
    private final ClassWriter classWriter;

    private IASProxyProxyBuilder(String name, Class<?>[] interfaces, ClassWriter classWriter) {
        this.classWriter = classWriter;
        this.name = name;
        this.interfaces = interfaces;
    }

    public byte[] build() {
        generateClassHeader();

        generateConstructor();

        List<ProxyMethod> methods = generateProxyMethods();

        generateMethodFields(methods);

        generateMethods(methods);

        generateStaticInitializer(methods);

        return classWriter.toByteArray();
    }

    public static IASProxyProxyBuilder newBuilder(String name, Class<?>[] interfaces) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        return new IASProxyProxyBuilder(Utils.dotToSlash(name), interfaces, classWriter);
    }

    public void generateProxyMethod(ProxyMethod proxyMethod) {
        Method method = proxyMethod.getMethod();
        String methodFieldName = proxyMethod.getMethodFieldName();
        Class<?>[] exceptions = method.getExceptionTypes();
        Label startLabel = new Label();
        Label endLabel = new Label();
        Label annotatedExceptionsLabel = new Label();
        Label unknownExceptionLabel = new Label();

        String[] exceptionNames = new String[exceptions.length];
        for (int i = 0; i < exceptions.length; i++) {
            exceptionNames[i] = Utils.getInternalName(exceptions[i]);
        }

        MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        Type[] args = Type.getArgumentTypes(method);

        int modifiers = method.getModifiers();
        modifiers &= ~Opcodes.ACC_ABSTRACT;
        MethodVisitor mv = classWriter.visitMethod(modifiers, method.getName(), methodType.toMethodDescriptorString(), null, exceptionNames);
        mv.visitCode();

        if (exceptions.length > 0) {
            for (Class<?> ex : exceptions) {
                mv.visitTryCatchBlock(startLabel, endLabel, annotatedExceptionsLabel,
                        Utils.getInternalName(ex));
            }

            mv.visitTryCatchBlock(startLabel, endLabel, unknownExceptionLabel,
                    Utils.getInternalName(Throwable.class));
        }

        mv.visitLabel(startLabel);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, Utils.getInternalName(IASProxyProxy.class), "h", Type.getType(InvocationHandler.class).getDescriptor());
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETSTATIC, this.name, methodFieldName, Descriptor.classNameToDescriptorName(Method.class.getName()));

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
            mv.visitInsn(Opcodes.ARETURN);
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
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Utils.getInternalName(primitiveToWrapper(type)), type.getCanonicalName() + "Value", "()" + type.getName(), false);
        }
    }

    private void loadAndWrapParameter(MethodVisitor mv, Class<?> arg, int registerIndex) {
        if (arg.isPrimitive()) {
            int opcode = Type.getType(arg).getOpcode(Opcodes.ALOAD);
            Class<?> wrapper = primitiveToWrapper(arg);
            mv.visitVarInsn(opcode, registerIndex);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Utils.getInternalName(arg), "valueOf", MethodType.methodType(arg, new Class[]{wrapper}).toMethodDescriptorString(), false);
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
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Utils.getInternalName(IASProxyProxy.class), Constants.Init, MethodType.methodType(void.class, InvocationHandler.class).toMethodDescriptorString(), false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private void generateClassHeader() {
        String[] interfaceNames = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            interfaceNames[i] = Utils.getInternalName(interfaces[i]);
        }
        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, Utils.dotToSlash(this.name), null, Utils.getInternalName(IASProxyProxy.class), interfaceNames);
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
                mv.visitLdcInsn(Type.getType(param));
                mv.visitInsn(Opcodes.AASTORE);
            }

            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Utils.getInternalName(Class.class), "getMethod", MethodType.methodType(Method.class, String.class, Class[].class).toMethodDescriptorString(), false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, this.name, methodFieldName, Descriptor.classNameToDescriptorName(Method.class.getName()));

            mv.visitInsn(Opcodes.RETURN);

            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }
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
                methods.add(new ProxyMethod(m, "m" + counter));
                counter++;
            }
        }
        return methods;
    }

    private static class ProxyMethod {
        private Method method;
        private String methodFieldName;

        public Method getMethod() {
            return method;
        }

        public String getMethodFieldName() {
            return methodFieldName;
        }

        public ProxyMethod(Method method, String methodFieldName) {
            this.method = method;
            this.methodFieldName = methodFieldName;
        }
    }
}

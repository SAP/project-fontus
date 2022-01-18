package com.sap.fontus.instrumentation;

import com.sap.fontus.Constants;
import com.sap.fontus.asm.*;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.Sink;
import com.sap.fontus.config.Source;
import com.sap.fontus.instrumentation.transformer.*;
import com.sap.fontus.taintaware.unified.*;
import com.sap.fontus.taintaware.unified.reflect.*;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.Utils;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.objectweb.asm.*;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.lang.reflect.Method;
import java.util.*;


@SuppressWarnings("deprecation")
public class MethodTaintingVisitor extends BasicMethodVisitor {
    private static final Logger logger = LogUtils.getLogger();
    private final boolean implementsInvocationHandler;
    private final String owner;

    private final String name;
    private final String methodDescriptor;
    private final ClassResolver resolver;
    /**
     * Some methods are not handled in a generic fashion, one can defined specialized proxies here
     */
    private final HashMap<FunctionCall, FunctionCall> methodProxies;
    /**
     * Some dynamic method invocations can't be handled generically. Add proxy functions here.
     */
    private final HashMap<ProxiedDynamicFunctionEntry, Runnable> dynProxies;
    private final String ownerSuperClass;
    private final List<LambdaCall> jdkLambdaMethodProxies;
    private final boolean isOwnerInterface;

    private int used;
    private int usedAfterInjection;

    private final InstrumentationHelper instrumentationHelper;

    private final Configuration config;

    private final CombinedExcludedLookup combinedExcludedLookup;
    private final List<DynamicCall> bootstrapMethods;
    private final SignatureInstrumenter signatureInstrumenter;

    /**
     * If a method which is part of an interface should be proxied, place it here
     * The owner should be the interface
     */
    private final Map<FunctionCall, FunctionCall> methodInterfaceProxies;
    private final ClassLoader loader;

    public MethodTaintingVisitor(int acc, String owner, String name, String methodDescriptor, MethodVisitor methodVisitor, ClassResolver resolver, Configuration config, boolean implementsInvocationHandler, InstrumentationHelper instrumentationHelper, CombinedExcludedLookup combinedExcludedLookup, List<DynamicCall> bootstrapMethods, List<LambdaCall> jdkLambdaMethodProxies, String ownerSuperClass, ClassLoader loader, boolean isOwnerInterface) {
        super(Opcodes.ASM9, methodVisitor);
        this.resolver = resolver;
        this.owner = owner;
        this.isOwnerInterface = isOwnerInterface;
        this.combinedExcludedLookup = combinedExcludedLookup;
        this.bootstrapMethods = bootstrapMethods;
        logger.info("Instrumenting method: {}{}", name, methodDescriptor);
        this.used = Type.getArgumentsAndReturnSizes(methodDescriptor) >> 2;
        this.usedAfterInjection = this.used;
        if ((acc & Opcodes.ACC_STATIC) != 0) this.used--; // no this
        this.name = name;
        this.methodDescriptor = methodDescriptor;
        this.instrumentationHelper = instrumentationHelper;
        this.signatureInstrumenter = new SignatureInstrumenter(this.api, this.instrumentationHelper);
        this.implementsInvocationHandler = implementsInvocationHandler;
        this.methodProxies = new HashMap<>();
        this.methodInterfaceProxies = new HashMap<>();
        this.dynProxies = new HashMap<>();
        this.jdkLambdaMethodProxies = jdkLambdaMethodProxies;
        this.loader = loader;
        this.ownerSuperClass = ownerSuperClass;
        this.fillProxies();
        this.fillInterfaceProxies();
        this.config = config;
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        // TODO: Why do we ignore this?
        //String desc = this.instrumentationHelper.instrumentQN(descriptor);
        //String sig = this.signatureInstrumenter.instrumentSignature(signature);
        //System.out.printf("Local var: %s: %s [%s] -> %s [%s]%n", name, descriptor, signature, desc, sig);
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    /**
     * See https://stackoverflow.com/questions/47674972/getting-the-number-of-local-variables-in-a-method
     * for keeping track of used locals..
     */

    @Override
    public void visitFrame(
            int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        if (type != Opcodes.F_NEW)
            throw new IllegalStateException("only expanded frames supported");
        int l = numLocal;
        for (int ix = 0; ix < numLocal; ix++)
            if (local[ix] == Opcodes.LONG || local[ix] == Opcodes.DOUBLE) l++;
        if (l > this.used) this.used = l;
        super.visitFrame(type, numLocal, local, numStack, stack);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        int newMax = var + Utils.storeOpcodeSize(opcode);
        if (newMax > this.used) this.used = newMax;
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, Math.max(this.used, this.usedAfterInjection));
    }

    public void visitMethodInsn(FunctionCall fc) {
        logger.info("Invoking [{}] {}.{}{}", Utils.opcodeToString(fc.getOpcode()), fc.getOwner(), fc.getName(), fc.getDescriptor());
        super.visitMethodInsn(fc.getOpcode(), fc.getOwner(), fc.getName(), fc.getDescriptor(), fc.isInterface());
    }

    /**
     * Initializes the method proxy maps.
     */
    private void fillProxies() {
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/lang/System", "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASStringUtils.class), "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", false));
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "forName", String.format("(%s)Ljava/lang/Class;", Type.getDescriptor(IASString.class)), false));
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "forName", String.format("(%sZLjava/lang/ClassLoader;)Ljava/lang/Class;", Type.getDescriptor(IASString.class)), false));
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/net/URLEncoder", "encode", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(TURLEncoder.class), "encode", String.format("(%s%s)%s", Type.getDescriptor(IASString.class), Type.getDescriptor(IASString.class), Type.getDescriptor(IASString.class)), false));
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/net/URLEncoder", "encode", "(Ljava/lang/String;)Ljava/lang/String;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(TURLEncoder.class), "encode", String.format("(%s)%s", Type.getDescriptor(IASString.class), Type.getDescriptor(IASString.class)), false));
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/net/URLDecoder", "decode", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(TURLDecoder.class), "decode", String.format("(%s%s)%s", Type.getDescriptor(IASString.class), Type.getDescriptor(IASString.class), Type.getDescriptor(IASString.class)), false));
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/net/URLDecoder", "decode", "(Ljava/lang/String;)Ljava/lang/String;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(TURLDecoder.class), "decode", String.format("(%s)%s", Type.getDescriptor(IASString.class), Type.getDescriptor(IASString.class)), false));
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/lang/System", "getenv", "()Ljava/util/Map;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASStringUtils.class), "getenv", "()Ljava/util/Map;", false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false),
                 new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getName", Type.getMethodDescriptor(Type.getType(IASString.class), Type.getType(Class.class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false),
                 new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getSimpleName", Type.getMethodDescriptor(Type.getType(IASString.class), Type.getType(Class.class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getCanonicalName", "()Ljava/lang/String;", false),
                 new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getCanonicalName", Type.getMethodDescriptor(Type.getType(IASString.class), Type.getType(Class.class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getTypeParameters", Type.getMethodDescriptor(Type.getType(TypeVariable[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getTypeParameters", Type.getMethodDescriptor(Type.getType(TypeVariable[].class), Type.getType(Class.class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getInterfaces", Type.getMethodDescriptor(Type.getType(Class[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getInterfaces", Type.getMethodDescriptor(Type.getType(Class[].class), Type.getType(Class.class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getGenericInterfaces", Type.getMethodDescriptor(Type.getType(java.lang.reflect.Type.class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getGenericInterfaces", Type.getMethodDescriptor(Type.getType(java.lang.reflect.Type[].class), Type.getType(Class.class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getEnclosingMethod", Type.getMethodDescriptor(Type.getType(Method.class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getEnclosingMethod", Type.getMethodDescriptor(Type.getType(IASMethod.class), Type.getType(Class.class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getEnclosingConstructor", Type.getMethodDescriptor(Type.getType(Constructor.class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getEnclosingConstructor", Type.getMethodDescriptor(Type.getType(IASConstructor.class), Type.getType(Class.class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getMethods", Type.getMethodDescriptor(Type.getType(Method[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getMethods", Type.getMethodDescriptor(Type.getType(IASMethod[].class), Type.getType(Class.class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethods", Type.getMethodDescriptor(Type.getType(Method[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getDeclaredMethods", Type.getMethodDescriptor(Type.getType(IASMethod[].class), Type.getType(Class.class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getMethod", Type.getMethodDescriptor(Type.getType(Method.class), Type.getType(String.class), Type.getType(Class[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getMethod", Type.getMethodDescriptor(Type.getType(IASMethod.class), Type.getType(Class.class), Type.getType(IASString.class), Type.getType(Class[].class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod", Type.getMethodDescriptor(Type.getType(Method.class), Type.getType(String.class), Type.getType(Class[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getDeclaredMethod", Type.getMethodDescriptor(Type.getType(IASMethod.class), Type.getType(Class.class), Type.getType(IASString.class), Type.getType(Class[].class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getFields", Type.getMethodDescriptor(Type.getType(Field[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getFields", Type.getMethodDescriptor(Type.getType(IASField[].class), Type.getType(Class.class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredFields", Type.getMethodDescriptor(Type.getType(Field[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getDeclaredFields", Type.getMethodDescriptor(Type.getType(IASField[].class), Type.getType(Class.class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getField", Type.getMethodDescriptor(Type.getType(Field.class), Type.getType(String.class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getField", Type.getMethodDescriptor(Type.getType(IASField.class), Type.getType(Class.class), Type.getType(IASString.class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredField", Type.getMethodDescriptor(Type.getType(Field.class), Type.getType(String.class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getDeclaredField", Type.getMethodDescriptor(Type.getType(IASField.class), Type.getType(Class.class), Type.getType(IASString.class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getConstructors", Type.getMethodDescriptor(Type.getType(Constructor[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getConstructors", Type.getMethodDescriptor(Type.getType(IASConstructor[].class), Type.getType(Class.class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredConstructors", Type.getMethodDescriptor(Type.getType(Constructor[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getDeclaredConstructors", Type.getMethodDescriptor(Type.getType(IASConstructor[].class), Type.getType(Class.class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getConstructor", Type.getMethodDescriptor(Type.getType(Constructor.class), Type.getType(Class[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getConstructor", Type.getMethodDescriptor(Type.getType(IASConstructor.class), Type.getType(Class.class), Type.getType(Class[].class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredConstructor", Type.getMethodDescriptor(Type.getType(Constructor.class), Type.getType(Class[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getDeclaredConstructor", Type.getMethodDescriptor(Type.getType(IASConstructor.class), Type.getType(Class.class), Type.getType(Class[].class)), false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getResourceAsStream", "(Ljava/lang/String;)Ljava/io/InputStream;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getResourceAsStream", "(Ljava/lang/Class;Lcom/sap/fontus/taintaware/unified/IASString;)Ljava/io/InputStream;", false));
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getPackage", "()Ljava/lang/Package;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASReflectionProxy.class), "getPackageOfClass", "(Ljava/lang/Class;)Ljava/lang/Package;", false));

        this.methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/ClassLoader", "getResourceAsStream", "(Ljava/lang/String;)Ljava/io/InputStream;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassLoaderProxy.class), "getResourceAsStream", "(Ljava/lang/ClassLoader;Lcom/sap/fontus/taintaware/unified/IASString;)Ljava/io/InputStream;", false));
    }

    private void fillInterfaceProxies() {
        this.methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/util/Collection", "toArray", "([Ljava/lang/Object;)[Ljava/lang/Object;", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASToArrayProxy.class), "toArray", String.format("(L%s;[Ljava/lang/Object;)[Ljava/lang/Object;", Utils.dotToSlash(Collection.class.getName())), false));
        this.methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/util/Collection", "toArray", "()[Ljava/lang/Object;", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASToArrayProxy.class), "toArray", String.format("(L%s;)[Ljava/lang/Object;", Utils.dotToSlash(Collection.class.getName())), false));
        this.methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/sql/PreparedStatement", "setString", "(ILjava/lang/String;)V", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASPreparedStatementUtils.class), "setString", String.format("(L%s;ILcom/sap/fontus/taintaware/unified/IASString;)V", Utils.dotToSlash(java.sql.PreparedStatement.class.getName())), false));
        this.methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/sql/PreparedStatement", "setNString", "(ILjava/lang/String;)V", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASPreparedStatementUtils.class), "setNString", String.format("(L%s;ILcom/sap/fontus/taintaware/unified/IASString;)V", Utils.dotToSlash(java.sql.PreparedStatement.class.getName())), false));
        this.methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/sql/PreparedStatement", "setObject", "(ILjava/lang/Object;I)V", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASPreparedStatementUtils.class), "setObject", String.format("(L%s;ILjava/lang/Object;I)V", Utils.dotToSlash(java.sql.PreparedStatement.class.getName())), false));
        this.methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/sql/PreparedStatement", "setObject", "(ILjava/lang/Object;II)V", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASPreparedStatementUtils.class), "setObject", String.format("(L%s;ILjava/lang/Object;II)V", Utils.dotToSlash(java.sql.PreparedStatement.class.getName())), false));
        this.methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/sql/PreparedStatement", "setObject", "(ILjava/lang/Object;)V", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASPreparedStatementUtils.class), "setObject", String.format("(L%s;ILjava/lang/Object;)V", Utils.dotToSlash(java.sql.PreparedStatement.class.getName())), false));
        this.methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/sql/ResultSet", "getString", "(I)Ljava/lang/String;", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASResultSetUtils.class), "getString", String.format("(L%s;I)Lcom/sap/fontus/taintaware/unified/IASString;", Utils.dotToSlash(java.sql.ResultSet.class.getName())), false));
        this.methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/sql/ResultSet", "getString", "(Ljava/lang/String;)Ljava/lang/String;", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASResultSetUtils.class), "getString", String.format("(L%s;Lcom/sap/fontus/taintaware/unified/IASString;)Lcom/sap/fontus/taintaware/unified/IASString;", Utils.dotToSlash(java.sql.ResultSet.class.getName())), false));
    }


    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.ARETURN && this.isInvocationHandlerMethod(this.name, this.methodDescriptor)) {
            // Handling, that method proxies return the correct type (we're in a InvocationHandler.invoke implementation)
            super.visitVarInsn(Opcodes.ALOAD, 1); // Load proxy param
            super.visitVarInsn(Opcodes.ALOAD, 2); // Load method param
            super.visitVarInsn(Opcodes.ALOAD, 3); // Load args param
            String resultConverterDescriptor = String.format("(L%s;L%s;L%s;[L%s;)L%s;", Utils.dotToSlash(Object.class.getName()), Utils.dotToSlash(Object.class.getName()), Utils.dotToSlash(Method.class.getName()), Utils.dotToSlash(Object.class.getName()), Utils.dotToSlash(Object.class.getName()));
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASReflectionProxy.class), "handleInvocationProxyCall", resultConverterDescriptor, false);
        }
        super.visitInsn(opcode);
    }

    private boolean isInvocationHandlerMethod(String name, String descriptor) {
        boolean nameEquals = name.equals("invoke");
        String expectedDescriptor = String.format("(L%s;L%s;[L%s;)L%s;", Utils.dotToSlash(Object.class.getName()), Utils.dotToSlash(Method.class.getName()), Utils.dotToSlash(Object.class.getName()), Utils.dotToSlash(Object.class.getName()));
        boolean descriptorEquals = descriptor.equals(expectedDescriptor);
        return nameEquals && descriptorEquals && this.implementsInvocationHandler;
    }

    /**
     * Replace access to fields of type IASString/IASStringBuilder
     */
    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {

        if (this.combinedExcludedLookup.isJdkClass(owner) && this.instrumentationHelper.canHandleType(descriptor)) {
            if ((opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC)) {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ConversionUtilsQN, Constants.ConversionUtilsToOrigName, Constants.ConversionUtilsToOrigDesc, false);
                mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(descriptor).getInternalName());
                mv.visitFieldInsn(opcode, owner, name, descriptor);
            } else {
                mv.visitFieldInsn(opcode, owner, name, descriptor);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ConversionUtilsQN, Constants.ConversionUtilsToConcreteName, Constants.ConversionUtilsToConcreteDesc, false);
                Type fieldType = Type.getType(descriptor);
                String instrumentedFieldDescriptor = this.instrumentationHelper.instrumentQN(fieldType.getInternalName());
                mv.visitTypeInsn(Opcodes.CHECKCAST, instrumentedFieldDescriptor);
            }
            return;
        }

        if (this.instrumentationHelper.instrumentFieldIns(mv, opcode, owner, name, descriptor)) {
            return;
        }
    }

    /**
     * All method calls are handled here.
     */
    @Override
    public void visitMethodInsn(
            final int opcode,
            final String owner,
            final String name,
            final String descriptor,
            final boolean isInterface) {
        FunctionCall fc = new FunctionCall(opcode, owner, name, descriptor, isInterface);

        if (this.combinedExcludedLookup.isFontusClass(owner)) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            return;
        }

        // If a method has a defined proxy, apply it right away
        fc = this.shouldBeProxied(fc);

        FunctionCall functionCall = this.instrumentationHelper.rewriteOwnerMethod(fc);
        if (functionCall != null) {
            super.visitMethodInsn(functionCall.getOpcode(), functionCall.getOwner(), functionCall.getName(), functionCall.getDescriptor(), functionCall.isInterface());
            return;
        }

        if (this.name.equals("toString") && this.methodDescriptor.equals(Type.getMethodDescriptor(Type.getType(IASString.class)))
                && opcode == Opcodes.INVOKESPECIAL && name.equals("toString") && descriptor.equals(Type.getMethodDescriptor(Type.getType(String.class)))
                && this.combinedExcludedLookup.isPackageExcludedOrJdk(owner)
                && !this.combinedExcludedLookup.isPackageExcludedOrJdk(this.ownerSuperClass)) {
            Descriptor instrumented = new Descriptor(Type.getType(IASString.class));
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, this.ownerSuperClass, name, instrumented.toDescriptor(), false);
            return;
        }

        if (this.isVirtualOrStaticMethodHandleLookup(fc)) {
            this.generateVirtualOrStaticMethodHandleLookupIntercept(fc);
            return;
        }

        if (this.isConstructorMethodHandleLookup(fc)) {
            this.generateConstructorMethodHandleLookupIntercept(fc);
            return;
        }

        if (this.isSpecialMethodHandleLookup(fc)) {
            this.generateSpecialMethodHandleLookupIntercept(fc);
            return;
        }

        // Call any functions which manipulate function call parameters and return types
        // for example sources, sinks and JDK functions
        if (!this.isRelevantMethodHandleInvocation(fc) && this.rewriteParametersAndReturnType(fc)) {
            return;
        }

        String desc = this.instrumentationHelper.instrumentForNormalCall(fc.getDescriptor());
        // TODO: hack that we can't reset desc for fc here
        if (desc.equals(fc.getDescriptor())) {
            logger.info("Skipping invoke [{}] {}.{}{}", Utils.opcodeToString(fc.getOpcode()), fc.getOwner(), fc.getName(), fc.getDescriptor());
        } else {
            logger.info("Rewriting invoke containing String-like type [{}] {}.{}{} to {}.{}{}", Utils.opcodeToString(fc.getOpcode()), fc.getOwner(), fc.getName(), fc.getDescriptor(), fc.getOwner(), fc.getName(), desc);
        }
        super.visitMethodInsn(fc.getOpcode(), fc.getOwner(), fc.getName(), desc, fc.isInterface());
    }

    private void storeArgumentsToLocals(FunctionCall call) {
        Stack<String> params = call.getParsedDescriptor().getParameterStack();

        int i = Utils.getArgumentsStackSize(call.getDescriptor());
        while (!params.isEmpty()) {
            String param = params.pop();

            i -= Type.getType(param).getSize();

            int storeOpcode = Type.getType(param).getOpcode(Opcodes.ISTORE);

            super.visitVarInsn(storeOpcode, this.used + i);
        }
        this.usedAfterInjection = Math.max(this.used + Utils.getArgumentsStackSize(call.getDescriptor()), this.usedAfterInjection);
    }

    private boolean isRelevantMethodHandleInvocation(FunctionCall fc) {
        return fc.getOwner().equals("java/lang/invoke/MethodHandle") && (
                fc.getName().equals("invoke") ||
                        fc.getName().equals("invokeExact") ||
                        fc.getName().equals("invokeWithArguments"));
    }

    private void generateVirtualOrStaticMethodHandleLookupIntercept(FunctionCall fc) {
        Label label1 = new Label();
        Label label3 = new Label();

        // Copying the class reference to the top of the Stack
        this.storeArgumentsToLocals(fc);

        int classLocal = this.used;
        int nameLocal = classLocal + 1;
        int methodTypeLocal = nameLocal + 1;
        int isJdkLocal = this.used + Utils.getArgumentsStackSize(fc.getDescriptor());
        int methodHandleLocal = isJdkLocal + 1;

        super.visitVarInsn(Opcodes.ALOAD, classLocal);
        super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASLookupUtils.class), "isJdkOrExcluded", new Descriptor(new String[]{Type.getDescriptor(Class.class)}, Type.getDescriptor(boolean.class)).toDescriptor(), false);
        super.visitVarInsn(Opcodes.ISTORE, isJdkLocal);

        super.visitVarInsn(Opcodes.ILOAD, isJdkLocal);
        super.visitJumpInsn(Opcodes.IFEQ, label1);
        {
            super.visitVarInsn(Opcodes.ALOAD, methodTypeLocal);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASLookupUtils.class), "uninstrumentForJdk", Type.getMethodDescriptor(Type.getType(MethodType.class), Type.getType(MethodType.class)), false);
            super.visitVarInsn(Opcodes.ASTORE, methodTypeLocal);
        }
        super.visitLabel(label1);

        super.visitVarInsn(Opcodes.ALOAD, classLocal);
        super.visitVarInsn(Opcodes.ALOAD, nameLocal);
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(IASString.class), "getString", Type.getMethodDescriptor(Type.getType(String.class)), false);
        super.visitVarInsn(Opcodes.ALOAD, methodTypeLocal);

        this.visitMethodInsn(fc);

        super.visitVarInsn(Opcodes.ASTORE, methodHandleLocal);

        super.visitVarInsn(Opcodes.ILOAD, isJdkLocal);
        super.visitJumpInsn(Opcodes.IFEQ, label3);
        {
            super.visitVarInsn(Opcodes.ALOAD, methodHandleLocal);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASLookupUtils.class), "convertForJdk", Type.getMethodDescriptor(Type.getType(MethodHandle.class), Type.getType(MethodHandle.class)), false);
            super.visitVarInsn(Opcodes.ASTORE, methodHandleLocal);
        }
        super.visitLabel(label3);

        super.visitVarInsn(Opcodes.ALOAD, methodHandleLocal);
        this.usedAfterInjection = Math.max(this.used + Utils.getArgumentsStackSize(fc.getDescriptor()) + 2, this.usedAfterInjection);
    }

    private void generateConstructorMethodHandleLookupIntercept(FunctionCall fc) {
        Label label = new Label();

        // Copying the class reference to the top of the Stack
        this.storeArgumentsToLocals(fc);

        int classLocal = this.used;
        int methodTypeLocal = classLocal + 1;
        int isJdkLocal = this.used + Utils.getArgumentsStackSize(fc.getDescriptor());

        super.visitVarInsn(Opcodes.ALOAD, classLocal);
        super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASLookupUtils.class), "isJdkOrExcluded", new Descriptor(new String[]{Type.getDescriptor(Class.class)}, Type.getDescriptor(boolean.class)).toDescriptor(), false);
        super.visitVarInsn(Opcodes.ISTORE, isJdkLocal);

        super.visitVarInsn(Opcodes.ILOAD, isJdkLocal);
        super.visitJumpInsn(Opcodes.IFEQ, label);
        {
            super.visitVarInsn(Opcodes.ALOAD, methodTypeLocal);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASLookupUtils.class), "uninstrumentForJdk", Type.getMethodDescriptor(Type.getType(MethodType.class), Type.getType(MethodType.class)), false);
            super.visitVarInsn(Opcodes.ASTORE, methodTypeLocal);
        }
        super.visitLabel(label);

        super.visitVarInsn(Opcodes.ALOAD, classLocal);
        super.visitVarInsn(Opcodes.ALOAD, methodTypeLocal);

        this.visitMethodInsn(fc);

        this.usedAfterInjection = Math.max(this.used + Utils.getArgumentsStackSize(fc.getDescriptor()) + 1, this.usedAfterInjection);
    }

    private void generateSpecialMethodHandleLookupIntercept(FunctionCall fc) {
        Label label1 = new Label();
        Label label3 = new Label();

        // Copying the class reference to the top of the Stack
        this.storeArgumentsToLocals(fc);

        int classLocal = this.used;
        int nameLocal = classLocal + 1;
        int methodTypeLocal = nameLocal + 1;
        int callerLocal = methodTypeLocal + 1;
        int isJdkLocal = this.used + Utils.getArgumentsStackSize(fc.getDescriptor());
        int methodHandleLocal = isJdkLocal + 1;

        super.visitVarInsn(Opcodes.ALOAD, classLocal);
        super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASLookupUtils.class), "isJdkOrExcluded", new Descriptor(new String[]{Type.getDescriptor(Class.class)}, Type.getDescriptor(boolean.class)).toDescriptor(), false);
        super.visitVarInsn(Opcodes.ISTORE, isJdkLocal);

        super.visitVarInsn(Opcodes.ILOAD, isJdkLocal);
        super.visitJumpInsn(Opcodes.IFEQ, label1);
        {
            super.visitVarInsn(Opcodes.ALOAD, methodTypeLocal);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASLookupUtils.class), "uninstrumentForJdk", Type.getMethodDescriptor(Type.getType(MethodType.class), Type.getType(MethodType.class)), false);
            super.visitVarInsn(Opcodes.ASTORE, methodTypeLocal);
        }
        super.visitLabel(label1);

        super.visitVarInsn(Opcodes.ALOAD, classLocal);
        super.visitVarInsn(Opcodes.ALOAD, nameLocal);
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(IASString.class), "getString", Type.getMethodDescriptor(Type.getType(String.class)), false);
        super.visitVarInsn(Opcodes.ALOAD, methodTypeLocal);
        super.visitVarInsn(Opcodes.ALOAD, callerLocal);

        this.visitMethodInsn(fc);

        super.visitVarInsn(Opcodes.ASTORE, methodHandleLocal);

        super.visitVarInsn(Opcodes.ILOAD, isJdkLocal);
        super.visitJumpInsn(Opcodes.IFEQ, label3);
        {
            super.visitVarInsn(Opcodes.ALOAD, methodHandleLocal);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASLookupUtils.class), "convertForJdk", Type.getMethodDescriptor(Type.getType(MethodHandle.class), Type.getType(MethodHandle.class)), false);
            super.visitVarInsn(Opcodes.ASTORE, methodHandleLocal);
        }
        super.visitLabel(label3);

        super.visitVarInsn(Opcodes.ALOAD, methodHandleLocal);
        this.usedAfterInjection = Math.max(this.used + Utils.getArgumentsStackSize(fc.getDescriptor()) + 2, this.usedAfterInjection);
    }

    private boolean isVirtualOrStaticMethodHandleLookup(FunctionCall fc) {
        return fc.getOwner().equals("java/lang/invoke/MethodHandles$Lookup") && (
                fc.getName().equals("findVirtual") ||
                        fc.getName().equals("findStatic")
        );
    }

    private boolean isSpecialMethodHandleLookup(FunctionCall fc) {
        return fc.getOwner().equals("java/lang/invoke/MethodHandles$Lookup") && fc.getName().equals("findSpecial");
    }

    private boolean isConstructorMethodHandleLookup(FunctionCall fc) {
        return fc.getOwner().equals("java/lang/invoke/MethodHandles$Lookup") && fc.getName().equals("findConstructor");
    }

    private boolean rewriteParametersAndReturnType(FunctionCall call) {
        boolean isExcluded = this.combinedExcludedLookup.isJdkOrAnnotation(call.getOwner()) || this.combinedExcludedLookup.isPackageExcluded(call.getOwner());

        if (isExcluded) {
            String desc = this.instrumentationHelper.uninstrumentForJdkCall(call.getDescriptor());
            call = new FunctionCall(call.getOpcode(), call.getOwner(), call.getName(), desc, call.isInterface());
        }

        MethodParameterTransformer transformer = new MethodParameterTransformer(this, call);

        // Add always apply transformer
        FunctionCall converter = this.config.getConverterForReturnValue(call, true);
        if (converter != null) {
            transformer.addReturnTransformation(new AlwaysApplyReturnGenericTransformer(converter));
        }
        // Always apply transformation for the parameters as well (even JDK classes)
        transformer.addParameterTransformation(new RegularParameterTransformer(call, this.instrumentationHelper, this.config));

        // Add JDK transformations
        if (isExcluded) {
            logger.info("Transforming JDK method call for [{}] {}.{}{}", Utils.opcodeToString(call.getOpcode()), call.getOwner(), call.getName(), call.getDescriptor());
            JdkMethodTransformer t = new JdkMethodTransformer(call, this.instrumentationHelper, this.config);
            transformer.addParameterTransformation(t);
            transformer.addReturnTransformation(t);
        }

        // Add Source transformations
        Source source = this.config.getSourceConfig().getSourceForFunction(call);
        if (source != null) {
            logger.info("Adding source tainting for [{}] {}.{}{}", Utils.opcodeToString(call.getOpcode()), call.getOwner(), call.getName(), call.getDescriptor());
            SourceTransformer t = new SourceTransformer(source, this.used);
            transformer.addReturnTransformation(t);
            transformer.addParameterTransformation(t);
        }

        // Add Sink transformations
        Sink sink = this.config.getSinkConfig().getSinkForFunction(call);
        if (sink != null) {
            logger.info("Adding sink checks for [{}] {}.{}{}", Utils.opcodeToString(call.getOpcode()), call.getOwner(), call.getName(), call.getDescriptor());
            SinkTransformer t = new SinkTransformer(sink, this.instrumentationHelper, this.used);
            transformer.addParameterTransformation(t);
            transformer.addReturnTransformation(t);
        }

        // No transformations required
        if (!transformer.needsTransformation()) {
            return false;
        }

        // Do the transformations
        transformer.modifyStackParameters(this.used);
        this.usedAfterInjection = Math.max(this.used + transformer.getExtraStackSlots(), this.usedAfterInjection);

        // Instrument descriptor if source/sink is not a JDK class
        if (!isExcluded) {
            String desc = this.instrumentationHelper.instrumentForNormalCall(call.getDescriptor());
            call = new FunctionCall(call.getOpcode(), call.getOwner(), call.getName(), desc, call.isInterface());
        }
        this.visitMethodInsn(call);

        // Modify Return parameters
        transformer.modifyReturnType();

        logger.info("Finished transforming parameters for [{}] {}.{}{}", Utils.opcodeToString(call.getOpcode()), call.getOwner(), call.getName(), call.getDescriptor());
        return true;
    }

    /**
     * The 'ldc' instruction loads a constant value out of the constant pool.
     * <p>
     * It might load String values, so we have to transform them.
     */
    @Override
    public void visitLdcInsn(Object value) {

        if (this.instrumentationHelper.handleLdc(this.mv, value)) {
            return;
        }

        if (value instanceof Type) {
            Type type = (Type) value;
            int sort = type.getSort();
            if (sort == Type.OBJECT) {
                if (this.instrumentationHelper.handleLdcType(this.mv, type)) {
                    return;
                }
                //TODO: handle Arrays etc..
            } else if (sort == Type.ARRAY) {
                if (this.instrumentationHelper.handleLdcArray(this.mv, type)) {
                    return;
                }
            }
        }
        super.visitLdcInsn(value);
    }


    /**
     * We want to override some instantiations of classes with our own types
     */
    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        // TODO All instrumented classes not only strings
        if (/*this.shouldRewriteCheckCast &&*/ opcode == Opcodes.CHECKCAST && Constants.StringQN.equals(type)) {
            logger.info("Rewriting checkcast to call to TString.fromObject(Object obj)");
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASStringUtils.class), "fromObject", String.format("(%s)%s", Constants.ObjectDesc, Type.getDescriptor(IASString.class)), false);
            super.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(IASString.class));
            return;
        }
        logger.info("Visiting type [{}] instruction: {}", type, opcode);
        String newType = this.instrumentationHelper.rewriteTypeIns(type);
        super.visitTypeInsn(opcode, newType);
    }

    @Override
    public void visitInvokeDynamicInsn(
            final String name,
            final String descriptor,
            final Handle bootstrapMethodHandle,
            final Object... bootstrapMethodArguments) {

        if (this.shouldBeDynProxied(name, descriptor)) {
            return;
        }

        if ("java/lang/invoke/LambdaMetafactory".equals(bootstrapMethodHandle.getOwner()) &&
                ("metafactory".equals(bootstrapMethodHandle.getName()) || "altMetafactory".equals(bootstrapMethodHandle.getName()))) {
            Handle realFunction = (Handle) bootstrapMethodArguments[1];
            Type desc = Type.getType(descriptor);

            LambdaCall call = new LambdaCall(Type.getMethodType(descriptor).getReturnType(), realFunction, desc);
            if (call.isInstanceCall()) {
                call.setConcreteImplementationType(desc.getArgumentTypes().length == 1 ? desc.getArgumentTypes()[0] : null);
            }

            MethodTaintingUtils.invokeVisitLambdaCall(this.getParentVisitor(), this.instrumentationHelper, call.getProxyDescriptor(this.loader, this.instrumentationHelper), call, this.owner, name, descriptor, isOwnerInterface, bootstrapMethodHandle, bootstrapMethodArguments);

            if (MethodTaintingUtils.needsLambdaProxy(descriptor, realFunction, (Type) bootstrapMethodArguments[2], this.instrumentationHelper)) {
                this.jdkLambdaMethodProxies.add(call);
            }
        } else if ("makeConcatWithConstants".equals(name)) {
            this.rewriteConcatWithConstants(name, descriptor, bootstrapMethodArguments);
        } else {
            String desc = this.instrumentationHelper.instrumentForNormalCall(descriptor);

            String instrumentedBootstrapDesc = this.instrumentationHelper.instrumentForNormalCall(bootstrapMethodHandle.getDesc());

            Handle instrumentedOriginalHandle = new Handle(bootstrapMethodHandle.getTag(), bootstrapMethodHandle.getOwner(), bootstrapMethodHandle.getName(), instrumentedBootstrapDesc, bootstrapMethodHandle.isInterface());

            Handle proxyHandle = new Handle(bootstrapMethodHandle.getTag(), this.owner, "$fontus$" + bootstrapMethodHandle.getName() + bootstrapMethodHandle.hashCode(), bootstrapMethodHandle.getDesc(), bootstrapMethodHandle.isInterface());

            DynamicCall dynamicCall = new DynamicCall(instrumentedOriginalHandle, proxyHandle, bootstrapMethodArguments);

            this.bootstrapMethods.add(dynamicCall);

            logger.info("invokeDynamic {}{}", name, descriptor);
            super.visitInvokeDynamicInsn(name, desc, proxyHandle, bootstrapMethodArguments);
        }
    }

    private void rewriteConcatWithConstants(String name, String descriptor, Object[] bootstrapMethodArguments) {
        logger.info("Trying to rewrite invokeDynamic {}{} towards Concat!", name, descriptor);

        Descriptor desc = Descriptor.parseDescriptor(descriptor);
        assert bootstrapMethodArguments.length == 1;
        Object fmtStringObj = bootstrapMethodArguments[0];
        assert fmtStringObj instanceof String;
        String formatString = (String) fmtStringObj;
        int parameterCount = desc.parameterCount();
        MethodTaintingUtils.pushNumberOnTheStack(this.getParentVisitor(), parameterCount);
        super.visitTypeInsn(Opcodes.ANEWARRAY, Constants.ObjectQN);
        int currRegister = this.used;
        super.visitVarInsn(Opcodes.ASTORE, currRegister);
        // newly created array is now stored in currRegister, concat operands on top
        Stack<String> parameters = desc.getParameterStack();
        int paramIndex = 0;
        while (!parameters.empty()) {
            String parameter = parameters.pop();
            // Convert topmost value (if required)
            MethodTaintingUtils.invokeConversionFunction(this.getParentVisitor(), parameter);
            // put array back on top
            super.visitVarInsn(Opcodes.ALOAD, currRegister);
            // swap array and object to array
            super.visitInsn(Opcodes.SWAP);
            // push the index where the value shall be stored
            MethodTaintingUtils.pushNumberOnTheStack(this.getParentVisitor(), paramIndex);
            // swap, this puts them into the order arrayref, index, value
            super.visitInsn(Opcodes.SWAP);
            // store the value into arrayref at index, next parameter is on top now (if there are any more)
            super.visitInsn(Opcodes.AASTORE);
            paramIndex++;
        }

        // Load the format String constant
        super.visitLdcInsn(formatString);
        // Load the param array
        super.visitVarInsn(Opcodes.ALOAD, currRegister);
        // Call our concat method
        super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASStringUtils.class), "concat", Constants.CONCAT_DESC, false);
    }

    /**
     * Is there a dynamic proxy defined? If so apply and return true.
     */
    private boolean shouldBeDynProxied(String name, String descriptor) {
        ProxiedDynamicFunctionEntry pdfe = new ProxiedDynamicFunctionEntry(name, descriptor);
        if (this.dynProxies.containsKey(pdfe)) {
            logger.info("Proxying dynamic call to {}{}", name, descriptor);
            Runnable pf = this.dynProxies.get(pdfe);
            pf.run();
            return true;
        }
        return false;
    }

    /**
     * Is there a proxy defined? If so apply and return true.
     */
    private FunctionCall shouldBeProxied(FunctionCall pfe) {
        if (this.methodProxies.containsKey(pfe)) {
            logger.info("Proxying call to {}.{}{}", pfe.getOwner(), pfe.getName(), pfe.getDescriptor());
            FunctionCall pf = this.methodProxies.get(pfe);
            return pf;
        }
        if (pfe.getOpcode() == Opcodes.INVOKEVIRTUAL || pfe.getOpcode() == Opcodes.INVOKEINTERFACE) {
            if (this.combinedExcludedLookup.isJdkClass(pfe.getOwner())) {
                for (FunctionCall mip : this.methodInterfaceProxies.keySet()) {
                    if (pfe.getName().equals(mip.getName()) && pfe.getDescriptor().equals(mip.getDescriptor())) {
                        if (thisOrSuperQNEquals(pfe.getOwner(), mip.getOwner())) {
                            logger.info("Proxying interface call to {}.{}{}", pfe.getOwner(), pfe.getName(), pfe.getDescriptor());
                            FunctionCall pf = this.methodInterfaceProxies.get(mip);
                            return pf;
                        }
                    }
                }

            }
        }
        return pfe;
    }

    private boolean thisOrSuperQNEquals(String thisQn, final String requiredQn) {
        if (thisQn.equals(requiredQn)) {
            return true;
        }
        try {
            for (Class<?> cls = Class.forName(Utils.slashToDot(thisQn)); cls.getSuperclass() != null; cls = cls.getSuperclass()) {
                for (Class<?> interf : cls.getInterfaces()) {
                    if (Utils.dotToSlash(interf.getName()).equals(requiredQn)) {
                        return true;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void visitCode() {
        super.visitCode();
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
        super.visitMethodInsn(opcode, owner, name, descriptor);
    }


    @Override
    public void visitJumpInsn(int opcode, Label label) {
        if (opcode == Opcodes.IF_ACMPEQ) {
            // Returns 1
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.CompareProxyQN, Constants.CompareProxyEqualsName, Constants.CompareProxyEqualsDesc, false);
            // Expects something different from 0
            super.visitJumpInsn(Opcodes.IFNE, label);
        } else if (opcode == Opcodes.IF_ACMPNE) {
            // Returns 0
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.CompareProxyQN, Constants.CompareProxyEqualsName, Constants.CompareProxyEqualsDesc, false);
            // Expects 0
            super.visitJumpInsn(Opcodes.IFEQ, label);
        } else {
            super.visitJumpInsn(opcode, label);
        }
    }


    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        super.visitIincInsn(var, increment);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        String instrumented = this.instrumentationHelper.instrumentQN(descriptor);
        super.visitMultiANewArrayInsn(instrumented, numDimensions);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}

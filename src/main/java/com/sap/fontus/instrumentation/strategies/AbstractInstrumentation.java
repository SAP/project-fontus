package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.TriConsumer;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.Utils;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.objectweb.asm.*;

import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sap.fontus.asm.Descriptor.replaceSuffix;

public class AbstractInstrumentation implements InstrumentationStrategy {
    protected static final Logger logger = LogUtils.getLogger();
    protected final CombinedExcludedLookup combinedExcludedLookup = new CombinedExcludedLookup();
    protected final Pattern qnMatcher;
    protected final Pattern qnInstrumentedMatcher;
    protected final Type origType;
    protected final Type instrumentedType;
    protected final Pattern descPattern;
    protected final InstrumentationHelper instrumentationHelper;
    protected final HashMap<String, String> methodsToRename = new HashMap<>(1);
    protected final String taintedToOrig;

    public AbstractInstrumentation(Type origType, Type instrumentedType, InstrumentationHelper instrumentationHelper, String taintedToOrig) {
        this.origType = origType;
        this.instrumentedType = instrumentedType;
        this.qnMatcher = Pattern.compile(this.origType.getInternalName(), Pattern.LITERAL);
        this.qnInstrumentedMatcher = Pattern.compile(this.instrumentedType.getInternalName(), Pattern.LITERAL);
        this.descPattern = Pattern.compile(this.origType.getDescriptor());
        this.instrumentationHelper = instrumentationHelper;
        this.taintedToOrig = taintedToOrig;
        this.methodsToRename.put(Constants.ToString, Constants.TO_TSTRING);
    }

    @Override
    public Descriptor instrument(Descriptor desc) {
        return desc.replaceType(this.origType.getDescriptor(), this.instrumentedType.getDescriptor());
    }

    @Override
    public String uninstrument(String typeDescriptor) {
        return replaceSuffix(typeDescriptor, this.instrumentedType.getDescriptor(), this.origType.getDescriptor());
    }

    @Override
    public Descriptor uninstrumentForJdkCall(Descriptor descriptor) {
        return descriptor.replaceType(this.instrumentedType.getDescriptor(), this.origType.getDescriptor());
    }

    @Override
    public String instrumentQN(String qn) {
        return this.qnMatcher.matcher(qn).replaceAll(Matcher.quoteReplacement(this.instrumentedType.getInternalName()));
    }

    @Override
    public String uninstrumentQN(String qn) {
        return this.qnInstrumentedMatcher.matcher(qn).replaceAll(Matcher.quoteReplacement(this.origType.getInternalName()));
    }

    @Override
    public Optional<String> translateClassName(String className) {
        if (className.equals(Utils.slashToDot(this.origType.getInternalName()))) {
            return Optional.of(Utils.slashToDot(this.instrumentedType.getInternalName()));
        }
        return Optional.empty();
    }

    @Override
    public boolean handlesType(String descriptor) {
        return descriptor.endsWith(this.origType.getDescriptor());
    }

    @Override
    public boolean isInstrumented(String descriptor) {
        return descriptor.endsWith(this.instrumentedType.getDescriptor());
    }

    @Override
    public FunctionCall rewriteOwnerMethod(FunctionCall functionCall) {
        boolean isInstrumentable = Type.getObjectType(functionCall.getOwner()).equals(this.origType);
        //boolean isArrayInstrumentable = Type.getObjectType(functionCall.getOwner()).equals(this.getOrigArrayType());
        boolean isArrayInstrumentable = Type.getObjectType(functionCall.getOwner()).getSort() == Type.ARRAY && Type.getObjectType(functionCall.getOwner()).getElementType().equals(this.origType);
        if (isInstrumentable || isArrayInstrumentable) {
            Descriptor newDescriptor = this.instrumentationHelper.instrument(functionCall.getParsedDescriptor());
            String newOwner = isArrayInstrumentable ? this.getInstrumentedArrayType(Type.getObjectType(functionCall.getOwner()).getDimensions()).getInternalName() : this.instrumentedType.getInternalName();
            // Some methods names (e.g., toString) need to be replaced to not break things, look those up
            String newName = this.methodsToRename.getOrDefault(functionCall.getName(), functionCall.getName());

            logger.info("Rewriting {} invoke [{}] {}.{}{} to {}.{}{}", this.origType.getInternalName(), Utils.opcodeToString(functionCall.getOpcode()), functionCall.getOwner(), functionCall.getName(), functionCall.getName(), newOwner, newName, newDescriptor);
            return new FunctionCall(functionCall.getOpcode(), newOwner, newName, newDescriptor.toDescriptor(), functionCall.isInterface());
        }
        return null;
    }

    @Override
    public void instrumentStackTop(MethodVisitor mv, Type origType) {
        if (this.origType.equals(origType)) {
            this.origToTainted(mv);
        } else if (this.getOrigArrayType().equals(origType)) {
            this.arrayOrigToTainted(mv);
        }
    }

    private void arrayOrigToTainted(MethodVisitor mv) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ConversionUtilsQN, Constants.ConversionUtilsToConcreteName, Constants.ConversionUtilsToConcreteDesc, false);
        mv.visitTypeInsn(Opcodes.CHECKCAST, this.getInstrumentedArrayType(1).getInternalName());
    }

    protected void origToTainted(MethodVisitor mv) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ConversionUtilsQN, Constants.ConversionUtilsToConcreteName, Constants.ConversionUtilsToConcreteDesc, false);
        mv.visitTypeInsn(Opcodes.CHECKCAST, this.instrumentedType.getInternalName());
    }

    @Override
    public boolean handleLdc(MethodVisitor mv, Object value) {
        return false;
    }

    @Override
    public boolean handleLdcType(MethodVisitor mv, Type type) {
        if (this.origType.equals(type)) {
            mv.visitLdcInsn(Type.getObjectType(this.instrumentedType.getInternalName()));
            return true;
        }
        return false;
    }

    @Override
    public boolean handleLdcArray(MethodVisitor mv, Type type) {
        return false;
    }

    @Override
    public String rewriteTypeIns(String type) {
        boolean isArray = type.startsWith("[");
        if (Type.getObjectType(type).equals(this.origType) || (isArray && type.endsWith(this.origType.getDescriptor()))) {
            return this.instrumentQN(type);
        }
        return type;
    }

    @Override
    public Optional<FieldVisitor> instrumentFieldInstruction(ClassVisitor classVisitor, int access, String name, String descriptor, String signature, Object value, TriConsumer tc) {
        Matcher descMatcher = this.descPattern.matcher(descriptor);
        if (descMatcher.find()) {
            String newDescriptor = descMatcher.replaceAll(this.instrumentedType.getDescriptor());
            logger.info("Replacing {} field [{}]{}.{} with [{}]{}.{}", this.origType.getInternalName(), access, name, descriptor, access, name, newDescriptor);
            return Optional.of(classVisitor.visitField(access, name, newDescriptor, signature, value));
        }
        return Optional.empty();
    }


    @Override
    public boolean instrumentFieldIns(MethodVisitor mv, int opcode, String owner, String name, String descriptor) {
        String newOwner = owner;
        if (this.origType.equals(Type.getObjectType(owner))) {
            newOwner = this.instrumentedType.getInternalName();
        }
        Matcher matcher = Pattern.compile(this.origType.getDescriptor()).matcher(descriptor);
        if (matcher.find()) {
            if (this.combinedExcludedLookup.isPackageExcludedOrJdk(newOwner)) {
                mv.visitFieldInsn(opcode, newOwner, name, descriptor);
                this.origToTainted(mv);
            } else {
                String newDescriptor = matcher.replaceAll(this.instrumentedType.getDescriptor());
                mv.visitFieldInsn(opcode, newOwner, name, newDescriptor);
            }
            return true;
        }
        if (!owner.equals(newOwner)) {
            mv.visitFieldInsn(opcode, newOwner, name, descriptor);
            return true;
        }
        return false;
    }

    @Override
    public boolean insertJdkMethodParameterConversion(MethodVisitor mv, Type parameter) {
        if (this.origType.equals(parameter)) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ConversionUtilsQN, Constants.ConversionUtilsToOrigName, Constants.ConversionUtilsToOrigDesc, false);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.origType.getInternalName());
            return true;
        }
        if (this.getOrigArrayType().equals(parameter)) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ConversionUtilsQN, Constants.ConversionUtilsToOrigName, Constants.ConversionUtilsToOrigDesc, false);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.getOrigArrayType().getInternalName());
            return true;
        }
        return false;
    }

    @Override
    public String instrumentSuperClass(String superClass) {
        return superClass;
    }

    protected Type getOrigArrayType() {
        return AbstractInstrumentation.getArrayType(this.origType, this.origType.getDimensions());
    }

    private static Type getArrayType(Type type, int dimensions) {
        return Type.getType("[".repeat(dimensions) + type);
    }

    protected Type getInstrumentedArrayType(int dimensions) {
        return AbstractInstrumentation.getArrayType(this.instrumentedType, dimensions);
    }
}

package com.sap.fontus.instrumentation.strategies;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sap.fontus.Constants;
import com.sap.fontus.TriConsumer;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.instrumentation.TaintingUtils;
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
    protected final Pattern qnMatcherWithDotsInsteadOfSlashes;
    protected final Type origType;
    protected final Type instrumentedType;
    protected final Pattern descPattern;
    protected final InstrumentationHelper instrumentationHelper;
    protected final HashMap<String, String> methodsToRename = new HashMap<>(1);
    protected final String taintedToOrig;
    protected final String instrumentedTypeNameWithDots;

    public AbstractInstrumentation(Type origType, Type instrumentedType, InstrumentationHelper instrumentationHelper, String taintedToOrig) {
        this.origType = origType;
        this.instrumentedType = instrumentedType;
        this.qnMatcher = literalPatternCache.get(this.origType.getInternalName());
        this.qnInstrumentedMatcher = literalPatternCache.get(this.instrumentedType.getInternalName());
        // This one is to match against the public class name with dots, e.g. "java.lang.String"
        this.qnMatcherWithDotsInsteadOfSlashes = literalPatternCache.get(Utils.slashToDot(this.origType.getInternalName()));
        this.descPattern = patternCache.get(this.origType.getDescriptor());
        this.instrumentationHelper = instrumentationHelper;
        this.taintedToOrig = taintedToOrig;
        this.methodsToRename.put(Constants.ToString, Constants.TO_TSTRING);
        this.instrumentedTypeNameWithDots = Utils.slashToDot(this.instrumentedType.getInternalName());
    }

    @Override
    public Descriptor instrument(Descriptor desc) {
        return desc.replaceType(this.origType.getDescriptor(), this.instrumentedType.getDescriptor());
    }

    @Override
    public String instrument(String typeDescriptor) {
        return replaceSuffix(typeDescriptor, this.origType.getDescriptor(), this.instrumentedType.getDescriptor());
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
        String classNameWithSlashes = Utils.dotToSlash(className);
        boolean isInstrumentable = Type.getObjectType(classNameWithSlashes).equals(this.origType);
        boolean isArrayInstrumentable = Type.getObjectType(classNameWithSlashes).getSort() == Type.ARRAY && Type.getObjectType(classNameWithSlashes).getElementType().equals(this.origType);
        if (isInstrumentable || isArrayInstrumentable) {
            Matcher m = this.qnMatcherWithDotsInsteadOfSlashes.matcher(className);
            return Optional.of(m.replaceAll(Matcher.quoteReplacement(this.instrumentedTypeNameWithDots)));
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
    public Type instrumentStackTop(MethodVisitor mv, Type origType) {
        if (this.origType.equals(origType)) {
            return this.origToTainted(mv);
        } else if (this.getOrigArrayType().equals(origType)) {
            return this.arrayOrigToTainted(mv);
        }
        return null;
    }

    private Type arrayOrigToTainted(MethodVisitor mv) {
        Type retType = this.getInstrumentedArrayType(1);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ConversionUtilsQN, Constants.ConversionUtilsToConcreteName, Constants.ConversionUtilsToConcreteDesc, false);
        mv.visitTypeInsn(Opcodes.CHECKCAST, retType.getInternalName());
        return retType;
    }

    protected Type origToTainted(MethodVisitor mv) {
        return TaintingUtils.convertTypeToTainted(this.origType, this.instrumentedType, mv);
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
        Matcher matcher = patternCache.get(this.origType.getDescriptor()).matcher(descriptor);
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
            TaintingUtils.convertTypeToUntainted(this.instrumentedType, this.origType, mv);
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
    public boolean insertJdkMethodParameterConversion(MethodVisitor mv, Type source, Type parameter) {
        if(this.instrumentedType.equals(source) && this.origType.equals(parameter)) {
            TaintingUtils.convertTypeToUntainted(this.instrumentedType, this.origType, mv);
            return true;
        }
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
    public boolean needsJdkMethodParameterConversion(Type parameter) {
        return (this.origType.equals(parameter)) || (this.getOrigArrayType().equals(parameter));
    }

    @Override
    public String instrumentSuperClass(String superClass) {
        return superClass;
    }

    protected Type getOrigArrayType() {
        return getArrayType(this.origType, this.origType.getDimensions());
    }

    private static Type getArrayType(Type type, int dimensions) {
        return Type.getType("[".repeat(dimensions) + type);
    }

    protected Type getInstrumentedArrayType(int dimensions) {
        return getArrayType(this.instrumentedType, dimensions);
    }

    private static final LoadingCache<String, Pattern> patternCache = Caffeine.newBuilder().build(Pattern::compile);
    private static final LoadingCache<String, Pattern> literalPatternCache = Caffeine.newBuilder().build((str) -> Pattern.compile(str, Pattern.LITERAL));
}

package com.sap.fontus.instrumentation;

import com.sap.fontus.Constants;
import com.sap.fontus.TriConsumer;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.instrumentation.strategies.*;
import com.sap.fontus.taintaware.unified.IASMatchResult;
import com.sap.fontus.taintaware.unified.IASStringJoiner;
import com.sap.fontus.taintaware.unified.reflect.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.MatchResult;

public final class InstrumentationHelper implements InstrumentationStrategy {
    private final Collection<InstrumentationStrategy> strategies = new ArrayList<>(20);

    public InstrumentationHelper() {
        this.strategies.add(new FormatterInstrumentation(this));
        this.strategies.add(new MatcherInstrumentation(this));
        this.strategies.add(new PatternInstrumentation(this));
        this.strategies.add(new AbstractInstrumentation(Type.getType(MatchResult.class), Type.getType(IASMatchResult.class), this, "toMatchResult"));
        this.strategies.add(new StringInstrumentation(this));
        this.strategies.add(new StringBuilderInstrumentation(this));
        this.strategies.add(new StringBufferInstrumentation(this));
        this.strategies.add(new AbstractInstrumentation(Type.getType(StringJoiner.class), Type.getType(IASStringJoiner.class), this, Constants.TJoinerToJoiner));
        this.strategies.add(new PropertiesStrategy(this));
        this.strategies.add(new ProxyInstrumentation(this));
        this.strategies.add(new AbstractInstrumentation(Type.getType(AccessibleObject.class), Type.getType(IASAccessibleObject.class), this, Constants.TAccessibleObjectToAccesibleObject));
        this.strategies.add(new AbstractInstrumentation(Type.getType(Executable.class), Type.getType(IASExecutable.class), this, Constants.TExecutableToExecutable));
        this.strategies.add(new AbstractInstrumentation(Type.getType(Parameter.class), Type.getType(IASParameter.class), this, Constants.TParameterToParameter));
        this.strategies.add(new AbstractInstrumentation(Type.getType(Constructor.class), Type.getType(IASConstructor.class), this, Constants.TConstructorToConstructor));
        this.strategies.add(new AbstractInstrumentation(Type.getType(Method.class), Type.getType(IASMethod.class), this, Constants.TMethodToMethodName));
        this.strategies.add(new AbstractInstrumentation(Type.getType(Field.class), Type.getType(IASField.class), this, Constants.TFieldToField));
        this.strategies.add(new AbstractInstrumentation(Type.getType(Member.class), Type.getType(IASMember.class), this, Constants.TMemberToMember));
        this.strategies.add(new ObjectInputStreamStrategy(this));
        this.strategies.add(new DefaultInstrumentation());
    }

    @Override
    public String instrumentQN(String qn) {
        String newQN = qn;
        for (InstrumentationStrategy is : this.strategies) {
            newQN = is.instrumentQN(newQN);
            if (!qn.equals(newQN)) {
                break;
            }
        }
        return newQN;
    }

    @Override
    public String uninstrumentQN(String qn) {
        String newQN = qn;
        for (InstrumentationStrategy is : this.strategies) {
            newQN = is.uninstrumentQN(newQN);
            if (!qn.equals(newQN)) {
                break;
            }
        }
        return newQN;
    }

    /**
     * This instruments the descriptors for normal application classes (uses the actual taintaware classes (e.g. IASString))
     */
    public String instrumentForNormalCall(String desc) {
        return this.instrument(Descriptor.parseDescriptor(desc)).toDescriptor();
    }

    /**
     * This instruments the descriptors for normal application classes (uses the actual taintaware classes (e.g. IASString))
     */
    @Override
    public Descriptor instrument(Descriptor desc) {
        Descriptor newDesc = desc;
        for (InstrumentationStrategy is : this.strategies) {
            newDesc = is.instrument(newDesc);
        }
        return newDesc;
    }

    public String uninstrumentForJdkCall(String desc) {
        return this.uninstrumentForJdkCall(Descriptor.parseDescriptor(desc)).toDescriptor();
    }

    /**
     * This uninstruments the descriptors for jdk/excluded calls
     */
    @Override
    public Descriptor uninstrumentForJdkCall(Descriptor desc) {
        Descriptor newDesc = desc;
        for (InstrumentationStrategy is : this.strategies) {
            newDesc = is.uninstrumentForJdkCall(newDesc);
        }
        return newDesc;
    }

    @Override
    public String instrument(String typeDescriptor) {
        String newDesc = typeDescriptor;
        for (InstrumentationStrategy is : this.strategies) {
            newDesc = is.instrument(newDesc);
        }
        return newDesc;
    }

    @Override
    public String uninstrument(String typeDescriptor) {
        String newDesc = typeDescriptor;
        for (InstrumentationStrategy is : this.strategies) {
            newDesc = is.uninstrument(newDesc);
        }
        return newDesc;
    }

//    @Override
//    public String instrumentDescForIASCall(String desc) {
//        String newDesc = desc;
//        for (InstrumentationStrategy is : this.strategies) {
//            newDesc = is.instrumentDescForIASCall(newDesc);
//        }
//        return newDesc;
//    }

    @Override
    public Optional<String> translateClassName(String clazzName) {
        for (InstrumentationStrategy is : this.strategies) {
            Optional<String> os = is.translateClassName(clazzName);
            if (os.isPresent()) {
                return os;
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean handlesType(String descriptor) {
        for (InstrumentationStrategy s : this.strategies) {
            if (s.handlesType(descriptor)) {
                return true;
            }
        }
        return false;
    }

    public boolean canHandleType(String typeDescriptor) {
        for (InstrumentationStrategy is : this.strategies) {
            if (is.handlesType(typeDescriptor)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isInstrumented(String descriptor) {
        for (InstrumentationStrategy is : this.strategies) {
            if (is.isInstrumented(descriptor)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<FieldVisitor> instrumentFieldInstruction(ClassVisitor classVisitor, int access, String name, String descriptor, String signature, Object value, TriConsumer tc) {
        for (InstrumentationStrategy is : this.strategies) {
            Optional<FieldVisitor> ofv = is.instrumentFieldInstruction(classVisitor, access, name, descriptor, signature, value, tc);
            if (ofv.isPresent()) {
                return ofv;
            }
        }
        return Optional.empty();
    }

    @Override
    public String instrumentSuperClass(String superClass) {
        String instrumentedSuperClass = superClass;
        for (InstrumentationStrategy s : this.strategies) {
            instrumentedSuperClass = s.instrumentSuperClass(instrumentedSuperClass);
        }
        return instrumentedSuperClass;
    }

    @Override
    public boolean instrumentFieldIns(MethodVisitor mv, int opcode, String owner, String name, String descriptor) {
        for (InstrumentationStrategy s : this.strategies) {
            if (s.instrumentFieldIns(mv, opcode, owner, name, descriptor)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean insertJdkMethodParameterConversion(MethodVisitor mv, Type parameter) {
        for (InstrumentationStrategy s : this.strategies) {
            if (s.insertJdkMethodParameterConversion(mv, parameter)) {
                return true;
            }
        }
        return false;
    }

    public boolean insertJdkMethodParameterConversion(MethodVisitor mv, Type source, Type parameter) {
        for (InstrumentationStrategy s : this.strategies) {
            if (s.insertJdkMethodParameterConversion(mv, source, parameter)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean needsJdkMethodParameterConversion(Type parameter) {
        for (InstrumentationStrategy s : this.strategies) {
            if (s.needsJdkMethodParameterConversion(parameter)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public FunctionCall rewriteOwnerMethod(FunctionCall functionCall) {
        for (InstrumentationStrategy s : this.strategies) {
            FunctionCall rewrittenFC = s.rewriteOwnerMethod(functionCall);
            if (rewrittenFC != null) {
                return rewrittenFC;
            }
        }
        return null;
    }

    @Override
    public Type instrumentStackTop(MethodVisitor mv, Type origType) {
        Type returnType = origType;
        for (InstrumentationStrategy strategy : this.strategies) {
            Type r = strategy.instrumentStackTop(mv, origType);
            if(r != null) {
                returnType = r;
            }
        }
        return returnType;
    }

    @Override
    public boolean handleLdc(MethodVisitor mv, Object value) {
        for (InstrumentationStrategy s : this.strategies) {
            if (s.handleLdc(mv, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleLdcType(MethodVisitor mv, Type type) {
        for (InstrumentationStrategy s : this.strategies) {
            if (s.handleLdcType(mv, type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleLdcArray(MethodVisitor mv, Type type) {
        for (InstrumentationStrategy s : this.strategies) {
            if (s.handleLdcArray(mv, type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String rewriteTypeIns(String type) {
        String newType = type;
        for (InstrumentationStrategy s : this.strategies) {
            newType = s.rewriteTypeIns(newType);
        }
        return newType;
    }
}

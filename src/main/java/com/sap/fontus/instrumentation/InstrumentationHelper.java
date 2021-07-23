package com.sap.fontus.instrumentation;

import com.sap.fontus.TriConsumer;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.instrumentation.strategies.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public final class InstrumentationHelper implements InstrumentationStrategy {
    private final Collection<InstrumentationStrategy> strategies = new ArrayList<>(10);

    public InstrumentationHelper(TaintStringConfig configuration) {
        this.strategies.add(new FormatterInstrumentation(configuration, this));
        this.strategies.add(new MatcherInstrumentation(configuration, this));
        this.strategies.add(new PatternInstrumentation(configuration, this));
        this.strategies.add(new StringInstrumentation(configuration, this));
        this.strategies.add(new StringBuilderInstrumentation(configuration, this));
        this.strategies.add(new StringBufferInstrumentation(configuration, this));
        this.strategies.add(new PropertiesStrategy(configuration, this));
        this.strategies.add(new ProxyInstrumentation(this));
        this.strategies.add(new DefaultInstrumentation(configuration));
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


    /**
     * This instruments the descriptors for normal application classes (uses the actual taintaware classes (e.g. IASString))
     */
    public String instrumentForNormalCall(String desc) {
        return this.instrumentForNormalCall(Descriptor.parseDescriptor(desc)).toDescriptor();
    }

    /**
     * This instruments the descriptors for normal application classes (uses the actual taintaware classes (e.g. IASString))
     */
    @Override
    public Descriptor instrumentForNormalCall(Descriptor desc) {
        Descriptor newDesc = desc;
        for (InstrumentationStrategy is : this.strategies) {
            newDesc = is.instrumentForNormalCall(newDesc);
        }
        return newDesc;
    }

    @Override
    public String uninstrumentNormalCall(String typeDescriptor) {
        String newDesc = typeDescriptor;
        for (InstrumentationStrategy is : this.strategies) {
            newDesc = is.uninstrumentNormalCall(newDesc);
        }
        return newDesc;
    }

    public Descriptor uninstrumentNormalCall(Descriptor typeDescriptor) {
        return Descriptor.parseDescriptor(this.uninstrumentNormalCall(typeDescriptor.toDescriptor()));
    }

    @Override
    public String instrumentDescForIASCall(String desc) {
        String newDesc = desc;
        for (InstrumentationStrategy is : this.strategies) {
            newDesc = is.instrumentDescForIASCall(newDesc);
        }
        return newDesc;
    }

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
    public void instrumentStackTop(MethodVisitor mv, Type origType) {
        for (InstrumentationStrategy strategy : this.strategies) {
            strategy.instrumentStackTop(mv, origType);
        }
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

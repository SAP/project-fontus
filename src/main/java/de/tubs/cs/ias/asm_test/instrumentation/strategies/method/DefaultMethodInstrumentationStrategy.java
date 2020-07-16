package de.tubs.cs.ias.asm_test.instrumentation.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.utils.LogUtils;
import de.tubs.cs.ias.asm_test.utils.Utils;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.DefaultInstrumentation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import de.tubs.cs.ias.asm_test.utils.Logger;

import static de.tubs.cs.ias.asm_test.utils.MethodUtils.isToString;

public class DefaultMethodInstrumentationStrategy extends DefaultInstrumentation implements MethodInstrumentationStrategy {
    private final MethodVisitor mv;
    private static final Logger logger = LogUtils.getLogger();
    private static final Set<Type> requireValueOf = fillRequireValueOfSet();

    private static Set<Type> fillRequireValueOfSet() {
        Set<Type> set = new HashSet<>();
        set.add(Type.getType(CharSequence.class));
        set.add(Type.getType(Object.class));
        set.add(Type.getType(Serializable.class));
        set.add(Type.getType(Appendable.class));
        return set;
    }

    public DefaultMethodInstrumentationStrategy(MethodVisitor mv, TaintStringConfig configuration) {
        super(configuration);
        this.mv = mv;
    }

    @Override
    public boolean instrumentFieldIns(int opcode, String owner, String name, String descriptor) {
        this.mv.visitFieldInsn(opcode, owner, name, descriptor);
        return true;
    }

    @Override
    public void insertJdkMethodParameterConversion(String parameter) {
    }

    @Override
    public boolean rewriteOwnerMethod(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        Type tOwner = Type.getObjectType(owner);
        if(isToString(name, descriptor) && requireValueOf.contains(tOwner)) {
            int newOpcode = Opcodes.INVOKESTATIC;
            String newOwner = this.stringConfig.getTStringQN();
            String newDescriptor = "(" + Constants.ObjectDesc + ")" + this.stringConfig.getTStringDesc();
            String newName = Constants.VALUE_OF;
            boolean newIsInterface = false;
            logger.info("Rewriting toString invoke [{}] {}.{}{} to valueOf call {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, newOwner, newName, newDescriptor);
            this.mv.visitMethodInsn(newOpcode, newOwner, newName, newDescriptor, false);
            return true;
        }
        return false;
    }

    @Override
    public void instrumentReturnType(String owner, String name, Descriptor desc) {
    }

    @Override
    public boolean handleLdc(Object value) {
        return false;
    }

    @Override
    public boolean handleLdcType(Type type) {
        return false;
    }

    @Override
    public boolean handleLdcArray(Type type) {
        return false;
    }

    @Override
    public String rewriteTypeIns(String type) {
        return type;
    }
}

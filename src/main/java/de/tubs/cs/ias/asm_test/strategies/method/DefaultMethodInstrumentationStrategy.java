package de.tubs.cs.ias.asm_test.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.Utils;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.strategies.DefaultInstrumentation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Set;

public class DefaultMethodInstrumentationStrategy extends DefaultInstrumentation implements MethodInstrumentationStrategy {
    private final MethodVisitor mv;
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
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
            String newName = "valueOf";
            boolean newIsInterface = false;
            logger.info("Rewriting String invoke [{}] {}.{}{} to {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, newOwner, newName, newDescriptor);
            this.mv.visitMethodInsn(newOpcode, newOwner, newName, newDescriptor, newIsInterface);
            return true;
        }
        return false;
    }

    private static boolean isToString(String name, String descriptor) {
        return name.equals(Constants.ToString) && descriptor.equals(Constants.ToStringDesc);
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

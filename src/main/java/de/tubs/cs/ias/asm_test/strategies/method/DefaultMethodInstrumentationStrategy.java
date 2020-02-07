package de.tubs.cs.ias.asm_test.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;
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

    public DefaultMethodInstrumentationStrategy(MethodVisitor mv) {
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
            logger.info("Replacing toString for {} with call to TString.valueOf",  owner);
            this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TStringQN, Constants.VALUE_OF, String.format("(%s)%s", Constants.ObjectDesc, Constants.TStringDesc), false);
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

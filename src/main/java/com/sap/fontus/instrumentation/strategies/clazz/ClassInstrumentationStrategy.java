package com.sap.fontus.instrumentation.strategies.clazz;

import com.sap.fontus.TriConsumer;
import org.objectweb.asm.FieldVisitor;

import java.util.Optional;

public interface ClassInstrumentationStrategy  {

    Optional<FieldVisitor> instrumentFieldInstruction(int access, String name, String descriptor,
                                                      String signature, Object value, TriConsumer tc);

    String instrumentSuperClass(String superClass);
}

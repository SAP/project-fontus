package com.sap.fontus.instrumentation;

public interface InstrumenterInterface {

    byte[] instrumentClassByteArray(byte[] classfileBuffer, ClassLoader loader, String className);

}

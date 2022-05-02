package com.sap.fontus.instrumentation;

public interface InstrumenterInterface {

    public byte[] instrumentClassByteArray(byte[] classfileBuffer, ClassLoader loader, String className);

}

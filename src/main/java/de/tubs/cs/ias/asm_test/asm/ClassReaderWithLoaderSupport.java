package de.tubs.cs.ias.asm_test.asm;

import de.tubs.cs.ias.asm_test.ClassResolver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClassReaderWithLoaderSupport extends org.objectweb.asm.ClassReader {

    private static final int FourKB = 4096;

    /**
     * Constructs a new {@link org.objectweb.asm.ClassReader} object.
     *
     * @param classFile the JVMS ClassFile structure to be read.
     */
    public ClassReaderWithLoaderSupport(byte[] classFile) {
        super(classFile);
    }

    /**
     * Constructs a new {@link org.objectweb.asm.ClassReader} object.
     *
     * @param classFileBuffer a byte array containing the JVMS ClassFile structure to be read.
     * @param classFileOffset the offset in byteBuffer of the first byte of the ClassFile to be read.
     * @param classFileLength the length in bytes of the ClassFile to be read.
     */
    public ClassReaderWithLoaderSupport(byte[] classFileBuffer, int classFileOffset, int classFileLength) {
        super(classFileBuffer, classFileOffset, classFileLength);
    }

    /**
     * Constructs a new {@link org.objectweb.asm.ClassReader} object.
     *
     * @param inputStream an input stream of the JVMS ClassFile structure to be read. This input
     * stream must contain nothing more than the ClassFile structure itself. It is read from its
     * current position to its end.
     * @throws IOException if a problem occurs during reading.
     */
    public ClassReaderWithLoaderSupport(InputStream inputStream) throws IOException {
        super(inputStream);
    }

    /**
     * Constructs a new {@link org.objectweb.asm.ClassReader} object.
     *
     * @param className the fully qualified name of the class to be read. The ClassFile structure is
     * retrieved with the current class loader's {@link ClassLoader#getSystemResourceAsStream}.
     * @throws IOException if an exception occurs during reading.
     */
    public ClassReaderWithLoaderSupport(String className) throws IOException {
        super(className);
        throw new UnsupportedOperationException("Can't call constructor without providing a classloader");
    }

    public ClassReaderWithLoaderSupport(ClassResolver resolver, String className) throws IOException {
        this(readStream(resolver.resolve(className), className));
    }

    private static byte[] readStream(final InputStream inputStream, String className)
            throws IOException {
        if (inputStream == null) {
            throw new IOException(String.format("Class '%s' not found!", className));
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] data = new byte[FourKB];
            int bytesRead;
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                outputStream.write(data, 0, bytesRead);
            }
            outputStream.flush();
            return outputStream.toByteArray();
        } finally {
            inputStream.close();
        }
    }
}

package com.sap.fontus.utils.offline;

import com.sap.fontus.utils.Pair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

class JarOutputStreamWriter implements Consumer<Pair<JarEntry, byte[]>> {
    private static final int OneKB = 1024;
    private final JarOutputStream jos;

    JarOutputStreamWriter(JarOutputStream jos) {
        this.jos = jos;
    }

    @Override
    public void accept(Pair<JarEntry, byte[]> output) {
        JarEntry jeo = output.x;
        byte[] bytes = output.y;

        this.writeJarEntry(jeo, bytes);
    }

    private synchronized void writeJarEntry(JarEntry jeo, byte[] bytes) {
        try {
            jos.putNextEntry(jeo);
            if (bytes != null) {
                copySingleEntry(new ByteArrayInputStream(bytes), jos);
            }
            jos.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copySingleEntry(InputStream i, OutputStream o) throws IOException {
        int len = 0;
        byte[] buffer = new byte[OneKB];

        while ((len = i.read(buffer, 0, buffer.length)) != -1) {
            o.write(buffer, 0, len);
        }
    }
}

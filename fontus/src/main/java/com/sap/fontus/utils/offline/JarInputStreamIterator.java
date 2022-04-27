package com.sap.fontus.utils.offline;

import com.sap.fontus.utils.IOUtils;
import com.sap.fontus.utils.Pair;

import java.io.IOException;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

class JarInputStreamIterator implements Iterator<Pair<JarEntry, byte[]>> {
    private final JarInputStream jis;
    private final Queue<Pair<JarEntry, byte[]>> streamBuffer;

    JarInputStreamIterator(JarInputStream jis) {
        this.jis = jis;
        this.streamBuffer = new ConcurrentLinkedQueue<>();
    }

    @Override
    public synchronized boolean hasNext() {
        Pair<JarEntry, byte[]> jarEntry = this.readJarEntry();

        if (jarEntry != null) {
            this.streamBuffer.add(jarEntry);
            return true;
        }

        return false;
    }

    @Override
    public Pair<JarEntry, byte[]> next() {
        return this.streamBuffer.remove();
    }


    private synchronized Pair<JarEntry, byte[]> readJarEntry() {
        try {
            JarEntry jei = jis.getNextJarEntry();
            if (jei == null) {
                return null;
            }
            byte[] entryBytes = IOUtils.readStream(jis);

            return new Pair<>(jei, entryBytes);
        } catch (IOException e) {
            return null;
        }

    }
}

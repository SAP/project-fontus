package com.sap.fontus.utils.offline;

import com.sap.fontus.utils.Pair;

import java.util.concurrent.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class ParallelInstrumenter {
    private final JarInputStream jis;
    private final JarOutputStream jos;
    private final Function<Pair<JarEntry, byte[]>, Pair<JarEntry, byte[]>> jarEntryProcessor;

    public ParallelInstrumenter(JarInputStream jis, JarOutputStream jos, Function<Pair<JarEntry, byte[]>, Pair<JarEntry, byte[]>> jarEntryProcessor) {
        this.jis = jis;
        this.jos = jos;
        this.jarEntryProcessor = jarEntryProcessor;
    }

    public void execute() {
        JarInputStreamIterator jisi = new JarInputStreamIterator(this.jis);
        JarOutputStreamWriter josw = new JarOutputStreamWriter(this.jos);
        ExecutorService processingExecutor = Executors.newFixedThreadPool(4);
        ExecutorService outputExecutor = Executors.newSingleThreadExecutor();

        while (jisi.hasNext()) {
            Pair<JarEntry, byte[]> input = jisi.next();
            processingExecutor.submit(() -> {
                Pair<JarEntry, byte[]> output = this.jarEntryProcessor.apply(input);

                outputExecutor.submit(() -> josw.accept(output));
            });
        }

        try {
            processingExecutor.awaitTermination(15L, TimeUnit.MINUTES);
            outputExecutor.awaitTermination(15L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

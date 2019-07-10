package de.tubs.cs.ias.asm_test;

import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * A recording of the visitXXX calls for a given Method.
 *
 * It can be replayed by calling replay with a MethodVisitor where the code should be moved to.
 */
class MethodVisitRecording {
    private final Collection<Consumer<MethodVisitor>> recording;

    MethodVisitRecording() {
        this.recording = new ArrayList<>();
    }

    /**
     * Adds a visitXXX call to the recording
     */
    void add(Consumer<MethodVisitor> c) {
        this.recording.add(c);
    }

    /**
     * Replays the recording
     * @param mv The visitor onto which the recording shall be replayed
     */
    void replay(MethodVisitor mv) {
        for(Consumer<MethodVisitor> c : this.recording) {
            c.accept(mv);
        }
    }
}

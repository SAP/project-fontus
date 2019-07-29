package de.tubs.cs.ias.asm_test.method;

import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * A recording of the visitXXX calls for a given Method.
 * <p>
 * It can be replayed by calling replay with a MethodVisitor where the code should be moved to.
 */
public class MethodVisitRecording {
    private final Collection<Consumer<MethodVisitor>> recording;

    public MethodVisitRecording() {
        this.recording = new ArrayList<>();
    }

    /**
     * Adds a visitXXX call to the recording
     */
    public void add(Consumer<MethodVisitor> c) {
        this.recording.add(c);
    }

    /**
     * Replays the recording
     *
     * @param mv The visitor onto which the recording shall be replayed
     */
    public void replay(MethodVisitor mv) {
        for (Consumer<MethodVisitor> c : this.recording) {
            c.accept(mv);
        }
    }
}

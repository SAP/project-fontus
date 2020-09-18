package de.tubs.cs.ias.asm_test.taintaware.shared;

import java.util.ArrayList;
import java.util.List;

public class IASTaintSourceRegistry {
    private static IASTaintSourceRegistry instance;
    private final List<IASTaintSource> sources = new ArrayList<>();
    private int counter = 0;

    public synchronized IASTaintSource getOrRegisterTaintSource(String name) {
        for (IASTaintSource source : sources) {
            if (source.getName().equals(name)) {
                return source;
            }
        }
        this.counter++;
        IASTaintSource source = new IASTaintSource(name, this.counter);
        this.sources.add(source);
        return source;
    }

    public synchronized IASTaintSource get(int id) {
        if (id <= 0 || id > this.counter) {
            return null;
        }
        return this.sources.get(id - 1);
    }

    public static synchronized IASTaintSourceRegistry getInstance() {
        if (instance == null) {
            instance = new IASTaintSourceRegistry();
        }
        return instance;
    }
}

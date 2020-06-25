package de.tubs.cs.ias.asm_test;

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public abstract class AbstractBenchmark {
    @Param("default")
    public String factoryName;
    protected Factory factory;

    @Setup
    public void setup() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        this.factory = (Factory) Class.forName(factoryName).newInstance();
    }
}

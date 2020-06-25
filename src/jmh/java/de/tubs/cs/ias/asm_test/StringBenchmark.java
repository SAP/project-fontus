package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;
import org.openjdk.jmh.annotations.*;

@State(Scope.Benchmark)
public class StringBenchmark extends AbstractBenchmark {
    @Param({"100", "250", "10000"})
    public int length;
    @Param({"0", "1", "2", "3", "4", "5", "10", "50", "100"})
    public int trCount;
    public int start;
    public int end;

    public IASStringable string1;
    public IASStringable string2;
    private IASStringable testSubstring;
    private IASStringable testConcat;

    @Setup
    public void localSetup() {
        string1 = factory.createRandomString(length, trCount);
        string2 = factory.createRandomString(length, trCount);
        start = length / 3;
        end = start * 2;
    }

    @Setup(Level.Invocation)
    public void setupEach() {
        testSubstring = testSubstring();
        testConcat = testConcat();
    }

    @Benchmark
    public IASStringable testSubstring() {
        return string1.substring(start, end);
    }

    @Benchmark
    public IASStringable testConcat() {
        return string1.concat(string2);
    }

    @Benchmark
    public boolean testSubstringEvaluation() {
        return testSubstring.isTainted();
    }

    @Benchmark
    public boolean testConcatEvaluation () {
        return testConcat.isTainted();
    }
}

package de.tubs.cs.ias.asm_test;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.tubs.cs.ias.asm_test.taintaware.array.ArrayFactory;
import de.tubs.cs.ias.asm_test.taintaware.bool.BoolFactory;
import de.tubs.cs.ias.asm_test.taintaware.lazybasic.LazyBasicFactory;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.LazyComplexFactory;
import de.tubs.cs.ias.asm_test.taintaware.range.RangeFactory;
import de.tubs.cs.ias.asm_test.taintaware.plain.PlainFactory;

public class Main {
    public static void main(String[] args) throws IOException, RunnerException {
        final Class<?>[] benchmarks = {
                StringBenchmark.class
        };
        final Class<?>[] factories = new Class[]{
                ArrayFactory.class,
                BoolFactory.class,
                LazyBasicFactory.class,
                LazyComplexFactory.class,
                RangeFactory.class,
                PlainFactory.class,
        };
        for(Class<?> factory : factories) {
            final String filePath = String.format("../benchmark_results/comparing_benchmarks/%s-%s.json", factory.getSimpleName(), new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date()));
            final File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            } else {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            try {
                ChainedOptionsBuilder optBuilder = new OptionsBuilder();

                for (Class<?> benchmark : benchmarks) {
                    optBuilder.include(benchmark.getSimpleName());
                }

                optBuilder.timeUnit(TimeUnit.NANOSECONDS)
                        .mode(Mode.AverageTime)
                        .forks(1)
                        .warmupIterations(5)
                        .warmupTime(TimeValue.milliseconds(1000))
                        .measurementIterations(10)
                        .measurementTime(TimeValue.milliseconds(1000))
                        .param("factoryName", factory.getName() )
                        .resultFormat(ResultFormatType.JSON)
                        .detectJvmArgs()
                        .result(filePath);

                Options opt = optBuilder.build();
                new Runner(opt).run();
            } catch (Exception e) {
                e.printStackTrace(System.out);
                throw e;
            }
        }
    }
}

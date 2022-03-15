package com.sap.fontus.asm.speculative;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sap.fontus.asm.resolver.ClassResolverFactory;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.instrumentation.Instrumenter;
import com.sap.fontus.instrumentation.InstrumenterInterface;
import com.sap.fontus.utils.BytecodeRegistry;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpeculativeParallelInstrumenter implements InstrumenterInterface {
    private static final SpeculativeParallelInstrumenter INSTANCE = new SpeculativeParallelInstrumenter();
    private final ExecutorService executorService;
    private final Cache<String, byte[]> instrumentedCache;
    private final Instrumenter instrumenter;
    private final Configuration configuration;
    private final CombinedExcludedLookup combinedExcludedLookup;

    private SpeculativeParallelInstrumenter() {
        this.executorService = Executors.newFixedThreadPool(10);
        this.instrumentedCache = Caffeine.newBuilder().build();
        this.configuration = Configuration.getConfiguration();
        this.instrumenter = new Instrumenter(this.configuration);
        this.combinedExcludedLookup = new CombinedExcludedLookup();
    }

    public void submitSpeculativeInstrumentation(String internalName, ClassLoader classLoader) {
        if (ClassResolverFactory.createClassFinder().isClassLoaded(internalName)) {
            return;
        }

        if (this.combinedExcludedLookup.isJdkClass(internalName)) {
            return;
        }

        Optional<byte[]> commonClass = BytecodeRegistry.getInstance().getClassData(internalName);

        if (commonClass.isPresent()) {
            byte[] bytes = commonClass.get();

            if (!this.instrumentedCache.asMap().containsKey(internalName)) {
                this.executorService.submit(() -> {
                    this.instrumentedCache.get(internalName, (name) -> this.instrumenter.instrumentClassByteArray(bytes, classLoader, name));
                });
            }
        }
    }

    @Override
    public byte[] instrumentClassByteArray(byte[] classfileBuffer, ClassLoader loader, String className) {
        return this.instrumentedCache.get(className, (name) -> this.instrumenter.instrumentClassByteArray(classfileBuffer, loader, name));
    }

    public static SpeculativeParallelInstrumenter getInstance() {
        return INSTANCE;
    }



}

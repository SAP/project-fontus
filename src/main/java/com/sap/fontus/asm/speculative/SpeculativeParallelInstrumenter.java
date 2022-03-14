package com.sap.fontus.asm.speculative;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sap.fontus.asm.resolver.ClassResolverFactory;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.instrumentation.Instrumenter;
import com.sap.fontus.utils.BytecodeRegistry;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpeculativeParallelInstrumenter {
    private static final SpeculativeParallelInstrumenter INSTANCE = new SpeculativeParallelInstrumenter();
    private final ExecutorService executorService;
    private final Cache<String, byte[]> instumentedCache;
    private final Instrumenter instrumenter;
    private final Configuration configuration;
    private final CombinedExcludedLookup combinedExcludedLookup;

    private SpeculativeParallelInstrumenter() {
        this.executorService = Executors.newFixedThreadPool(10);
        this.instumentedCache = Caffeine.newBuilder().build();
        this.instrumenter = new Instrumenter();
        this.configuration = Configuration.getConfiguration();
        this.combinedExcludedLookup = new CombinedExcludedLookup();
    }

    private boolean isSpeculativeDeactive() {
        return this.configuration.isOfflineInstrumentation()
                || !this.configuration.isSpeculativeInstrumentation()
                || this.configuration.isHybridMode();
    }

    private boolean isSpeculativeActive() {
        return !this.isSpeculativeDeactive();
    }

    public void submitSpeculativeInstrumentation(String internalName, ClassLoader classLoader) {
        if (this.isSpeculativeDeactive()) {
            return;
        }

        if (ClassResolverFactory.createClassFinder().isClassLoaded(internalName)) {
            return;
        }

        if (this.combinedExcludedLookup.isJdkClass(internalName)) {
            return;
        }

        Optional<byte[]> commonClass = BytecodeRegistry.getInstance().getClassData(internalName);

        if (commonClass.isPresent()) {
            byte[] bytes = commonClass.get();

            if (!this.instumentedCache.asMap().containsKey(internalName)) {
                this.executorService.submit(() -> {
                    this.instumentedCache.get(internalName, (name) -> this.instrumenter.instrumentClassByteArray(bytes, classLoader, name));
                });
            }
        }
    }

    public byte[] instrument(String internalName, ClassLoader classLoader, byte[] classBytes) {
        if (this.isSpeculativeActive()) {
            return this.instumentedCache.get(internalName, (name) -> this.instrumenter.instrumentClassByteArray(classBytes, classLoader, name));
        }
        return this.instrumenter.instrumentClassByteArray(classBytes, classLoader, internalName);
    }

    public static SpeculativeParallelInstrumenter getInstance() {
        return INSTANCE;
    }
}

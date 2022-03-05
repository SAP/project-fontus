package com.sap.fontus.asm.speculative;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sap.fontus.asm.resolver.AgentClassResolver;
import com.sap.fontus.asm.resolver.ClassResolverFactory;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.instrumentation.Instrumenter;
import com.sap.fontus.utils.ClassFinder;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpeculativeParallelInstrumenter {
    private static final SpeculativeParallelInstrumenter INSTANCE = new SpeculativeParallelInstrumenter();
    private final ExecutorService executorService;
    private final Cache<String, byte[]> instumentedCache;
    private final Instrumenter instrumenter;
    private final CombinedExcludedLookup combinedExcludedLookup;

    private SpeculativeParallelInstrumenter() {
        this.executorService = Executors.newFixedThreadPool(10);
        this.instumentedCache = Caffeine.newBuilder().build();
        this.instrumenter = new Instrumenter();
        this.combinedExcludedLookup = new CombinedExcludedLookup();
    }

    public void submitSpeculativeInstrumentation(String internalName, ClassLoader classLoader) {
        if (Configuration.getConfiguration().isOfflineInstrumentation()) {
            return;
        }

        if (!Configuration.getConfiguration().isSpeculativeInstrumentation()) {
            return;
        }

        if (ClassResolverFactory.createClassFinder().isClassLoaded(internalName)) {
            return;
        }

        if (this.combinedExcludedLookup.isJdkClass(internalName)) {
            return;
        }

        Map<String, byte[]> commonClassCache = ClassResolverFactory.getCommonClassesCache();

        if (commonClassCache.containsKey(internalName)) {
            byte[] bytes = commonClassCache.get(internalName);
            this.executorService.submit(() -> {
                this.instumentedCache.get(internalName, (name) -> this.instrumenter.instrumentClassByteArray(bytes, classLoader, name));
            });
        }
    }

    public byte[] instrument(String internalName, ClassLoader classLoader, byte[] classBytes) {
        return this.instumentedCache.get(internalName, (name) -> this.instrumenter.instrumentClassByteArray(classBytes, classLoader, name));
    }

    public static SpeculativeParallelInstrumenter getInstance() {
        return INSTANCE;
    }
}

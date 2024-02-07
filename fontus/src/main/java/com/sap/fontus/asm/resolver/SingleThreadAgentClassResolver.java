package com.sap.fontus.asm.resolver;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SingleThreadAgentClassResolver extends BackgroundAgentClassResolver {

    SingleThreadAgentClassResolver(ClassLoader classLoader) {
        super(classLoader, Executors.newSingleThreadExecutor());
    }

    public void initialize() {
        super.initialize();

        // Now wait for shutdown with a timeout
        this.executorService.shutdown();
        try {
            this.executorService.awaitTermination(5L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

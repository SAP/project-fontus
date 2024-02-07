package com.sap.fontus.asm.resolver;

import java.util.concurrent.Executors;

public class ParallelAgentClassResolver extends BackgroundAgentClassResolver {

    ParallelAgentClassResolver(ClassLoader classLoader) {
        super(classLoader, Executors.newCachedThreadPool());
    }

}

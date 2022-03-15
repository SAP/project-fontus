package com.sap.fontus.asm.resolver;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sap.fontus.agent.TaintAgent;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.utils.ClassFinder;

public class ClassResolverFactory {
    private static final LoadingCache<ClassLoader, AgentClassResolver> classResolvers;
    private static final OfflineClassResolver offlineClassResolver;
    private static final AgentClassResolver nullResolver;
    private static final ClassFinder classFinder;

    static {
        classResolvers = Caffeine.newBuilder().build(ClassResolverFactory::createOnlineClassResolver);
        nullResolver = Configuration.getConfiguration().isParallel() ? new ParallelAgentClassResolver(null) : new AgentClassResolver(null);
        offlineClassResolver = new OfflineClassResolver(classResolvers.get(ClassLoader.getSystemClassLoader()));
        classFinder = new ClassFinder(TaintAgent.getInstrumentation());
    }

    public static IClassResolver createClassResolver(ClassLoader classLoader) {
        if (Configuration.getConfiguration().isOfflineInstrumentation()) {
            return offlineClassResolver;
        } else {
            if (classLoader == null) {
                return nullResolver;
            }
            return classResolvers.get(classLoader);
        }
    }

    public static ClassFinder createClassFinder() {
        return classFinder;
    }

    private static AgentClassResolver createOnlineClassResolver(ClassLoader classLoader) {
        AgentClassResolver agentClassResolver = Configuration.getConfiguration().isParallel() ? new ParallelAgentClassResolver(classLoader) : new AgentClassResolver(classLoader);

        agentClassResolver.initialize();

        return agentClassResolver;
    }

    private static void loadCommonClassCacheFor(ClassLoader classLoader) {

    }
}

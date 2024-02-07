package com.sap.fontus.asm.resolver;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sap.fontus.Constants;
import com.sap.fontus.utils.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * AgentClassResolvers have been split into the following hierarchy:
 *
 * IClassResolver
 *  |-AgentClassResolver
 *     |-CachingAgentClassResolver
 *         |-CallingThreadAgentClassResolver
 *         |-BackgroundAgentClassResolver
 *             |-SingleThreadAgentClassResolver
 *             |-ParallelAgentClassResolver
 *
 * The AgentClassResolver is simple and keeps a single cache to itself for resolvedClasses.
 * No pre-loading is performed.
 *
 * The CachingAgentClassResolver attempts to pre-load classes from the classloader into
 * the BytecodeRegistry during the initialize() method. This can be performed either
 * on the calling thread or in the background.
 *
 * The CallingThreadAgentClassResolver pre-caches all classes using the calling thread during
 * initialization, which may block execution if there are a lot of classes to load or some
 * are unreachable.
 *
 * The BackgroundAgentClassResolver pre-caches classes using executors in two ways:
 *
 * The SingleThreadAgentClassResolver pre-loads classes using a single thread in the background,
 * but then wait for the thread to terminate, with a timeout. The behaviour should be similar to
 * that of the CallingThreadAgentClassResolver, except with a timeout. This resolver in some cases
 * caused execution to hang (until the timeout) which needs invesitgation.
 *
 * The ParallelAgentClassResolver loads classes using a thread pool, and does not wait for pre-caching
 * to complete before exiting the initialize method.
 */
public class AgentClassResolver implements IClassResolver {

    private static final Logger logger = LogUtils.getLogger();

    protected final ClassLoader classLoader;
    protected final LoadingCache<String, byte[]> classCache;

    AgentClassResolver(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.classCache = Caffeine.newBuilder().build(this::loadClassBytes);
    }

    public void initialize() {

    }

    private byte[] loadClassBytes(String className) throws IOException {
        String fixed = Utils.dotToSlash(className) + Constants.CLASS_FILE_SUFFIX;
        logger.info("Trying to resolve {} from {}", fixed, className);

        if (this.classLoader != null) {
            InputStream is = this.classLoader.getResourceAsStream(fixed);
            if (is != null) {
                return IOUtils.readStream(is);
            }
        }

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fixed);
        if (is == null) {
            is = ClassLoader.getSystemResourceAsStream(fixed);
        }
        if (is != null) {
            return IOUtils.readStream(is);
        }
        return null;
    }

    public Optional<byte[]> resolve(String className) {
        // TODO: what about multi-dimensional arrays?
        if (className.startsWith("[L") && className.endsWith(";")) {
            className = className.substring(2, className.length() - 1);
        }

        byte[] bytes = this.classCache.get(className);
        return Optional.ofNullable(bytes);
    }

}

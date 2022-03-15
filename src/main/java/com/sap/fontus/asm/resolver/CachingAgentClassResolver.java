package com.sap.fontus.asm.resolver;

import com.sap.fontus.utils.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

public abstract class CachingAgentClassResolver extends AgentClassResolver {

    CachingAgentClassResolver(ClassLoader classLoader) {
        super(classLoader);
    }

    protected void loadUrl(URL url) {
        try {
            InputStream inputStream = url.openStream();
            JarClassResolver jarClassResolver = new JarClassResolver();

            Map<String, byte[]> classes = jarClassResolver.loadClassesFrom(inputStream);

            classes.forEach(BytecodeRegistry.getInstance()::addClassData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<byte[]> resolve(String className) {
        // TODO: what about multi-dimensional arrays?
        if (className.startsWith("[L") && className.endsWith(";")) {
            className = className.substring(2, className.length() - 1);
        }

        Optional<byte[]> commonCached = BytecodeRegistry.getInstance().getClassData(className);
        if (commonCached.isPresent()) {
            return commonCached;
        }

        byte[] bytes = this.classCache.get(className);
        return Optional.ofNullable(bytes);
    }
}

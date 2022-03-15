package com.sap.fontus.asm.resolver;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelAgentClassResolver extends CachingAgentClassResolver {

    private final ExecutorService executorService;

    ParallelAgentClassResolver(ClassLoader classLoader) {
        super(classLoader);
        this.executorService = Executors.newCachedThreadPool();
    }

    public void initialize() {
        if (classLoader instanceof URLClassLoader) {
            URL[] urls = ((URLClassLoader) classLoader).getURLs();
            if (urls != null) {
                for (URL url : urls) {
                    if ("jar".equals(url.getProtocol())) {
                        this.executorService.submit(() -> this.loadUrl(url));
                    }
                }
            }
        }
    }

}

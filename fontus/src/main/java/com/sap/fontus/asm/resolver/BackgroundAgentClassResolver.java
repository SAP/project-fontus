package com.sap.fontus.asm.resolver;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ExecutorService;

public abstract class BackgroundAgentClassResolver extends CachingAgentClassResolver {

    protected final ExecutorService executorService;

    BackgroundAgentClassResolver(ClassLoader classLoader, ExecutorService executorService) {
        super(classLoader);
        this.executorService = executorService;
    }

    public void initialize() {
        if (this.classLoader instanceof URLClassLoader urlClassLoader) {
            URL[] urls = urlClassLoader.getURLs();
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

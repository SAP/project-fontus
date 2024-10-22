package com.sap.fontus.asm.resolver;

import java.net.URL;
import java.net.URLClassLoader;

public class CallingThreadAgentClassResolver extends CachingAgentClassResolver {

    CallingThreadAgentClassResolver(ClassLoader classLoader) {
        super(classLoader);
    }

    public void initialize() {

        if (this.classLoader instanceof URLClassLoader urlClassLoader) {
            URL[] urls = urlClassLoader.getURLs();
            if (urls != null) {
                for (URL url : urls) {
                    if ("jar".equals(url.getProtocol())) {
                        this.loadUrl(url);
                    }
                }
            }
        }

    }
}

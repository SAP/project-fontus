package com.sap.fontus.utils.lookups;

import com.sap.fontus.Constants;
import com.sap.fontus.asm.ClassResolver;
import com.sap.fontus.utils.Utils;

public class CombinedExcludedLookup {
    private final ClassResolver resolver;
    private final ClassLoader classLoader;

    public CombinedExcludedLookup(ClassLoader classLoader) {
        this.resolver = new ClassResolver(classLoader);
        this.classLoader = classLoader;
    }

    public CombinedExcludedLookup() {
        this.classLoader = null;
        this.resolver = null;
    }

    public boolean isJdkOrAnnotation(String internalName) {
        return this.isJdkClass(internalName) || this.isAnnotation(internalName);
    }

    public boolean isJdkClass(String internalName) {
        return JdkClassesLookup.getInstance().isJdkClass(internalName, classLoader);
    }

    public boolean isJdkClass(Class cls) {
        return JdkClassesLookup.getInstance().isJdkClass(Utils.getInternalName(cls), classLoader);
    }

    public boolean isPackageExcludedOrJdk(String internalName) {
        return this.isJdkClass(internalName) || this.isPackageExcluded(internalName);
    }

    public boolean isPackageExcluded(String internalName) {
        return ExcludedPackagesLookup.isExcluded(internalName);
    }

    public boolean isAnnotation(String internalName) {
        if (this.resolver == null) {
            throw new IllegalStateException("You did not provide a ClassResolver when created this lookup, therefore annotation lookup is not possible");
        }
        return AnnotationLookup.getInstance().isAnnotation(internalName, this.resolver);
    }

    public boolean isProxyClass(String internalName, byte[] classBuffer) {
        return ProxyLookup.isProxyClass(internalName, classBuffer);
    }

    public boolean isFontusClass(String internalName) {
        return internalName.startsWith(Utils.dotToSlash(Constants.PACKAGE));
    }

    public boolean isFontusClass(Class<?> cls) {
        return cls.getName().startsWith(Constants.PACKAGE);
    }

    public boolean isAnnotation(Class<?> cls) {
        return isAnnotation(Utils.dotToSlash(cls.getName()));
    }

    public boolean isPackageExcluded(Class<?> cls) {
        return isPackageExcluded(Utils.dotToSlash(cls.getName()));
    }
}

package de.tubs.cs.ias.asm_test.utils.lookups;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.ClassResolver;
import de.tubs.cs.ias.asm_test.utils.Utils;

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
}

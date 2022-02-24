package com.sap.fontus.utils.lookups;

import com.sap.fontus.Constants;
import com.sap.fontus.TaintStringHelper;
import com.sap.fontus.asm.IClassResolver;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.utils.InstrumentationFactory;
import com.sap.fontus.utils.Utils;
import org.objectweb.asm.Type;

public class CombinedExcludedLookup {
    private final IClassResolver resolver;
    private final ClassLoader classLoader;

    public CombinedExcludedLookup(ClassLoader classLoader) {
        this.resolver = InstrumentationFactory.createClassResolver(classLoader);
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
        return this.isJdkClass(internalName) || this.isExcluded(internalName);
    }

    public boolean isExcluded(String internalName) {
        return this.isPackageExcluded(internalName) || this.isClassExcluded(internalName);
    }

    private boolean isPackageExcluded(String internalName) {
        return ExcludedPackagesLookup.isExcludedPackage(internalName);
    }

    private boolean isClassExcluded(String internalName) {
        return ExcludedPackagesLookup.isExcludedClass(internalName);
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

    public boolean isClassAlreadyInstrumentedForHybrid(String internalName) {
        return Configuration.getConfiguration().getInstumentedClasses().contains(internalName);
    }

    public boolean isFontusClass(String internalName) {
        return internalName.startsWith(Utils.dotToSlash(Constants.PACKAGE)) && !TaintStringHelper.class.getName().equals(Utils.slashToDot(internalName));
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

    public boolean isPackageExcludedOrJdk(Class<?> cls) {
        return isPackageExcludedOrJdk(Type.getInternalName(cls));
    }
}

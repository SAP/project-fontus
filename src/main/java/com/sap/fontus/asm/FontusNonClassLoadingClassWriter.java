package com.sap.fontus.asm;

import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.mutabilitydetector.asm.NonClassloadingClassWriter;
import org.mutabilitydetector.asm.typehierarchy.TypeHierarchyReader;
import org.objectweb.asm.ClassReader;

public class FontusNonClassLoadingClassWriter extends NonClassloadingClassWriter {
    private final CombinedExcludedLookup combinedExcludedLookup;
    public FontusNonClassLoadingClassWriter(int flags) {
        super(flags);
        this.combinedExcludedLookup = new CombinedExcludedLookup();
    }

    public FontusNonClassLoadingClassWriter(ClassReader classReader, int flags) {
        super(classReader, flags);
        this.combinedExcludedLookup = new CombinedExcludedLookup();
    }

    public FontusNonClassLoadingClassWriter(ClassReader classReader, int flags, TypeHierarchyReader typeHierarchyReader) {
        super(classReader, flags, typeHierarchyReader);
        this.combinedExcludedLookup = new CombinedExcludedLookup();
    }

    public FontusNonClassLoadingClassWriter(ClassReader classReader, int flags, TypeHierarchyReader typeHierarchyReader, CombinedExcludedLookup excludedLookup) {
        super(classReader, flags, typeHierarchyReader);
        this.combinedExcludedLookup = excludedLookup;
    }
    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        String result = super.getCommonSuperClass(type1, type2);

        // TODO: Consider how to make this nicer to use/more generic
        if(!this.combinedExcludedLookup.isPackageExcludedOrJdk(type1) && !this.combinedExcludedLookup.isPackageExcludedOrJdk(type2)) {
            result = new InstrumentationHelper().instrumentSuperClass(result);
        }
        //System.out.printf("Common super of: %s <-> %s: %s%n", type1, type2, result);
        return result;
    }
}

package com.sap.fontus.asm;

import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.mutabilitydetector.asm.NonClassloadingClassWriter;
import org.mutabilitydetector.asm.typehierarchy.TypeHierarchyReader;
import org.objectweb.asm.ClassReader;

import org.objectweb.asm.Type;

public class FontusNonClassLoadingClassWriter extends NonClassloadingClassWriter {
    private final CombinedExcludedLookup combinedExcludedLookup;
    private final InstrumentationHelper helper;

    public FontusNonClassLoadingClassWriter(int flags) {
        super(flags);
        this.combinedExcludedLookup = new CombinedExcludedLookup();
        this.helper = new InstrumentationHelper();
    }

    public FontusNonClassLoadingClassWriter(ClassReader classReader, int flags) {
        super(classReader, flags);
        this.combinedExcludedLookup = new CombinedExcludedLookup();
        this.helper = new InstrumentationHelper();
    }

    public FontusNonClassLoadingClassWriter(ClassReader classReader, int flags, TypeHierarchyReader typeHierarchyReader) {
        super(classReader, flags, typeHierarchyReader);
        this.combinedExcludedLookup = new CombinedExcludedLookup();
        this.helper = new InstrumentationHelper();
    }

    public FontusNonClassLoadingClassWriter(ClassReader classReader, int flags, TypeHierarchyReader typeHierarchyReader, CombinedExcludedLookup excludedLookup) {
        super(classReader, flags, typeHierarchyReader);
        this.combinedExcludedLookup = excludedLookup;
        this.helper = new InstrumentationHelper();
    }
    @Override
    protected String getCommonSuperClass(String type1, String type2) {

        String type1Uninstrumented = helper.uninstrumentQN(type1);
        String type2Uninstrumented = helper.uninstrumentQN(type2);

        String resultInitial = super.getCommonSuperClass(type1Uninstrumented, type2Uninstrumented);
        String result = resultInitial;

        if(!this.combinedExcludedLookup.isPackageExcludedOrJdk(type1) && !this.combinedExcludedLookup.isPackageExcludedOrJdk(type2)) {
            result = helper.instrumentQN(result);
        } else {
            result = super.getCommonSuperClass(type1, type2);
        }
        // System.out.printf("Common super of: %s (uninstrumented: %s super: %s) <-> %s (uninstrumented: %s super: %s): %s (initially %s)%n",
        //                   type1, type1Uninstrumented, typeHierarchyReader.getSuperClass(Type.getObjectType(type1)),
        //                   type2, type2Uninstrumented, typeHierarchyReader.getSuperClass(Type.getObjectType(type2)),
        //                   result, resultInitial);
        return result;
    }
}

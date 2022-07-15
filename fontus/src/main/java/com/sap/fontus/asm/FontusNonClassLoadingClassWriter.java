package com.sap.fontus.asm;

import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.Utils;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.mutabilitydetector.asm.NonClassloadingClassWriter;
import org.mutabilitydetector.asm.typehierarchy.TypeHierarchyReader;
import org.objectweb.asm.ClassReader;

public class FontusNonClassLoadingClassWriter extends NonClassloadingClassWriter {
    private static final Logger logger = LogUtils.getLogger();

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
        //
        // In some cases, the types are uninstrumented, so if they inherit from an instrumented type (e.g. IASProperties)
        // the superclass found by the TypeHierarcyReader in getCommonSuperClass may be the uninstrumented version (e.g. Properties)
        // This means that if getCommonSuperClass is called with IASProperties and a class extending IASProperties, the common
        // Super class will not be IASProperties, but the parent of IASProperties (which is Hashtable). This messes up the stack
        // types.
        //
        // To get around this, we first *uninstrument* the input types (only if neither are JDK classes) and then find the
        // common supertype. Finally re-instrument the result to give e.g. IASProperties at the output.
        //
        // The bigger question is why the TypeHierarcyReader doesn't return the instrumented superclass...
        //
        String type1Uninstrumented = this.helper.uninstrumentQN(type1);
        String type2Uninstrumented = this.helper.uninstrumentQN(type2);
        try {
            String result = super.getCommonSuperClass(type1Uninstrumented, type2Uninstrumented);

            if (!this.combinedExcludedLookup.isPackageExcludedOrJdk(type1) && !this.combinedExcludedLookup.isPackageExcludedOrJdk(type2)) {
                result = this.helper.instrumentQN(result);
            } else {
                result = super.getCommonSuperClass(type1, type2);
            }
            // System.out.printf("Common super of: %s (uninstrumented: %s super: %s) <-> %s (uninstrumented: %s super: %s): %s (initially %s)%n",
            //                   type1, type1Uninstrumented, typeHierarchyReader.getSuperClass(Type.getObjectType(type1)),
            //                   type2, type2Uninstrumented, typeHierarchyReader.getSuperClass(Type.getObjectType(type2)),
            //                   result, resultInitial);
            return result;
        } catch(Exception e) {
            logger.warn("Can't determine common superclass of %s and %s as one of them can't be loaded. Returning Object..", type1, type2);
            Utils.logException(e);
            return "java/lang/Object";
        }
    }
}

package com.sap.fontus.instrumentation;

import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.unified.*;
import com.sap.fontus.taintaware.unified.reflect.*;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.Utils;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MethodProxies {

    private static final Logger logger = LogUtils.getLogger();

    /**
     * Some methods are not handled in a generic fashion, one can defined specialized proxies here
     */
    private static final Map<FunctionCall, FunctionCall> methodProxies = new HashMap<>();

    /**
     * If a method which is part of an interface should be proxied, place it here
     * The owner should be the interface
     */
    private static final Map<FunctionCall, FunctionCall> methodInterfaceProxies = new HashMap<>();

    /**
     * Initializes the method proxy maps.
     */
    private static void fillProxies() {
        methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/lang/System", "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASStringUtils.class), "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", false));
        methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "forName", String.format("(%s)Ljava/lang/Class;", Type.getDescriptor(IASString.class)), false));
        methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "forName", String.format("(%sZLjava/lang/ClassLoader;)Ljava/lang/Class;", Type.getDescriptor(IASString.class)), false));
        methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/net/URLEncoder", "encode", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(TURLEncoder.class), "encode", String.format("(%s%s)%s", Type.getDescriptor(IASString.class), Type.getDescriptor(IASString.class), Type.getDescriptor(IASString.class)), false));
        methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/net/URLEncoder", "encode", "(Ljava/lang/String;)Ljava/lang/String;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(TURLEncoder.class), "encode", String.format("(%s)%s", Type.getDescriptor(IASString.class), Type.getDescriptor(IASString.class)), false));
        methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/net/URLDecoder", "decode", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(TURLDecoder.class), "decode", String.format("(%s%s)%s", Type.getDescriptor(IASString.class), Type.getDescriptor(IASString.class), Type.getDescriptor(IASString.class)), false));
        methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/net/URLDecoder", "decode", "(Ljava/lang/String;)Ljava/lang/String;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(TURLDecoder.class), "decode", String.format("(%s)%s", Type.getDescriptor(IASString.class), Type.getDescriptor(IASString.class)), false));
        methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/lang/System", "getenv", "()Ljava/util/Map;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASStringUtils.class), "getenv", "()Ljava/util/Map;", false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getName", Type.getMethodDescriptor(Type.getType(IASString.class), Type.getType(Class.class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getSimpleName", Type.getMethodDescriptor(Type.getType(IASString.class), Type.getType(Class.class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getCanonicalName", "()Ljava/lang/String;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getCanonicalName", Type.getMethodDescriptor(Type.getType(IASString.class), Type.getType(Class.class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getTypeParameters", Type.getMethodDescriptor(Type.getType(TypeVariable[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getTypeParameters", Type.getMethodDescriptor(Type.getType(TypeVariable[].class), Type.getType(Class.class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getInterfaces", Type.getMethodDescriptor(Type.getType(Class[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getInterfaces", Type.getMethodDescriptor(Type.getType(Class[].class), Type.getType(Class.class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getGenericInterfaces", Type.getMethodDescriptor(Type.getType(java.lang.reflect.Type.class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getGenericInterfaces", Type.getMethodDescriptor(Type.getType(java.lang.reflect.Type[].class), Type.getType(Class.class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getEnclosingMethod", Type.getMethodDescriptor(Type.getType(java.lang.reflect.Method.class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getEnclosingMethod", Type.getMethodDescriptor(Type.getType(IASMethod.class), Type.getType(Class.class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getEnclosingConstructor", Type.getMethodDescriptor(Type.getType(Constructor.class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getEnclosingConstructor", Type.getMethodDescriptor(Type.getType(IASConstructor.class), Type.getType(Class.class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getMethods", Type.getMethodDescriptor(Type.getType(java.lang.reflect.Method[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getMethods", Type.getMethodDescriptor(Type.getType(IASMethod[].class), Type.getType(Class.class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethods", Type.getMethodDescriptor(Type.getType(java.lang.reflect.Method[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getDeclaredMethods", Type.getMethodDescriptor(Type.getType(IASMethod[].class), Type.getType(Class.class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getMethod", Type.getMethodDescriptor(Type.getType(java.lang.reflect.Method.class), Type.getType(String.class), Type.getType(Class[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getMethod", Type.getMethodDescriptor(Type.getType(IASMethod.class), Type.getType(Class.class), Type.getType(IASString.class), Type.getType(Class[].class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod", Type.getMethodDescriptor(Type.getType(Method.class), Type.getType(String.class), Type.getType(Class[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getDeclaredMethod", Type.getMethodDescriptor(Type.getType(IASMethod.class), Type.getType(Class.class), Type.getType(IASString.class), Type.getType(Class[].class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getFields", Type.getMethodDescriptor(Type.getType(Field[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getFields", Type.getMethodDescriptor(Type.getType(IASField[].class), Type.getType(Class.class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredFields", Type.getMethodDescriptor(Type.getType(Field[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getDeclaredFields", Type.getMethodDescriptor(Type.getType(IASField[].class), Type.getType(Class.class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getField", Type.getMethodDescriptor(Type.getType(Field.class), Type.getType(String.class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getField", Type.getMethodDescriptor(Type.getType(IASField.class), Type.getType(Class.class), Type.getType(IASString.class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredField", Type.getMethodDescriptor(Type.getType(Field.class), Type.getType(String.class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getDeclaredField", Type.getMethodDescriptor(Type.getType(IASField.class), Type.getType(Class.class), Type.getType(IASString.class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getConstructors", Type.getMethodDescriptor(Type.getType(Constructor[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getConstructors", Type.getMethodDescriptor(Type.getType(IASConstructor[].class), Type.getType(Class.class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredConstructors", Type.getMethodDescriptor(Type.getType(Constructor[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getDeclaredConstructors", Type.getMethodDescriptor(Type.getType(IASConstructor[].class), Type.getType(Class.class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getConstructor", Type.getMethodDescriptor(Type.getType(Constructor.class), Type.getType(Class[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getConstructor", Type.getMethodDescriptor(Type.getType(IASConstructor.class), Type.getType(Class.class), Type.getType(Class[].class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredConstructor", Type.getMethodDescriptor(Type.getType(Constructor.class), Type.getType(Class[].class)), false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getDeclaredConstructor", Type.getMethodDescriptor(Type.getType(IASConstructor.class), Type.getType(Class.class), Type.getType(Class[].class)), false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "isAssignableFrom", "(Ljava/lang/Class;)Z", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "isAssignableFrom", "(Ljava/lang/Class;Ljava/lang/Class;)Z", false));

        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getPackage", "()Ljava/lang/Package;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASReflectionProxy.class), "getPackageOfClass", "(Ljava/lang/Class;)Ljava/lang/Package;", false));

        // 38: invokevirtual #8                  // Method java/io/BufferedReader.lines:()Ljava/util/stream/Stream;
        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/io/BufferedReader", "lines", "()Ljava/util/stream/Stream;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASStringUtils.class), "bufferedReaderLines", "(Ljava/io/BufferedReader;)Ljava/util/stream/Stream;", false));

        // 215: invokeinterface #77,  1           // InterfaceMethod org/apache/tomcat/jdbc/pool/PoolConfiguration.getUseStatementFacade:()Z
        methodProxies.put(new FunctionCall(Opcodes.INVOKEINTERFACE, "org/apache/tomcat/jdbc/pool/PoolConfiguration", "getUseStatementFacade", "()Z", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASPreparedStatementUtils.class), "useStatementFacade", "(Ljava/lang/Object;)Z", false ));
        methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/util/Comparator", "comparing", "(Ljava/util/function/Function;Ljava/util/Comparator;)Ljava/util/Comparator;", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASStringUtils.class), "comparing", "(Ljava/util/function/Function;Ljava/util/Comparator;)Ljava/util/Comparator;", false));
        methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "org/olat/core/util/StringHelper", "cleanUTF8ForXml", "(Ljava/lang/String;)Ljava/lang/String;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASStringUtils.class), "cleanUTF8ForXml", "(Lcom/sap/fontus/taintaware/unified/IASString;)Lcom/sap/fontus/taintaware/unified/IASString;", false));
        // Add this just to prevent re-casting the security provider as an IASProperties
        methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/security/Security", "getProvider", "(Ljava/lang/String;)Ljava/security/Provider;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASSecurity.class), "getProvider", "(Lcom/sap/fontus/taintaware/unified/IASString;)Ljava/security/Provider;", false));
        // Prevent Code Source detection of Fontus classes
        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, Type.getInternalName(ProtectionDomain.class), "getCodeSource", "()Ljava/security/CodeSource;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASProtectionDomain.class), "getCodeSource", "(Ljava/security/ProtectionDomain;)Ljava/security/CodeSource;", false));
        // Prevent that Fontus classes are detected by classloader
        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Class.class), "getClassLoader", "()Ljava/lang/ClassLoader;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASClassProxy.class), "getClassLoader", "(Ljava/lang/Class;)Ljava/lang/ClassLoader;", false));
        // Fixes ClassCastException
        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, Type.getInternalName(ResourceBundle.class), "getString", "(Ljava/lang/String;)Ljava/lang/String;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASStringUtils.class), "getStringFromResourceBundle", "(Ljava/util/ResourceBundle;Lcom/sap/fontus/taintaware/unified/IASString;)Lcom/sap/fontus/taintaware/unified/IASString;", false));
        // Fix environment mess
        methodProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, Type.getInternalName(ProcessBuilder.class), "environment", "()Ljava/util/Map;", false),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessBuilderEnvironmentProxy.class), "getProcessBuilderEnv", "(Ljava/lang/ProcessBuilder;)Ljava/util/Map;", false));
    }

    private static void fillInterfaceProxies() {
        methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/util/Collection", "toArray", "([Ljava/lang/Object;)[Ljava/lang/Object;", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASToArrayProxy.class), "toArray", String.format("(L%s;[Ljava/lang/Object;)[Ljava/lang/Object;", Utils.dotToSlash(Collection.class.getName())), false));
        methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/util/Collection", "toArray", "()[Ljava/lang/Object;", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASToArrayProxy.class), "toArray", String.format("(L%s;)[Ljava/lang/Object;", Utils.dotToSlash(Collection.class.getName())), false));


    }

    private static void fillTaintPersistenceProxies() {
        methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/sql/PreparedStatement", "setString", "(ILjava/lang/String;)V", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASPreparedStatementUtils.class), "setString", String.format("(L%s;ILcom/sap/fontus/taintaware/unified/IASString;)V", Utils.dotToSlash(java.sql.PreparedStatement.class.getName())), false));
        methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/sql/PreparedStatement", "setNString", "(ILjava/lang/String;)V", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASPreparedStatementUtils.class), "setNString", String.format("(L%s;ILcom/sap/fontus/taintaware/unified/IASString;)V", Utils.dotToSlash(java.sql.PreparedStatement.class.getName())), false));
        methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/sql/PreparedStatement", "setObject", "(ILjava/lang/Object;I)V", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASPreparedStatementUtils.class), "setObject", String.format("(L%s;ILjava/lang/Object;I)V", Utils.dotToSlash(java.sql.PreparedStatement.class.getName())), false));
        methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/sql/PreparedStatement", "setObject", "(ILjava/lang/Object;II)V", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASPreparedStatementUtils.class), "setObject", String.format("(L%s;ILjava/lang/Object;II)V", Utils.dotToSlash(java.sql.PreparedStatement.class.getName())), false));
        methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/sql/PreparedStatement", "setObject", "(ILjava/lang/Object;)V", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASPreparedStatementUtils.class), "setObject", String.format("(L%s;ILjava/lang/Object;)V", Utils.dotToSlash(java.sql.PreparedStatement.class.getName())), false));
        methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/sql/ResultSet", "getString", "(I)Ljava/lang/String;", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASResultSetUtils.class), "getString", String.format("(L%s;I)Lcom/sap/fontus/taintaware/unified/IASString;", Utils.dotToSlash(java.sql.ResultSet.class.getName())), false));
        methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/sql/ResultSet", "getString", "(Ljava/lang/String;)Ljava/lang/String;", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASResultSetUtils.class), "getString", String.format("(L%s;Lcom/sap/fontus/taintaware/unified/IASString;)Lcom/sap/fontus/taintaware/unified/IASString;", Utils.dotToSlash(java.sql.ResultSet.class.getName())), false));
        methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/sql/ResultSet", "getObject", "(I)Ljava/lang/Object;", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASResultSetUtils.class), "getObject", String.format("(L%s;I)Ljava/lang/Object;", Utils.dotToSlash(java.sql.ResultSet.class.getName())), false));
        methodInterfaceProxies.put(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/sql/ResultSet", "getObject", "(Ljava/lang/String;)Ljava/lang/Object;", true),
                new FunctionCall(Opcodes.INVOKESTATIC, Type.getInternalName(IASResultSetUtils.class), "getObject", String.format("(L%s;Lcom/sap/fontus/taintaware/unified/IASString;)Ljava/lang/Object;", Utils.dotToSlash(java.sql.ResultSet.class.getName())), false));

    }

    static {
        fillProxies();
        fillInterfaceProxies();
    }

    private static boolean thisOrSuperQNEquals(String thisQn, final String requiredQn) {
        if (thisQn.equals(requiredQn)) {
            return true;
        }
        try {
            for (Class<?> cls = Class.forName(Utils.slashToDot(thisQn)); cls.getSuperclass() != null; cls = cls.getSuperclass()) {
                for (Class<?> interf : cls.getInterfaces()) {
                    if (Utils.dotToSlash(interf.getName()).equals(requiredQn)) {
                        return true;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    CombinedExcludedLookup combinedExcludedLookup;

    MethodProxies(CombinedExcludedLookup combinedExcludedLookup, Configuration configuration) {
        this.combinedExcludedLookup = combinedExcludedLookup;
        if(configuration.hasTaintPersistence()) {
            fillTaintPersistenceProxies();
        }
    }

    /**
     * Is there a proxy defined? If so apply and return true.
     */
    public FunctionCall shouldBeProxied(FunctionCall pfe) {

        FunctionCall proxy = methodProxies.get(pfe);
        if (proxy != null) {
            logger.info("Proxying call to {}.{}{} to {}.{}{}", pfe.getOwner(), pfe.getName(), pfe.getDescriptor(), proxy.getOwner(), proxy.getName(), proxy.getDescriptor());
            return proxy;
        }
        if (pfe.getOpcode() == Opcodes.INVOKEVIRTUAL || pfe.getOpcode() == Opcodes.INVOKEINTERFACE) {
            if (this.combinedExcludedLookup.isJdkClass(pfe.getOwner())) {
                for (Map.Entry<FunctionCall, FunctionCall> entry : methodInterfaceProxies.entrySet()) {
                    FunctionCall mip = entry.getKey();
                    if (pfe.getName().equals(mip.getName()) && pfe.getDescriptor().equals(mip.getDescriptor())) {
                        if (thisOrSuperQNEquals(pfe.getOwner(), mip.getOwner())) {
                            if(LogUtils.LOGGING_ENABLED) {
                                logger.info("Proxying interface call to {}.{}{}", pfe.getOwner(), pfe.getName(), pfe.getDescriptor());
                            }
                            return entry.getValue();
                        }
                    }
                }

            }
        }
        return pfe;
    }

}

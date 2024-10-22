package com.sap.fontus.taintaware.unified;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.runtime.ObjectMethods;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ObjectMethodsTest {

    private static IASString foo = null;
    private static IASString bar = null;
    private static IASString taintedBar = null;
    @BeforeAll
    static void setup() {
        Configuration.setTestConfig(TaintMethod.defaultTaintMethod());
        foo = IASString.fromString("foo");
        bar = IASString.fromString("bar");
        IASTaintMetadata md1 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterObject("dummy"));
        IASString tbar = IASString.fromString("bar");
        tbar.setTaint(md1);
        taintedBar = tbar;
    }

    record Foobar(IASString foo, IASString bar) { }

        @Test
    public void bootstrapToTString() throws Throwable {
        Foobar foobar = new Foobar(foo, bar);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle getFoo = lookup.findGetter(Foobar.class, "foo", IASString.class);
        MethodHandle getBar = lookup.findGetter(Foobar.class, "bar", IASString.class);
        CallSite cs = (CallSite) ObjectMethods.bootstrap(lookup, "toString", MethodType.methodType(IASString.class, Foobar.class), Foobar.class, "foo;bar", getFoo, getBar);
        MethodHandle handle = cs.dynamicInvoker();
        IASString o = (IASString) handle.invoke(foobar);
        Assertions.assertEquals(IASString.fromString("Foobar[foo=foo, bar=bar]"), o);
        Assertions.assertTrue(!o.isTainted());

    }

    @Test
    public void bootstrapToTStringTainted() throws Throwable {
        Foobar foobar = new Foobar(foo, taintedBar);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle getFoo = lookup.findGetter(Foobar.class, "foo", IASString.class);
        MethodHandle getBar = lookup.findGetter(Foobar.class, "bar", IASString.class);
        CallSite cs = (CallSite) ObjectMethods.bootstrap(lookup, "toString", MethodType.methodType(IASString.class, Foobar.class), Foobar.class, "foo;bar", getFoo, getBar);
        MethodHandle handle = cs.dynamicInvoker();
        IASString o = (IASString) handle.invoke(foobar);
        Assertions.assertEquals(IASString.fromString("Foobar[foo=foo, bar=bar]"), o);
        Assertions.assertTrue(o.isTainted());

    }
}

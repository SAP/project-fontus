package com.sap.fontus.taintaware.unified;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class IASFormatterTest {

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

    @Test
    public void formatTStrings() {
        IASString formatString = IASString.fromString("Foobar[foo=%s, bar=%s]");
        IASString result = IASString.format(formatString, foo, bar);
        Assertions.assertEquals(IASString.fromString("Foobar[foo=foo, bar=bar]"), result);
    }
}

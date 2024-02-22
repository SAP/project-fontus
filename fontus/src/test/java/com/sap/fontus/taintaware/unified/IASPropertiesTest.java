package com.sap.fontus.taintaware.unified;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class IASPropertiesTest {
    @BeforeAll
    static void setup() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }


    @Test
    public void testProperties() {
        IASProperties defaults = new IASProperties();
        defaults.setProperty(IASString.fromString("sup"), IASString.fromString("son"));
        IASString k2 = IASString.fromString("what's");
        IASProperties user = new IASProperties(defaults);
        user.setProperty(k2, IASString.fromString("up"));

        IASString g2 = user.getProperty(k2);
        IASString g1 = user.getProperty(IASString.fromString("sup"));

        Assert.assertEquals(IASString.fromString("up"), g2);
        Assert.assertEquals(IASString.fromString("son"), g1);
    }

}

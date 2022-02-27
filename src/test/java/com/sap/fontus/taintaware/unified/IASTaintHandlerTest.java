package com.sap.fontus.taintaware.unified;

import com.sap.fontus.agent.InstrumentationConfiguration;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class IASTaintHandlerTest {
    @BeforeAll
    static void before() {
        IASTaintSourceRegistry.getInstance().getOrRegisterObject("mySource");
        Configuration.setTestConfig(TaintMethod.RANGE);
        InstrumentationConfiguration.init(null, null);
        Configuration.getConfiguration().setRecursiveTainting(true);
    }

    @Test
    public void testRecursiveTaint() {
        IASString string = new IASString("test");
        A a = new A(string);

        IASTaintHandler.taint(a, null, null,1);

        Assertions.assertTrue(string.isTainted());
    }

    static class A {
        IASString string;
        A a;

        public A(IASString string) {
            this.string = string;
            this.a = this;
        }
    }

    @Test
    public void testTaintMap() {
        Map<String, Object> map = new HashMap<>();
        List<String> types = new ArrayList<>();

        types.add("element1");
        types.add("element2");
        types.add("element3");

        map.put("key", types);

        Object result = IASTaintHandler.taint(map, null, null,1);
        assertTrue(result instanceof Map);
    }

    @Test
    public void testTaintMapTaintedString() {
        Map<String, List<IASString>> map = new HashMap<>();
        List<IASString> types = new ArrayList<>();

        types.add(new IASString("element1"));
        types.add(new IASString("element2"));
        types.add(new IASString("element3"));

        map.put("key", types);

        Object result = IASTaintHandler.taint(map, null, null,1);
        assertTrue(result instanceof Map);
        Map<String, List<IASString>> resultMap = (Map<String, List<IASString>>) result;
        assertEquals(map, resultMap);
        assertTrue(resultMap.get("key").get(0).isTainted());
        assertEquals(1, resultMap.get("key").get(0).getTaintInformation().getTaint(0).getSource().getId());
    }

    @Test
    public void testTaintList() {
        List<String> types = new ArrayList<>();

        types.add("element1");
        types.add("element2");
        types.add("element3");

        Object result = IASTaintHandler.taint(types, null, null,1);
        assertTrue(result instanceof List);
    }

}

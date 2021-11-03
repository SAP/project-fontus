package com.sap.fontus.taintaware.unified;

import com.sap.fontus.taintaware.IASTaintAware;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class IASTaintHandlerTest {

    @Test
    public void testTaintMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        List<String> types = new ArrayList<String>();

        types.add("element1");
        types.add("element2");
        types.add("element3");

        map.put("key", types);

        Object result = IASTaintHandler.taint(map, 1);
        assertTrue(result instanceof Map);
    }

    @Test
    public void testTaintList() {
        List<String> types = new ArrayList<String>();

        types.add("element1");
        types.add("element2");
        types.add("element3");

        Object result = IASTaintHandler.taint(types, 1);
        assertTrue(result instanceof List);
    }
}

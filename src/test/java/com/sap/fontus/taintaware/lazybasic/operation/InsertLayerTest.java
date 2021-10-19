package com.sap.fontus.taintaware.lazybasic.operation;

import com.sap.fontus.taintaware.lazybasic.IASTaintInformation;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class InsertLayerTest {
    @Test
    public void testInsertEmpty() {
        int size = 10;
        IASTaintRanges previous = new IASTaintRanges(size);
        InsertLayer insertLayer = new InsertLayer(0, new IASTaintInformation(size, Arrays.asList(new IASTaintRange(0, size, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN))));

        IASTaintRanges result = insertLayer.apply(previous);

        assertArrayEquals(new IASTaintRange[]{new IASTaintRange(0, 10, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)},
                result.getTaintRanges().toArray(new IASTaintRange[0]));
    }

    @Test
    public void testInsertInTR() {
        int size = 10;
        IASTaintRanges previous = new IASTaintRanges(size, Arrays.asList(
                new IASTaintRange(0, size, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)
        ));
        InsertLayer insertLayer = new InsertLayer(5, new IASTaintInformation(5, Arrays.asList(new IASTaintRange(0, 5, IASTaintSourceRegistry.TS_STRING_CREATED_FROM_CHAR_ARRAY))));

        IASTaintRanges result = insertLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 5, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                        new IASTaintRange(5, 10, IASTaintSourceRegistry.TS_STRING_CREATED_FROM_CHAR_ARRAY),
                        new IASTaintRange(10, 15, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                },
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }

    @Test
    public void testInsertInTR2() {
        IASTaintRanges previous = new IASTaintRanges(10, Arrays.asList(
                new IASTaintRange(0, 7, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(7, 10, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)
        ));
        InsertLayer insertLayer = new InsertLayer(5, new IASTaintInformation(5, Arrays.asList(new IASTaintRange(0, 5, IASTaintSourceRegistry.TS_STRING_CREATED_FROM_CHAR_ARRAY))));

        IASTaintRanges result = insertLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 5, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                        new IASTaintRange(5, 10, IASTaintSourceRegistry.TS_STRING_CREATED_FROM_CHAR_ARRAY),
                        new IASTaintRange(10, 15, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)
                },
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }

    @Test
    public void testInsertInTR3() {
        IASTaintRanges previous = new IASTaintRanges(10, Arrays.asList(
                new IASTaintRange(0, 7, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(7, 10, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)
        ));
        InsertLayer insertLayer = new InsertLayer(5, new IASTaintInformation(5, Arrays.asList(new IASTaintRange(0, 5, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN))));

        IASTaintRanges result = insertLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 15, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)
                },
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }

    @Test
    public void testInsertCutOut() {
        IASTaintRanges previous = new IASTaintRanges(10, Arrays.asList(
                new IASTaintRange(0, 7, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(7, 10, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)
        ));
        InsertLayer insertLayer = new InsertLayer(5, new IASTaintInformation(5, Arrays.asList(new IASTaintRange(0, 5, IASTaintSourceRegistry.TS_CHAR_UNKNOWN_ORIGIN))));

        IASTaintRanges result = insertLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 5, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                        new IASTaintRange(5, 10, IASTaintSourceRegistry.TS_CHAR_UNKNOWN_ORIGIN),
                        new IASTaintRange(10, 15, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                },
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }
}

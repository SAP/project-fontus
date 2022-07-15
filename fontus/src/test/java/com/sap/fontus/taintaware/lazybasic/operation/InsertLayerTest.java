package com.sap.fontus.taintaware.lazybasic.operation;

import com.sap.fontus.taintaware.lazybasic.IASLayer;
import com.sap.fontus.taintaware.lazybasic.IASTaintInformation;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class InsertLayerTest {
    @Test
    void testInsertEmpty() {
        int size = 10;
        IASTaintRanges previous = new IASTaintRanges(size);
        IASLayer insertLayer = new InsertLayer(0, new IASTaintInformation(size, List.of(new IASTaintRange(0, size, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN))));

        IASTaintRanges result = insertLayer.apply(previous);

        assertArrayEquals(new IASTaintRange[]{new IASTaintRange(0, 10, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)},
                result.getTaintRanges().toArray(new IASTaintRange[0]));
    }

    @Test
    void testInsertInTR() {
        int size = 10;
        IASTaintRanges previous = new IASTaintRanges(size, List.of(
                new IASTaintRange(0, size, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)
        ));
        IASLayer insertLayer = new InsertLayer(5, new IASTaintInformation(5, List.of(new IASTaintRange(0, 5, IASTaintSourceRegistry.MD_STRING_CREATED_FROM_CHAR_ARRAY))));

        IASTaintRanges result = insertLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 5, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
                        new IASTaintRange(5, 10, IASTaintSourceRegistry.MD_STRING_CREATED_FROM_CHAR_ARRAY),
                        new IASTaintRange(10, 15, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
                },
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }

    @Test
    void testInsertInTR2() {
        IASTaintRanges previous = new IASTaintRanges(10, Arrays.asList(
                new IASTaintRange(0, 7, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(7, 10, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)
        ));
        IASLayer insertLayer = new InsertLayer(5, new IASTaintInformation(5, List.of(new IASTaintRange(0, 5, IASTaintSourceRegistry.MD_STRING_CREATED_FROM_CHAR_ARRAY))));

        IASTaintRanges result = insertLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 5, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
                        new IASTaintRange(5, 10, IASTaintSourceRegistry.MD_STRING_CREATED_FROM_CHAR_ARRAY),
                        new IASTaintRange(10, 15, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)
                },
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }

    @Test
    void testInsertInTR3() {
        IASTaintRanges previous = new IASTaintRanges(10, Arrays.asList(
                new IASTaintRange(0, 7, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(7, 10, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)
        ));
        IASLayer insertLayer = new InsertLayer(5, new IASTaintInformation(5, List.of(new IASTaintRange(0, 5, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN))));

        IASTaintRanges result = insertLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 15, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)
                },
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }

    @Test
    void testInsertCutOut() {
        IASTaintRanges previous = new IASTaintRanges(10, Arrays.asList(
                new IASTaintRange(0, 7, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(7, 10, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)
        ));
        IASLayer insertLayer = new InsertLayer(5, new IASTaintInformation(5, List.of(new IASTaintRange(0, 5, IASTaintSourceRegistry.MD_CHAR_UNKNOWN_ORIGIN))));

        IASTaintRanges result = insertLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 5, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
                        new IASTaintRange(5, 10, IASTaintSourceRegistry.MD_CHAR_UNKNOWN_ORIGIN),
                        new IASTaintRange(10, 15, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
                },
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }
}

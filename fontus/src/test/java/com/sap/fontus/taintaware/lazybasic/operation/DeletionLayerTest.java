package com.sap.fontus.taintaware.lazybasic.operation;

import com.sap.fontus.taintaware.lazybasic.IASLayer;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DeletionLayerTest {
    @Test
    void testDeleteEmpty() {
        IASTaintRanges previous = new IASTaintRanges(10);
        IASLayer deleteLayer = new DeleteLayer(0, 10);

        IASTaintRanges result = deleteLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{},
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }

    @Test
    void testDeleteInTR1() {
        IASTaintRanges previous = new IASTaintRanges(10, List.of(new IASTaintRange(0, 10, IASTaintSourceRegistry.MD_CHAR_UNKNOWN_ORIGIN)));
        IASLayer deleteLayer = new DeleteLayer(2, 5);

        IASTaintRanges result = deleteLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 7, IASTaintSourceRegistry.MD_CHAR_UNKNOWN_ORIGIN)
                },
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }

    @Test
    void testDeleteInTR2() {
        IASTaintRanges previous = new IASTaintRanges(10, Arrays.asList(
                new IASTaintRange(0, 5, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(5, 10, IASTaintSourceRegistry.MD_CHAR_UNKNOWN_ORIGIN)
        ));
        IASLayer deleteLayer = new DeleteLayer(3, 7);

        IASTaintRanges result = deleteLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 3, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
                        new IASTaintRange(3, 6, IASTaintSourceRegistry.MD_CHAR_UNKNOWN_ORIGIN)
                },
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }

    @Test
    void testDeleteInTR3() {
        IASTaintRanges previous = new IASTaintRanges(10, Arrays.asList(
                new IASTaintRange(0, 5, IASTaintSourceRegistry.MD_CHAR_UNKNOWN_ORIGIN),
                new IASTaintRange(5, 10, IASTaintSourceRegistry.MD_CHAR_UNKNOWN_ORIGIN)
        ));
        IASLayer deleteLayer = new DeleteLayer(3, 10);

        IASTaintRanges result = deleteLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 3, IASTaintSourceRegistry.MD_CHAR_UNKNOWN_ORIGIN),
                },
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }

    @Test
    void testDeleteInTR4() {
        IASTaintRanges previous = new IASTaintRanges(15, Arrays.asList(
                new IASTaintRange(0, 5, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(5, 10, IASTaintSourceRegistry.MD_CHAR_UNKNOWN_ORIGIN),
                new IASTaintRange(10, 15, IASTaintSourceRegistry.MD_STRING_CREATED_FROM_CHAR_ARRAY)
        ));
        IASLayer deleteLayer = new DeleteLayer(3, 13);

        IASTaintRanges result = deleteLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 3, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
                        new IASTaintRange(3, 5, IASTaintSourceRegistry.MD_STRING_CREATED_FROM_CHAR_ARRAY)
                },
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }

    @Test
    void testDeleteInTR5() {
        IASTaintRanges previous = new IASTaintRanges(10, List.of(new IASTaintRange(0, 5, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)));
        IASLayer deleteLayer = new DeleteLayer(0, 5);

        IASTaintRanges result = deleteLayer.apply(previous);

        assertEquals(0, result.getTaintRanges().size());
    }
}

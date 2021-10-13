package com.sap.fontus.taintaware.lazybasic.operation;

import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeletionLayerTest {
    @Test
    public void testDeleteEmpty() {
        IASTaintRanges previous = new IASTaintRanges(10);
        DeleteLayer deleteLayer = new DeleteLayer(0, 10);

        IASTaintRanges result = deleteLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{},
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }

    @Test
    public void testDeleteInTR1() {
        IASTaintRanges previous = new IASTaintRanges(10, Arrays.asList(new IASTaintRange(0, 10, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)));
        DeleteLayer deleteLayer = new DeleteLayer(2, 5);

        IASTaintRanges result = deleteLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 7, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)
                },
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }

    @Test
    public void testDeleteInTR2() {
        IASTaintRanges previous = new IASTaintRanges(10, Arrays.asList(
                new IASTaintRange(0, 5, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(5, 10, IASTaintSourceRegistry.TS_CHAR_UNKNOWN_ORIGIN)
        ));
        DeleteLayer deleteLayer = new DeleteLayer(3, 7);

        IASTaintRanges result = deleteLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 3, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                        new IASTaintRange(3, 6, IASTaintSourceRegistry.TS_CHAR_UNKNOWN_ORIGIN)
                },
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }

    @Test
    public void testDeleteInTR3() {
        IASTaintRanges previous = new IASTaintRanges(10, Arrays.asList(
                new IASTaintRange(0, 5, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(5, 10, IASTaintSourceRegistry.TS_CHAR_UNKNOWN_ORIGIN)
        ));
        DeleteLayer deleteLayer = new DeleteLayer(3, 10);

        IASTaintRanges result = deleteLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 3, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                },
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }

    @Test
    public void testDeleteInTR4() {
        IASTaintRanges previous = new IASTaintRanges(15, Arrays.asList(
                new IASTaintRange(0, 5, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(5, 10, IASTaintSourceRegistry.TS_CHAR_UNKNOWN_ORIGIN),
                new IASTaintRange(10, 15, IASTaintSourceRegistry.TS_STRING_CREATED_FROM_CHAR_ARRAY)
        ));
        DeleteLayer deleteLayer = new DeleteLayer(3, 13);

        IASTaintRanges result = deleteLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 3, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                        new IASTaintRange(3, 5, IASTaintSourceRegistry.TS_STRING_CREATED_FROM_CHAR_ARRAY)
                },
                result.getTaintRanges().toArray(new IASTaintRange[0])
        );
    }

    @Test
    public void testDeleteInTR5() {
        IASTaintRanges previous = new IASTaintRanges(10, Arrays.asList(new IASTaintRange(0, 5, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)));
        DeleteLayer deleteLayer = new DeleteLayer(0, 5);

        IASTaintRanges result = deleteLayer.apply(previous);

        assertEquals(0, result.getTaintRanges().size());
    }
}

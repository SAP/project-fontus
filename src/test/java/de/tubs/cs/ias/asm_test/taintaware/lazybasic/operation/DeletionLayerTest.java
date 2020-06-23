package de.tubs.cs.ias.asm_test.taintaware.lazybasic.operation;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeletionLayerTest {
    @Test
    public void testDeleteEmpty() {
        List<IASTaintRange> previous = new ArrayList<>();
        DeleteLayer deleteLayer = new DeleteLayer(0, 10);

        List<IASTaintRange> result = deleteLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{},
                result.toArray()
        );
    }

    @Test
    public void testDeleteInTR1() {
        List<IASTaintRange> previous = Arrays.asList(new IASTaintRange(0, 10, IASTaintSource.TS_CS_UNKNOWN_ORIGIN));
        DeleteLayer deleteLayer = new DeleteLayer(2, 5);

        List<IASTaintRange> result = deleteLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 7, IASTaintSource.TS_CS_UNKNOWN_ORIGIN)
                },
                result.toArray()
        );
    }

    @Test
    public void testDeleteInTR2() {
        List<IASTaintRange> previous = Arrays.asList(
                new IASTaintRange(0, 5, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(5, 10, IASTaintSource.TS_CHAR_UNKNOWN_ORIGIN)
        );
        DeleteLayer deleteLayer = new DeleteLayer(3, 7);

        List<IASTaintRange> result = deleteLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 3, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                        new IASTaintRange(3, 6, IASTaintSource.TS_CHAR_UNKNOWN_ORIGIN)
                },
                result.toArray()
        );
    }

    @Test
    public void testDeleteInTR3() {
        List<IASTaintRange> previous = Arrays.asList(
                new IASTaintRange(0, 5, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(5, 10, IASTaintSource.TS_CHAR_UNKNOWN_ORIGIN)
        );
        DeleteLayer deleteLayer = new DeleteLayer(3);

        List<IASTaintRange> result = deleteLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 3, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                },
                result.toArray()
        );
    }

    @Test
    public void testDeleteInTR4() {
        List<IASTaintRange> previous = Arrays.asList(
                new IASTaintRange(0, 5, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(5, 10, IASTaintSource.TS_CHAR_UNKNOWN_ORIGIN),
                new IASTaintRange(10, 15, IASTaintSource.TS_STRING_CREATED_FROM_CHAR_ARRAY)
        );
        DeleteLayer deleteLayer = new DeleteLayer(3, 13);

        List<IASTaintRange> result = deleteLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 3, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                        new IASTaintRange(3, 5, IASTaintSource.TS_STRING_CREATED_FROM_CHAR_ARRAY)
                },
                result.toArray()
        );
    }

    @Test
    public void testDeleteInTR5() {
        List<IASTaintRange> previous = Arrays.asList(new IASTaintRange(0, 5, IASTaintSource.TS_CS_UNKNOWN_ORIGIN));
        DeleteLayer deleteLayer = new DeleteLayer(0, 5);

        List<IASTaintRange> result = deleteLayer.apply(previous);

        assertEquals(0, result.size());
    }
}

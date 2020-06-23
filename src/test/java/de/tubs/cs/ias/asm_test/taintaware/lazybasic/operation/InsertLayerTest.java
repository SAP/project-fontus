package de.tubs.cs.ias.asm_test.taintaware.lazybasic.operation;

import de.tubs.cs.ias.asm_test.taintaware.lazybasic.IASTaintInformation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class InsertLayerTest {
    @Test
    public void testInsertEmpty() {
        List<IASTaintRange> previous = new ArrayList<>();
        InsertLayer insertLayer = new InsertLayer(0, 10, IASTaintSource.TS_CS_UNKNOWN_ORIGIN);

        List<IASTaintRange> result = insertLayer.apply(previous);

        assertArrayEquals(new IASTaintRange[]{new IASTaintRange(0, 10, IASTaintSource.TS_CS_UNKNOWN_ORIGIN)},
                result.toArray());
    }

    @Test
    public void testInsertInTR() {
        List<IASTaintRange> previous = Arrays.asList(
                new IASTaintRange(0, 10, IASTaintSource.TS_CS_UNKNOWN_ORIGIN)
        );
        InsertLayer insertLayer = new InsertLayer(5, 10, IASTaintSource.TS_STRING_CREATED_FROM_CHAR_ARRAY);

        List<IASTaintRange> result = insertLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 5, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                        new IASTaintRange(5, 10, IASTaintSource.TS_STRING_CREATED_FROM_CHAR_ARRAY),
                        new IASTaintRange(10, 15, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                },
                result.toArray()
        );
    }

    @Test
    public void testInsertInTR2() {
        List<IASTaintRange> previous = Arrays.asList(
                new IASTaintRange(0, 7, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(7, 10, IASTaintSource.TS_CS_UNKNOWN_ORIGIN)
        );
        InsertLayer insertLayer = new InsertLayer(5, 10, IASTaintSource.TS_STRING_CREATED_FROM_CHAR_ARRAY);

        List<IASTaintRange> result = insertLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 5, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                        new IASTaintRange(5, 10, IASTaintSource.TS_STRING_CREATED_FROM_CHAR_ARRAY),
                        new IASTaintRange(10, 15, IASTaintSource.TS_CS_UNKNOWN_ORIGIN)
                },
                result.toArray()
        );
    }

    @Test
    public void testInsertInTR3() {
        List<IASTaintRange> previous = Arrays.asList(
                new IASTaintRange(0, 7, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(7, 10, IASTaintSource.TS_CS_UNKNOWN_ORIGIN)
        );
        InsertLayer insertLayer = new InsertLayer(5, 10, IASTaintSource.TS_CS_UNKNOWN_ORIGIN);

        List<IASTaintRange> result = insertLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 15, IASTaintSource.TS_CS_UNKNOWN_ORIGIN)
                },
                result.toArray()
        );
    }

    @Test
    public void testInsertCutOut() {
        List<IASTaintRange> previous = Arrays.asList(
                new IASTaintRange(0, 7, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(7, 10, IASTaintSource.TS_CS_UNKNOWN_ORIGIN)
        );
        InsertLayer insertLayer = new InsertLayer(5, 10, new IASTaintInformation(new BaseLayer(0, 10, IASTaintSource.TS_CHAR_UNKNOWN_ORIGIN)));

        List<IASTaintRange> result = insertLayer.apply(previous);

        assertArrayEquals(
                new IASTaintRange[]{
                        new IASTaintRange(0, 5, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                        new IASTaintRange(5, 10, IASTaintSource.TS_CHAR_UNKNOWN_ORIGIN),
                        new IASTaintRange(10, 15, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                },
                result.toArray()
        );
    }
}

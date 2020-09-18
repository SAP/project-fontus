package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSourceRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SplitOperationTest {
    @Test
    public void testCompletelyTainted() {
        String before = "bye,bye,bye";
        List<IASTaintRange> ranges = Collections.singletonList(new IASTaintRange(0, before.length(), IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN));
        IASOperation operation1 = new SplitOperation(",", 0, 0);
        IASOperation operation2 = new SplitOperation(",", 1, 0);
        IASOperation operation3 = new SplitOperation(",", 2, 0);

        List<IASTaintRange> result1 = operation1.apply(before, new ArrayList<>(ranges));
        List<IASTaintRange> result2 = operation2.apply(before, new ArrayList<>(ranges));
        List<IASTaintRange> result3 = operation3.apply(before, new ArrayList<>(ranges));

        assertEquals(1, result1.size());
        assertEquals(1, result2.size());
        assertEquals(1, result3.size());
        assertEquals(new IASTaintRange(0, 3, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN), result1.get(0));
        assertEquals(new IASTaintRange(0, 3, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN), result2.get(0));
        assertEquals(new IASTaintRange(0, 3, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN), result3.get(0));
    }

    @Test
    public void testPartlyTainted() {
        String before = "bye,bye,bye";
        List<IASTaintRange> ranges = Collections.singletonList(new IASTaintRange(8, before.length(), IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN));
        IASOperation operation1 = new SplitOperation(",", 0, 0);
        IASOperation operation2 = new SplitOperation(",", 1, 0);
        IASOperation operation3 = new SplitOperation(",", 2, 0);

        List<IASTaintRange> result1 = operation1.apply(before, new ArrayList<>(ranges));
        List<IASTaintRange> result2 = operation2.apply(before, new ArrayList<>(ranges));
        List<IASTaintRange> result3 = operation3.apply(before, new ArrayList<>(ranges));

        assertEquals(0, result1.size());
        assertEquals(0, result2.size());
        assertEquals(1, result3.size());
        assertEquals(new IASTaintRange(0, 3, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN), result3.get(0));
    }

    @Test
    public void testNotTainted() {
        String before = "bye,bye,bye";
        List<IASTaintRange> ranges = Collections.emptyList();
        IASOperation operation1 = new SplitOperation(",", 0, 0);
        IASOperation operation2 = new SplitOperation(",", 1, 0);
        IASOperation operation3 = new SplitOperation(",", 2, 0);

        List<IASTaintRange> result1 = operation1.apply(before, new ArrayList<>(ranges));
        List<IASTaintRange> result2 = operation2.apply(before, new ArrayList<>(ranges));
        List<IASTaintRange> result3 = operation3.apply(before, new ArrayList<>(ranges));

        assertEquals(0, result1.size());
        assertEquals(0, result2.size());
        assertEquals(0, result3.size());
    }

    @Test
    public void testPartlyTaintedWithLimit() {
        String before = "bye,bye,bye";
        List<IASTaintRange> ranges = Collections.singletonList(new IASTaintRange(8, before.length(), IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN));
        IASOperation operation1 = new SplitOperation(",", 0, 2);
        IASOperation operation2 = new SplitOperation(",", 1, 2);

        List<IASTaintRange> result1 = operation1.apply(before, new ArrayList<>(ranges));
        List<IASTaintRange> result2 = operation2.apply(before, new ArrayList<>(ranges));

        assertEquals(0, result1.size());
        assertEquals(1, result2.size());
        assertEquals(new IASTaintRange(4, 7, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN), result2.get(0));
    }
}

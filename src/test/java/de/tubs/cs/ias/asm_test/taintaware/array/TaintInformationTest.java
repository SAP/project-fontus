package de.tubs.cs.ias.asm_test.taintaware.array;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaintInformationTest {
    @Test
    public void testSetTaint1() {
        IASTaintInformation ti = new IASTaintInformation(5);

        ti.setTaint(0, 5, 1);

        assertTrue(ti.isInitialized());
        assertTrue(ti.isTainted());
        assertArrayEquals(new int[]{1, 1, 1, 1, 1}, ti.getTaints());
    }

    @Test
    public void testSetTaint2() {
        IASTaintInformation ti = new IASTaintInformation(5);

        ti.setTaint(1, 4, 1);

        assertTrue(ti.isInitialized());
        assertTrue(ti.isTainted());
        assertArrayEquals(new int[]{0, 1, 1, 1, 0}, ti.getTaints());
    }

    @Test
    public void testSetTaint3() {
        IASTaintInformation ti = new IASTaintInformation(0);

        ti.setTaint(0, 5, 1);

        assertTrue(ti.isInitialized());
        assertTrue(ti.isTainted());
        assertArrayEquals(new int[]{1, 1, 1, 1, 1}, ti.getTaints());
    }

    @Test
    public void testSetTaint4() {
        IASTaintInformation ti = new IASTaintInformation(0);

        ti.setTaint(1, 4, 1);

        assertTrue(ti.isInitialized());
        assertTrue(ti.isTainted());
        assertArrayEquals(new int[]{0, 1, 1, 1}, ti.getTaints());
    }

    @Test
    public void testSetTaint5() {
        IASTaintInformation ti = new IASTaintInformation(3);

        ti.setTaint(0, 5, 1);

        assertTrue(ti.isInitialized());
        assertTrue(ti.isTainted());
        assertArrayEquals(new int[]{1, 1, 1, 1, 1}, ti.getTaints());
    }

    @Test
    public void testSetTaint6() {
        IASTaintInformation ti = new IASTaintInformation(3);

        ti.setTaint(1, 4, 1);

        assertTrue(ti.isInitialized());
        assertTrue(ti.isTainted());
        assertArrayEquals(new int[]{0, 1, 1, 1}, ti.getTaints());
    }

    @Test
    public void testSetTaint7() {
        IASTaintInformation ti = new IASTaintInformation(new int[]{2, 2, 2, 2, 2});

        ti.setTaint(0, 5, 1);

        assertTrue(ti.isInitialized());
        assertTrue(ti.isTainted());
        assertArrayEquals(new int[]{1, 1, 1, 1, 1}, ti.getTaints());
    }

    @Test
    public void testSetTaint8() {
        IASTaintInformation ti = new IASTaintInformation(new int[]{2, 2, 2, 2, 2});

        ti.setTaint(1, 4, 1);

        assertTrue(ti.isInitialized());
        assertTrue(ti.isTainted());
        assertArrayEquals(new int[]{2, 1, 1, 1, 2}, ti.getTaints());
    }

    @Test
    public void testGetTaint1() {
        IASTaintInformation ti = new IASTaintInformation(0);

        int[] taint = ti.getTaints();

        assertArrayEquals(new int[0], taint);
    }

    @Test
    public void testGetTaint2() {
        IASTaintInformation ti = new IASTaintInformation(1);

        int[] taint = ti.getTaints();

        assertArrayEquals(new int[1], taint);
    }

    @Test
    public void testGetTaint3() {
        IASTaintInformation ti = new IASTaintInformation(new int[]{1, 2, 3});

        int[] taint = ti.getTaints();

        assertArrayEquals(new int[]{1, 2, 3}, taint);
    }

    @Test
    public void testGetTaintByIndex1() {
        IASTaintInformation ti = new IASTaintInformation(0);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            ti.getTaints(0, 1);
        });
    }

    @Test
    public void testGetTaintByIndex2() {
        IASTaintInformation ti = new IASTaintInformation(3);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            ti.getTaints(1, 5);
        });
    }

    @Test
    public void testGetTaintByIndex3() {
        IASTaintInformation ti = new IASTaintInformation(new int[]{1, 2, 3, 4, 5});

        int[] taint = ti.getTaints(1, 4);

        assertArrayEquals(new int[]{2, 3, 4}, taint);
    }
}

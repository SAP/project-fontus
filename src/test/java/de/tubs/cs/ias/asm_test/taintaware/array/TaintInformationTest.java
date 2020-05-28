package de.tubs.cs.ias.asm_test.taintaware.array;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}

package com.sap.fontus.taintaware.array;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaintInformationTest {

    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.ARRAY);
    }

    private final static IASTaintMetadata md0 = null;
    private final static IASTaintMetadata md1 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterTaintSource("dummy"));
    private final static IASTaintMetadata md2 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterTaintSource("dummy2"));
    private final static IASTaintMetadata md3 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterTaintSource("dummy3"));
    private final static IASTaintMetadata md4 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterTaintSource("dummy4"));
    private final static IASTaintMetadata md5 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterTaintSource("dummy5"));

    @Test
    public void testSetTaint1() {
        IASTaintInformation ti = new IASTaintInformation(5);

        ti.setTaint(0, 5, md1);

        assertTrue(ti.isTainted());
        assertArrayEquals(new IASTaintMetadata[]{md1, md1, md1, md1, md1}, ti.getTaints());
    }

    @Test
    public void testSetTaint2() {
        IASTaintInformation ti = new IASTaintInformation(5);

        ti.setTaint(1, 4, md1);

        assertTrue(ti.isTainted());
        assertArrayEquals(new IASTaintMetadata[]{md0, md1, md1, md1, md0}, ti.getTaints());
    }

    @Test
    public void testSetTaint3() {
        IASTaintInformation ti = new IASTaintInformation(0);

        ti.setTaint(0, 5, md1);

        assertTrue(ti.isTainted());
        assertArrayEquals(new IASTaintMetadata[]{md1, md1, md1, md1, md1}, ti.getTaints());
    }

    @Test
    public void testSetTaint4() {
        IASTaintInformation ti = new IASTaintInformation(0);

        ti.setTaint(1, 4, md1);

        assertTrue(ti.isTainted());
        assertArrayEquals(new IASTaintMetadata[]{md0, md1, md1, md1}, ti.getTaints());
    }

    @Test
    public void testSetTaint5() {
        IASTaintInformation ti = new IASTaintInformation(3);

        ti.setTaint(0, 5, md1);

        assertTrue(ti.isTainted());
        assertArrayEquals(new IASTaintMetadata[]{md1, md1, md1, md1, md1}, ti.getTaints());
    }

    @Test
    public void testSetTaint6() {
        IASTaintInformation ti = new IASTaintInformation(3);

        ti.setTaint(1, 4, md2);

        assertTrue(ti.isTainted());
        assertArrayEquals(new IASTaintMetadata[]{md0, md2, md2, md2}, ti.getTaints());
    }

    @Test
    public void testSetTaint7() {
        IASTaintInformation ti = new IASTaintInformation(new IASTaintMetadata[]{md2, md2, md2, md2, md2});

        ti.setTaint(0, 5, md1);

        assertTrue(ti.isTainted());
        assertArrayEquals(new IASTaintMetadata[]{md1, md1, md1, md1, md1}, ti.getTaints());
    }

    @Test
    public void testSetTaint8() {
        IASTaintInformation ti = new IASTaintInformation(new IASTaintMetadata[]{md2, md2, md2, md2, md2});

        ti.setTaint(1, 4, md1);

        assertTrue(ti.isTainted());
        assertArrayEquals(new IASTaintMetadata[]{md2, md1, md1, md1, md2}, ti.getTaints());
    }

    @Test
    public void testGetTaint1() {
        IASTaintInformation ti = new IASTaintInformation(0);

        IASTaintMetadata[] taint = ti.getTaints();

        assertArrayEquals(new IASTaintMetadata[0], taint);
    }

    @Test
    public void testGetTaint2() {
        IASTaintInformation ti = new IASTaintInformation(1);

        IASTaintMetadata[] taint = ti.getTaints();

        assertArrayEquals(new IASTaintMetadata[1], taint);
    }

    @Test
    public void testGetTaint3() {
        IASTaintInformation ti = new IASTaintInformation(new IASTaintMetadata[]{md1, md2, md3});

        IASTaintMetadata[] taint = ti.getTaints();

        assertArrayEquals(new IASTaintMetadata[]{md1, md2, md3}, taint);
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
        IASTaintInformation ti = new IASTaintInformation(new IASTaintMetadata[]{md1, md2, md3, md4, md5});

        IASTaintMetadata[] taint = ti.getTaints(1, 4);

        assertArrayEquals(new IASTaintMetadata[]{md2, md3, md4}, taint);
    }

    @Test
    public void testResize1() {
        IASTaintInformation ti = new IASTaintInformation(0);

        ti.resize(5);

        assertEquals(5, ti.getLength());
    }

    @Test
    public void testResize2() {
        IASTaintInformation ti = new IASTaintInformation(2);

        ti.resize(5);

        assertEquals(5, ti.getLength());
    }

    @Test
    public void testResize3() {
        IASTaintInformation ti = new IASTaintInformation(new IASTaintMetadata[]{md0, md0});

        ti.resize(5);

        assertEquals(5, ti.getLength());
        assertArrayEquals(new IASTaintMetadata[]{md0, md0, md0, md0, md0}, ti.getTaints());
    }

    @Test
    public void testResize4() {
        IASTaintInformation ti = new IASTaintInformation(new IASTaintMetadata[]{md1, md2});

        ti.resize(5);

        assertEquals(5, ti.getLength());
        assertArrayEquals(new IASTaintMetadata[]{md1, md2, md0, md0, md0}, ti.getTaints());
    }

    @Test
    public void testResize5() {
        IASTaintInformation ti = new IASTaintInformation(new IASTaintMetadata[]{md1, md2, md3, md4, md5});

        ti.resize(2);

        assertEquals(2, ti.getLength());
        assertArrayEquals(new IASTaintMetadata[]{md1, md2}, ti.getTaints());
    }

    @Test
    public void testResize6() {
        IASTaintInformation ti = new IASTaintInformation(5);

        ti.resize(2);

        assertEquals(2, ti.getLength());
    }

    @Test
    public void testRemoveAll1() {
        IASTaintInformation ti = new IASTaintInformation(0);

        ti.clearTaint(0,0);

        assertFalse(ti.isTainted());
    }

    @Test
    public void testRemoveAll2() {
        IASTaintInformation ti = new IASTaintInformation(3);

        ti.clearTaint(0, 3);

        assertFalse(ti.isTainted());
    }

    @Test
    public void testRemoveAll3() {
        IASTaintInformation ti = new IASTaintInformation(new IASTaintMetadata[]{md1, md2, md3});

        ti.clearTaint(0, 3);

        assertFalse(ti.isTainted());
    }

    @Test
    public void testInsert1() {
        IASTaintInformation ti = new IASTaintInformation(0);

        ti.insertTaint(5, new IASTaintMetadata[]{md1, md2, md3});

        assertArrayEquals(new IASTaintMetadata[]{md0, md0, md0, md0, md0, md1, md2, md3}, ti.getTaints());
    }

    @Test
    public void testInsert2() {
        IASTaintInformation ti = new IASTaintInformation(new IASTaintMetadata[]{md0, md0, md0, md0, md0});

        ti.insertTaint(5, new IASTaintMetadata[]{md1, md2, md3});

        assertArrayEquals(new IASTaintMetadata[]{md0, md0, md0, md0, md0, md1, md2, md3}, ti.getTaints());
    }

    @Test
    public void testInsert3() {
        IASTaintInformation ti = new IASTaintInformation(new IASTaintMetadata[]{md0, md0, md0});

        ti.insertTaint(2, new IASTaintMetadata[]{md1, md2, md3});

        assertArrayEquals(new IASTaintMetadata[]{md0, md0, md1, md2, md3, md0}, ti.getTaints());
    }

    @Test
    public void testRemoveTaintFor1() {
        IASTaintInformation ti = new IASTaintInformation(new IASTaintMetadata[]{md1, md2, md3, md4, md5});

        ti.deleteWithShift(1, 4);

        assertArrayEquals(new IASTaintMetadata[]{md1, md5}, ti.getTaints());
    }

    @Test
    public void testRemoveTaintFor3() {
        IASTaintInformation ti = new IASTaintInformation(5);

        ti.deleteWithShift(1, 4);

        assertFalse(ti.isTainted());
    }

    @Test
    public void testRemoveTaintFor4() {
        IASTaintInformation ti = new IASTaintInformation(5);

        ti.deleteWithShift(1, 4);

        assertFalse(ti.isTainted());
    }

    @Test
    public void testRemoveTaintFor5() {
        IASTaintInformation ti = new IASTaintInformation(0);

        assertThrows(IndexOutOfBoundsException.class, () -> ti.deleteWithShift(1, 4));
    }

    @Test
    public void testRemoveTaintFor6() {
        IASTaintInformation ti = new IASTaintInformation(0);

        assertThrows(IndexOutOfBoundsException.class, () -> ti.deleteWithShift(1, 4));
    }
}

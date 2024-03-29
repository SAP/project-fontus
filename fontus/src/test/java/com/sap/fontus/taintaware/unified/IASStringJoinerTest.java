package com.sap.fontus.taintaware.unified;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.helper.RangeChainer;
import com.sap.fontus.taintaware.helper.TaintMatcher;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.*;

class IASStringJoinerTest {

    private static final IASTaintMetadata md1 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterObject("dummy"));
    private static final IASTaintMetadata md2 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterObject("dummy2"));

    @BeforeAll
    static void setup() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    @Test
    void testStringJoiner() {
        IASStringJoiner joiner = new IASStringJoiner(",");
        joiner.add(new IASString("hello"));
        joiner.add(new IASString("world"));
        joiner.add(new IASString("goodbye"));

        assertEquals("hello,world,goodbye", joiner.toString());
    }

    @Test
    void testStringJoinerUntainted() {
        IASStringJoiner joiner = new IASStringJoiner(",");
        joiner.add(new IASString("hello"));
        joiner.add(new IASString("world"));
        joiner.add(new IASString("goodbye"));

        IASString taintedString = joiner.toIASString();

        assertEquals("hello,world,goodbye", taintedString.getString());
        assertFalse(taintedString.isTainted());
    }

    @Test
    void testStringJoinerTainted() {
        IASStringJoiner joiner = new IASStringJoiner(",");
        joiner.add(new IASString("hello"));
        IASString world = new IASString("world");
        world.setTaint(md1);
        joiner.add(world);
        joiner.add(new IASString("goodbye"));

        IASString taintedString = joiner.toIASString();

        assertEquals("hello,world,goodbye", taintedString.getString());
        assertTrue(taintedString.isTainted());
        MatcherAssert.assertThat(taintedString, TaintMatcher.taintEquals(RangeChainer.range(6, 11, md1)));
    }

    @Test
    void testStringJoinerDelimiterTainted() {
        IASString delimiter = new IASString(",");
        delimiter.setTaint(md1);
        IASStringJoiner joiner = new IASStringJoiner(delimiter);
        joiner.add(new IASString("hello"));
        joiner.add(new IASString("world"));
        joiner.add(new IASString("goodbye"));

        IASString taintedString = joiner.toIASString();

        assertEquals("hello,world,goodbye", taintedString.getString());
        assertTrue(taintedString.isTainted());
        MatcherAssert.assertThat(taintedString, TaintMatcher.taintEquals(
                RangeChainer.range(5, 6, md1)
                        .add(11, 12, md1)));
    }

    @Test
    void testStringJoinerPrefixTainted() {
        IASString prefix = new IASString("[");
        prefix.setTaint(md1);
        IASString suffix = new IASString("]");
        suffix.setTaint(md2);

        IASStringJoiner joiner = new IASStringJoiner(",", prefix, suffix);
        joiner.add(new IASString("hello"));
        joiner.add(new IASString("world"));
        joiner.add(new IASString("goodbye"));

        IASString taintedString = joiner.toIASString();

        assertEquals("[hello,world,goodbye]", taintedString.getString());
        assertTrue(taintedString.isTainted());
        MatcherAssert.assertThat(taintedString, TaintMatcher.taintEquals(
                RangeChainer.range(0, 1, md1)
                        .add(20, 21, md2)));
    }

    @Test
    void testStringEmptyTainted() {
        IASString def = new IASString("default");
        def.setTaint(md1);

        IASStringJoiner joiner = new IASStringJoiner(",");
        joiner.setEmptyValue(def);
        IASString taintedString = joiner.toIASString();

        assertEquals("default", taintedString.getString());
        assertTrue(taintedString.isTainted());
        MatcherAssert.assertThat(taintedString, TaintMatcher.taintEquals(
                RangeChainer.range(0, 7, md1)));
    }

    @Test
    void testStringEmptyNotDefined() {
        IASStringJoiner joiner = new IASStringJoiner(",");
        IASString taintedString = joiner.toIASString();

        assertEquals("", taintedString.getString());
        assertFalse(taintedString.isTainted());
    }

    @Test
    void testFromStringJoiner() {
        StringJoiner joiner = new StringJoiner(",");
        joiner.add(new IASString("hello"));
        joiner.add(new IASString("world"));
        joiner.add(new IASString("goodbye"));

        IASStringJoiner taintedJoiner = IASStringJoiner.fromStringJoiner(joiner);

        IASString taintedString = taintedJoiner.toIASString();
        assertEquals("hello,world,goodbye", taintedString.getString());
        assertFalse(taintedString.isTainted());
    }

    @Test
    void testStringJoinerMergeTainted() {
        IASStringJoiner joiner = new IASStringJoiner(",");
        joiner.add(new IASString("hello"));
        IASString world = new IASString("world");
        world.setTaint(md1);
        joiner.add(world);
        joiner.add(new IASString("goodbye"));

        IASStringJoiner joiner2 = new IASStringJoiner(";");
        joiner2.add(new IASString("hola"));
        IASString adios = new IASString("adios");
        adios.setTaint(md2);
        joiner2.add(adios);

        joiner.merge(joiner2);

        IASString taintedString = joiner.toIASString();

        assertEquals("hello,world,goodbye,hola;adios", taintedString.getString());
        assertTrue(taintedString.isTainted());
        MatcherAssert.assertThat(taintedString, TaintMatcher.taintEquals(
                RangeChainer.range(6, 11, md1)
                        .add(25, 30, md2)));
    }
}

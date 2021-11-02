package com.sap.fontus.taintaware.range;

import com.sap.fontus.taintaware.range.testHelper.HelperUtils;
import com.sap.fontus.taintaware.range.testHelper.RangeChainer;
import com.sap.fontus.taintaware.range.testHelper.THelper;
import com.sap.fontus.taintaware.range.testHelper.TaintMatcher;
import com.sap.fontus.TaintStringHelper;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASStringBuffer;
import com.sap.fontus.taintaware.unified.IASStringBuilder;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.EnableJUnit4MigrationSupport;

import static com.sap.fontus.taintaware.range.testHelper.RangeChainer.range;
import static com.sap.fontus.taintaware.range.testHelper.TaintMatcher.taintEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;


/**
 * This class contains test code regarding StringBuilder and therefore covers the code contained
 * in AbstractStringBuilder. Because of this (same parent) most functionality of StringBuffer is also tested in here.
 * <p>
 * StringBuilder actually just overrides the methods contained in AbstractStringBuilder in order to be able to
 * return its own type for method chaining. Solely toString() is abstract and is therefore implemented in StringBuilder.java itself.
 */
@EnableJUnit4MigrationSupport
@SuppressWarnings("ALL")
// David: As I didn't write this Code and don't want to mess with it I suppressed the warnings.
// TODO: Work out whether we can adapt it to the style of the remaining project?
public class StringBufferTest {

    private final static IASTaintMetadata md0 = null;
    private final static IASTaintMetadata md1 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterTaintSource("dummy"));
    private final static IASTaintMetadata md2 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterTaintSource("dummy2"));
    private final static IASTaintMetadata md3 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterTaintSource("dummy3"));
    private final static IASTaintMetadata md4 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterTaintSource("dummy4"));
    private final static IASTaintMetadata md5 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterTaintSource("dummy5"));

    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    private IASStringBuffer foo = null;
    private IASStringBuffer bar = null;

    @BeforeEach
    public void setup() {
        foo = new IASStringBuffer("foo");
        bar = new IASStringBuffer("bar");
    }

    @Test
    public void toString_1() {
        ((IASTaintInformation) THelper.get(foo)).addRange(1, 2, md3);

        IASString str = foo.toIASString();

        assertThat(str.toString(), is("foo"));
        MatcherAssert.assertThat(str, TaintMatcher.taintEquals(RangeChainer.range(1, 2, md3)));
    }

    @Test
    public void capacity_1() {
        IASString s = new IASString("test string");

        IASStringBuilder sbuilder = new IASStringBuilder(s);

        IASStringBuffer sbuffer = new IASStringBuffer(sbuilder);

        assertEquals(16 + sbuilder.length(), sbuffer.capacity());
    }

    @Test
    public void setTaint_test() {
        IASStringBuffer sb1 = new IASStringBuffer("hello");

        TaintStringHelper.setTaint(sb1, false);
        assertFalse(TaintStringHelper.isTainted(sb1));

        TaintStringHelper.setTaint(sb1, true);
        assertTrue(TaintStringHelper.isTainted(sb1));


        IASStringBuffer sb2 = new IASStringBuffer("hello");

        TaintStringHelper.setTaint(sb2, true);
        assertTrue(TaintStringHelper.isTainted(sb2));

        TaintStringHelper.setTaint(sb2, false);
        assertFalse(TaintStringHelper.isTainted(sb2));
    }

    @Test
    public void toString_2() {
        assertThat(foo.toIASString(), TaintMatcher.taintUninitialized());
    }

    @Test
    public void setLength_1() {
        IASStringBuffer sb = new IASStringBuffer("Hello World!");

        ((IASTaintInformation) THelper.get(sb)).addRange(0, 4, md3).addRange(4, 6, md1).addRange(6, 12, md2);

        sb.setLength(5);

        assertThat(sb.toString(), is("Hello"));
        MatcherAssert.assertThat(sb, TaintMatcher.taintEquals(RangeChainer.range(0, 4, md3).add(4, 5, md1)));
    }

    @Test
    public void setLength_2() {
        // Range of size 0 should be removed
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 2, md3).addRange(2, 3, md1);

        foo.setLength(2);

        assertThat(foo.toString(), is("fo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 2, md3)));
    }

    @Test
    public void setLength_3() {
        // Until now range will get end < start (end: 1, start: 2), which is absolutely wrong
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 1, md3).addRange(2, 3, md1);

        foo.setLength(1);

        assertThat(foo.toString(), is("f"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3)));
    }

    @Test
    public void setCharAt() {
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, md3);
        foo.setCharAt(1, 'l');

        assertThat(foo.toString(), is("flo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(2, 3, md3).done()));
    }

    @Test
    public void append_string() {
        IASString s = new IASString("bar");

        ((IASTaintInformation) THelper.get(s)).addRange(0, 4, md3); // 4 is actually to long, so let's see whether it gets cut down ;)

        foo.append(s);

        assertThat(foo.toString(), is("foobar"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(3, 6, md3)));
    }

    @Test
    public void append_stringBuffer() {
        IASStringBuffer sb = new IASStringBuffer("bar");

        ((IASTaintInformation) THelper.get(sb)).addRange(0, 4, md3); // 4 is actually to long, so let's see whether it gets cut down ;)

        foo.append(sb);

        assertThat(foo.toString(), is("foobar"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(3, 6, md3)));
    }

    @Test
    public void append_charSequence_1() {
        // append subtype of AbstractStringBuilder
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 1, md3);
        ((IASTaintInformation) THelper.get(bar)).addRange(0, 3, md1);

        foo.append(bar, 0, 2);

        assertThat(foo.toString(), is("fooba"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(3, 5, md1).done()));
    }

    @Test
    public void append_charSequence_2() {
        // append an unknown implementation of CharSequence
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 1, md3);

        foo.append(HelperUtils.createCharSequence("blub"), 1, 3);

        assertThat(foo.toString(), is("foolu"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).done()));
    }

    @Test
    public void append_charSequence_3() {
        // append an untainted String
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 1, md3);

        foo.append("blub", 1, 4);

        assertThat(foo.toString(), is("foolub"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3)));
    }

    @Test
    public void append_charArray() {
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, md3);
        char[] arr = new char[]{'b', 'a', 'r'};

        foo.append(arr);

        assertThat(foo.toString(), is("foobar"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 3, md3)));
    }

    @Test
    public void append_charArray_range() {
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, md3);
        char[] arr = new char[]{'b', 'a', 'r'};

        foo.append(arr, 1, 1);

        assertThat(foo.toString(), is("fooa"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 3, md3)));
    }

    @Test
    public void append_char() {
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, md3);
        foo.append('o');

        assertThat(foo.toString(), is("fooo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 3, md3)));
    }

    @Test
    public void delete() {
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 1, md3).
                addRange(1, 3, md1);

        foo.delete(1, 2);

        assertThat(foo.toString(), is("fo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(1, 2, md1).done()));
    }

    @Test
    public void deleteCharAt() {
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 1, md3).
                addRange(1, 3, md1);

        foo.deleteCharAt(1);

        assertThat(foo.toString(), is("fo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(1, 2, md1).done()));
    }

    @Test
    public void replace_1() {
        IASStringBuffer sb = new IASStringBuffer("hello world!");

        ((IASTaintInformation) THelper.get(sb)).addRange(0, 12, md3);
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, md1);

        sb.replace(2, 3, foo.toIASString());

        assertThat(sb.toString(), is("hefoolo world!"));
        MatcherAssert.assertThat(sb, TaintMatcher.taintEquals(RangeChainer.range(0, 2, md3).add(2, 5, md1).add(5, 14, md3)));
    }

    @Test
    public void replace_2() {
        IASStringBuffer sb = new IASStringBuffer("hello world!");

        ((IASTaintInformation) THelper.get(sb)).addRange(0, 12, md3);

        sb.replace(1, 11, new IASString(""));

        assertThat(sb.toString(), is("h!"));
        MatcherAssert.assertThat(sb, TaintMatcher.taintEquals(RangeChainer.range(0, 2, md3)));
    }

    @Test
    public void substring() {
        IASStringBuffer sb = new IASStringBuffer("hello world!");

        ((IASTaintInformation) THelper.get(sb)).addRange(0, 8, md3).addRange(8, 12, md1);

        IASString sub1 = sb.substring(1);
        IASString sub2 = sb.substring(7, 9);

        assertThat(sub1.toString(), is("ello world!"));
        MatcherAssert.assertThat(sub1, TaintMatcher.taintEquals(RangeChainer.range(0, 7, md3).add(7, 11, md1)));

        assertThat(sub2.toString(), is("or"));
        MatcherAssert.assertThat(sub2, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(1, 2, md1)));
    }

    @Test
    public void insert_charArray_partially() {
        char[] ar = "bar".toCharArray();

        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, md3);

        foo.insert(1, ar, 1, 2);

        assertThat(foo.toString(), is("faroo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(3, 5, md3)));
    }

    @Test
    public void insert_string_1() {
        IASString str = new IASString("bar");

        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, md3);
        ((IASTaintInformation) THelper.get(str)).addRange(0, 3, md1);

        foo.insert(1, str);

        assertThat(foo.toString(), is("fbaroo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(1, 4, md1).add(4, 6, md3)));
    }

    @Test
    public void insert_string_2() {
        IASString str = new IASString("bar");

        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, md3);

        foo.insert(1, str);

        assertThat(foo.toString(), is("fbaroo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(4, 6, md3)));
    }

    @Test
    public void insert_charArray() {
        char[] ar = new IASString("bar").toCharArray();

        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, md3);

        foo.insert(1, ar);

        assertThat(foo.toString(), is("fbaroo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(4, 6, md3)));
    }

    @Test
    public void insert_charSequence_1() {
        // insert subtype of AbstractStringBuilder
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, md3);
        ((IASTaintInformation) THelper.get(bar)).addRange(0, 3, md1);

        foo.insert(1, bar, 0, 2);

        assertThat(foo.toString(), is("fbaoo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(1, 3, md1).add(3, 5, md3)));
    }

    @Test
    public void insert_charSequence_2() {
        // insert an unknown implementation of CharSequence
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, md3);
        IASStringBuffer foo2 = new IASStringBuffer(foo);

        foo.insert(0, HelperUtils.createCharSequence("blub"));
        foo2.insert(1, HelperUtils.createCharSequence("blub"));

        assertThat(foo.toString(), is("blubfoo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(4, 7, md3)));

        assertThat(foo2.toString(), is("fbluboo"));
        MatcherAssert.assertThat(foo2, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(5, 7, md3)));
    }

    @Test
    public void insert_charSequence_3() {
        // insert an untainted String
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 1, md3);

        foo.insert(3, "blub", 0, 4); // we need 0 and 4 as parameters, even if they are meaningless, but otherwise compile would take insert(offset, String) instead

        assertThat(foo.toString(), is("fooblub"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3)));
    }

    @Test
    public void insert_charSequence_4() {
        // insert an empty range into an untainted StringBuffer
        foo.insert(0, "blub", 0, 0);

        assertThat(foo.toString(), is("foo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintUninitialized());
    }

    @Test
    public void insert_char() {
        // insert an untainted String
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, md3);

        IASStringBuffer foo2 = new IASStringBuffer(foo);

        foo.insert(0, 'u');
        foo2.insert(1, 'u');

        assertThat(foo.toString(), is("ufoo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(1, 4, md3)));

        assertThat(foo2.toString(), is("fuoo"));
        MatcherAssert.assertThat(foo2, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(2, 4, md3)));
    }

    @Test
    public void reverse_1() {
        // just Unicode characters contained in BMP
        IASStringBuffer sb = new IASStringBuffer("hello world!");

        ((IASTaintInformation) THelper.get(sb)).addRange(0, 8, md3).addRange(8, 12, md1);

        sb.reverse();

        assertThat(sb.toString(), is("!dlrow olleh"));
        MatcherAssert.assertThat(sb, TaintMatcher.taintEquals(RangeChainer.range(0, 4, md1).add(4, 12, md3)));
    }

    @Test
    public void reverse_2() {
        // now with characters not contained in BMP (and therefore represented by a surrogate pair)
        // these surrogate pairs, as a result, are taken as one unit for the process of reversing
        // For an example of such a character: https://www.compart.com/de/unicode/U+10000
        IASStringBuffer sb1 = new IASStringBuffer("a\uD800\uDC00b");
        IASStringBuffer sb2 = new IASStringBuffer("a\uD800\uDC00b");
        IASStringBuffer sb3 = new IASStringBuffer("a\uD800\uDC00b");
        IASStringBuffer sb4 = new IASStringBuffer("a\uD800\uDC00b");

        ((IASTaintInformation) THelper.get(sb1)).addRange(0, 2, md3);
        ((IASTaintInformation) THelper.get(sb2)).addRange(2, 4, md3);
        ((IASTaintInformation) THelper.get(sb3)).addRange(0, 2, md3).addRange(2, 4, md1);
        ((IASTaintInformation) THelper.get(sb4)).addRange(0, 3, md3);

        sb1.reverse();
        sb2.reverse();
        sb3.reverse();
        sb4.reverse();

        assertThat(sb1.toString(), is("b\uD800\uDC00a"));
        MatcherAssert.assertThat(sb1, TaintMatcher.taintEquals(RangeChainer.range(1, 2, md3).add(3, 4, md3)));

        assertThat(sb2.toString(), is("b\uD800\uDC00a"));
        MatcherAssert.assertThat(sb2, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(2, 3, md3)));

        assertThat(sb3.toString(), is("b\uD800\uDC00a"));
        MatcherAssert.assertThat(sb3, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md1).add(1, 2, md3).add(2, 3, md1).add(3, 4, md3)));

        assertThat(sb4.toString(), is("b\uD800\uDC00a"));
        MatcherAssert.assertThat(sb4, TaintMatcher.taintEquals(RangeChainer.range(1, 4, md3)));
    }
}

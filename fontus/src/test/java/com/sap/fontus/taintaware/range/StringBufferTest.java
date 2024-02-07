package com.sap.fontus.taintaware.range;

import com.sap.fontus.TaintStringHelper;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.helper.HelperUtils;
import com.sap.fontus.taintaware.helper.RangeChainer;
import com.sap.fontus.taintaware.helper.THelper;
import com.sap.fontus.taintaware.helper.TaintMatcher;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASStringBuffer;
import com.sap.fontus.taintaware.unified.IASStringBuilder;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.EnableJUnit4MigrationSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


/**
 * This class contains test code regarding StringBuilder and therefore covers the code contained
 * in AbstractStringBuilder. Because of this (same parent) most functionality of StringBuffer is also tested in here.
 * <p>
 * StringBuilder actually just overrides the methods contained in AbstractStringBuilder in order to be able to
 * return its own type for method chaining. Solely toString() is abstract and is therefore implemented in StringBuilder.java itself.
 */
@EnableJUnit4MigrationSupport
class StringBufferTest {

    private static final IASTaintMetadata md0 = null;
    private static final IASTaintMetadata md1 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterObject("dummy"));
    private static final IASTaintMetadata md2 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterObject("dummy2"));
    private static final IASTaintMetadata md3 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterObject("dummy3"));
    private static final IASTaintMetadata md4 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterObject("dummy4"));
    private static final IASTaintMetadata md5 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterObject("dummy5"));

    @BeforeAll
    static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    private IASStringBuffer foo;
    private IASStringBuffer bar;

    @BeforeEach
    void setup() {
        this.foo = new IASStringBuffer("foo");
        this.bar = new IASStringBuffer("bar");
    }

    @Test
    void toString_1() {
        THelper.get(this.foo).addRange(1, 2, md3);

        IASString str = this.foo.toIASString();

        assertThat(str.toString(), is("foo"));
        assertThat(str, TaintMatcher.taintEquals(RangeChainer.range(1, 2, md3)));
    }

    @Test
    void capacity_1() {
        IASString s = new IASString("test string");

        IASStringBuilder sbuilder = new IASStringBuilder(s);

        IASStringBuffer sbuffer = new IASStringBuffer(sbuilder);

        assertEquals(16 + sbuilder.length(), sbuffer.capacity());
    }

    @Test
    void setTaint_test() {
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
    void toString_2() {
        assertThat(this.foo.toIASString(), TaintMatcher.taintUninitialized());
    }

    @Test
    void setLength_1() {
        IASStringBuffer sb = new IASStringBuffer("Hello World!");

        THelper.get(sb).addRange(0, 4, md3).addRange(4, 6, md1).addRange(6, 12, md2);

        sb.setLength(5);

        assertThat(sb.toString(), is("Hello"));
        assertThat(sb, TaintMatcher.taintEquals(RangeChainer.range(0, 4, md3).add(4, 5, md1)));
    }

    @Test
    void setLength_2() {
        // Range of size 0 should be removed
        THelper.get(this.foo).addRange(0, 2, md3).addRange(2, 3, md1);

        this.foo.setLength(2);

        assertThat(this.foo.toString(), is("fo"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(0, 2, md3)));
    }

    @Test
    void setLength_3() {
        // Until now range will get end < start (end: 1, start: 2), which is absolutely wrong
        THelper.get(this.foo).addRange(0, 1, md3).addRange(2, 3, md1);

        this.foo.setLength(1);

        assertThat(this.foo.toString(), is("f"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3)));
    }

    @Test
    void setCharAt() {
        THelper.get(this.foo).addRange(0, 3, md3);
        this.foo.setCharAt(1, 'l');

        assertThat(this.foo.toString(), is("flo"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(2, 3, md3).done()));
    }

    @Test
    void append_string() {
        IASString s = new IASString("bar");

        THelper.get(s).addRange(0, 4, md3); // 4 is actually to long, so let's see whether it gets cut down ;)

        this.foo.append(s);

        assertThat(this.foo.toString(), is("foobar"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(3, 6, md3)));
    }

    @Test
    void append_stringBuffer() {
        IASStringBuffer sb = new IASStringBuffer("bar");

        THelper.get(sb).addRange(0, 4, md3); // 4 is actually to long, so let's see whether it gets cut down ;)

        this.foo.append(sb);

        assertThat(this.foo.toString(), is("foobar"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(3, 6, md3)));
    }

    @Test
    void append_charSequence_1() {
        // append subtype of AbstractStringBuilder
        THelper.get(this.foo).addRange(0, 1, md3);
        THelper.get(this.bar).addRange(0, 3, md1);

        this.foo.append(this.bar, 0, 2);

        assertThat(this.foo.toString(), is("fooba"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(3, 5, md1).done()));
    }

    @Test
    void append_charSequence_2() {
        // append an unknown implementation of CharSequence
        THelper.get(this.foo).addRange(0, 1, md3);

        this.foo.append(HelperUtils.createCharSequence("blub"), 1, 3);

        assertThat(this.foo.toString(), is("foolu"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).done()));
    }

    @Test
    void append_charSequence_3() {
        // append an untainted String
        THelper.get(this.foo).addRange(0, 1, md3);

        this.foo.append("blub", 1, 4);

        assertThat(this.foo.toString(), is("foolub"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3)));
    }

    @Test
    void append_charArray() {
        THelper.get(this.foo).addRange(0, 3, md3);
        char[] arr = {'b', 'a', 'r'};

        this.foo.append(arr);

        assertThat(this.foo.toString(), is("foobar"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(0, 3, md3)));
    }

    @Test
    void append_charArray_range() {
        THelper.get(this.foo).addRange(0, 3, md3);
        char[] arr = {'b', 'a', 'r'};

        this.foo.append(arr, 1, 1);

        assertThat(this.foo.toString(), is("fooa"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(0, 3, md3)));
    }

    @Test
    void append_char() {
        THelper.get(this.foo).addRange(0, 3, md3);
        this.foo.append('o');

        assertThat(this.foo.toString(), is("fooo"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(0, 3, md3)));
    }

    @Test
    void delete() {
        THelper.get(this.foo).addRange(0, 1, md3).
                addRange(1, 3, md1);

        this.foo.delete(1, 2);

        assertThat(this.foo.toString(), is("fo"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(1, 2, md1).done()));
    }

    @Test
    void deleteCharAt() {
        THelper.get(this.foo).addRange(0, 1, md3).
                addRange(1, 3, md1);

        this.foo.deleteCharAt(1);

        assertThat(this.foo.toString(), is("fo"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(1, 2, md1).done()));
    }

    @Test
    void replace_1() {
        IASStringBuffer sb = new IASStringBuffer("hello world!");

        THelper.get(sb).addRange(0, 12, md3);
        THelper.get(this.foo).addRange(0, 3, md1);

        sb.replace(2, 3, this.foo.toIASString());

        assertThat(sb.toString(), is("hefoolo world!"));
        assertThat(sb, TaintMatcher.taintEquals(RangeChainer.range(0, 2, md3).add(2, 5, md1).add(5, 14, md3)));
    }

    @Test
    void replace_2() {
        IASStringBuffer sb = new IASStringBuffer("hello world!");

        THelper.get(sb).addRange(0, 12, md3);

        sb.replace(1, 11, new IASString(""));

        assertThat(sb.toString(), is("h!"));
        assertThat(sb, TaintMatcher.taintEquals(RangeChainer.range(0, 2, md3)));
    }

    @Test
    void substring() {
        IASStringBuffer sb = new IASStringBuffer("hello world!");

        THelper.get(sb).addRange(0, 8, md3).addRange(8, 12, md1);

        IASString sub1 = sb.substring(1);
        IASString sub2 = sb.substring(7, 9);

        assertThat(sub1.toString(), is("ello world!"));
        assertThat(sub1, TaintMatcher.taintEquals(RangeChainer.range(0, 7, md3).add(7, 11, md1)));

        assertThat(sub2.toString(), is("or"));
        assertThat(sub2, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(1, 2, md1)));
    }

    @Test
    void insert_charArray_partially() {
        char[] ar = "bar".toCharArray();

        THelper.get(this.foo).addRange(0, 3, md3);

        this.foo.insert(1, ar, 1, 2);

        assertThat(this.foo.toString(), is("faroo"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(3, 5, md3)));
    }

    @Test
    void insert_string_1() {
        IASString str = new IASString("bar");

        THelper.get(this.foo).addRange(0, 3, md3);
        THelper.get(str).addRange(0, 3, md1);

        this.foo.insert(1, str);

        assertThat(this.foo.toString(), is("fbaroo"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(1, 4, md1).add(4, 6, md3)));
    }

    @Test
    void insert_string_2() {
        IASString str = new IASString("bar");

        THelper.get(this.foo).addRange(0, 3, md3);

        this.foo.insert(1, str);

        assertThat(this.foo.toString(), is("fbaroo"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(4, 6, md3)));
    }

    @Test
    void insert_charArray() {
        char[] ar = new IASString("bar").toCharArray();

        THelper.get(this.foo).addRange(0, 3, md3);

        this.foo.insert(1, ar);

        assertThat(this.foo.toString(), is("fbaroo"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(4, 6, md3)));
    }

    @Test
    void insert_charSequence_1() {
        // insert subtype of AbstractStringBuilder
        THelper.get(this.foo).addRange(0, 3, md3);
        THelper.get(this.bar).addRange(0, 3, md1);

        this.foo.insert(1, this.bar, 0, 2);

        assertThat(this.foo.toString(), is("fbaoo"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(1, 3, md1).add(3, 5, md3)));
    }

    @Test
    void insert_charSequence_2() {
        // insert an unknown implementation of CharSequence
        THelper.get(this.foo).addRange(0, 3, md3);
        IASStringBuffer foo2 = new IASStringBuffer(this.foo);

        this.foo.insert(0, HelperUtils.createCharSequence("blub"));
        foo2.insert(1, HelperUtils.createCharSequence("blub"));

        assertThat(this.foo.toString(), is("blubfoo"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(4, 7, md3)));

        assertThat(foo2.toString(), is("fbluboo"));
        assertThat(foo2, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(5, 7, md3)));
    }

    @Test
    void insert_charSequence_3() {
        // insert an untainted String
        THelper.get(this.foo).addRange(0, 1, md3);

        this.foo.insert(3, "blub", 0, 4); // we need 0 and 4 as parameters, even if they are meaningless, but otherwise compile would take insert(offset, String) instead

        assertThat(this.foo.toString(), is("fooblub"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3)));
    }

    @Test
    void insert_charSequence_4() {
        // insert an empty range into an untainted StringBuffer
        this.foo.insert(0, "blub", 0, 0);

        assertThat(this.foo.toString(), is("foo"));
        assertThat(this.foo, TaintMatcher.taintUninitialized());
    }

    @Test
    void insert_char() {
        // insert an untainted String
        THelper.get(this.foo).addRange(0, 3, md3);

        IASStringBuffer foo2 = new IASStringBuffer(this.foo);

        this.foo.insert(0, 'u');
        foo2.insert(1, 'u');

        assertThat(this.foo.toString(), is("ufoo"));
        assertThat(this.foo, TaintMatcher.taintEquals(RangeChainer.range(1, 4, md3)));

        assertThat(foo2.toString(), is("fuoo"));
        assertThat(foo2, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(2, 4, md3)));
    }

    @Test
    void reverse_1() {
        // just Unicode characters contained in BMP
        IASStringBuffer sb = new IASStringBuffer("hello world!");

        THelper.get(sb).addRange(0, 8, md3).addRange(8, 12, md1);

        sb.reverse();

        assertThat(sb.toString(), is("!dlrow olleh"));
        assertThat(sb, TaintMatcher.taintEquals(RangeChainer.range(0, 4, md1).add(4, 12, md3)));
    }

    @Test
    void reverse_2() {
        // now with characters not contained in BMP (and therefore represented by a surrogate pair)
        // these surrogate pairs, as a result, are taken as one unit for the process of reversing
        // For an example of such a character: https://www.compart.com/de/unicode/U+10000
        IASStringBuffer sb1 = new IASStringBuffer("a\uD800\uDC00b");
        IASStringBuffer sb2 = new IASStringBuffer("a\uD800\uDC00b");
        IASStringBuffer sb3 = new IASStringBuffer("a\uD800\uDC00b");
        IASStringBuffer sb4 = new IASStringBuffer("a\uD800\uDC00b");

        THelper.get(sb1).addRange(0, 2, md3);
        THelper.get(sb2).addRange(2, 4, md3);
        THelper.get(sb3).addRange(0, 2, md3).addRange(2, 4, md1);
        THelper.get(sb4).addRange(0, 3, md3);

        sb1.reverse();
        sb2.reverse();
        sb3.reverse();
        sb4.reverse();

        assertThat(sb1.toString(), is("b\uD800\uDC00a"));
        assertThat(sb1, TaintMatcher.taintEquals(RangeChainer.range(1, 2, md3).add(3, 4, md3)));

        assertThat(sb2.toString(), is("b\uD800\uDC00a"));
        assertThat(sb2, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md3).add(2, 3, md3)));

        assertThat(sb3.toString(), is("b\uD800\uDC00a"));
        assertThat(sb3, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md1).add(1, 2, md3).add(2, 3, md1).add(3, 4, md3)));

        assertThat(sb4.toString(), is("b\uD800\uDC00a"));
        assertThat(sb4, TaintMatcher.taintEquals(RangeChainer.range(1, 4, md3)));
    }
}

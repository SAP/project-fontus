package com.sap.fontus.taintaware.range;

import com.sap.fontus.taintaware.range.testHelper.HelperUtils;
import com.sap.fontus.taintaware.range.testHelper.RangeChainer;
import com.sap.fontus.taintaware.range.testHelper.THelper;
import com.sap.fontus.taintaware.range.testHelper.TaintMatcher;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
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
public class StringBuilderTest {

    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    private IASStringBuilder foo = null;
    private IASStringBuilder bar = null;

    @BeforeEach
    public void setup() {
        foo = new IASStringBuilder("foo");
        bar = new IASStringBuilder("bar");
    }

    @Test
    public void toString_1() {
        ((IASTaintInformation) THelper.get(foo)).addRange(1, 2, 3);

        IASString str = foo.toIASString();

        assertThat(str.toString(), is("foo"));
        MatcherAssert.assertThat(str, TaintMatcher.taintEquals(RangeChainer.range(1, 2, 3)));
    }

    @Test
    public void toString_2() {
        assertThat(foo.toIASString(), TaintMatcher.taintUninitialized());
    }

    @Test
    public void setLength_1() {
        IASStringBuilder sb = new IASStringBuilder("Hello World!");

        ((IASTaintInformation) THelper.get(sb)).addRange(0, 4, 3).addRange(4, 6, 1).addRange(6, 12, 2);

        sb.setLength(5);

        assertThat(sb.toString(), is("Hello"));
        MatcherAssert.assertThat(sb, TaintMatcher.taintEquals(RangeChainer.range(0, 4, 3).add(4, 5, 1)));
    }

    @Test
    public void setLength_2() {
        // Range of size 0 should be removed
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 2, 3).addRange(2, 3, 1);

        foo.setLength(2);

        assertThat(foo.toString(), is("fo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 2, 3)));
    }

    @Test
    public void setLength_3() {
        // Until now range will get end < start (end: 1, start: 2), which is absolutely wrong
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 1, 3).addRange(2, 3, 1);

        foo.setLength(1);

        assertThat(foo.toString(), is("f"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 3)));
    }

    @Test
    public void setCharAt() {
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, 3);
        foo.setCharAt(1, 'l');

        assertThat(foo.toString(), is("flo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 3).add(2, 3, 3).done()));
    }

    @Test
    public void append_string() {
        IASString s = new IASString("bar");

        ((IASTaintInformation) THelper.get(s)).addRange(0, 4, 3); // 4 is actually to long, so let's see whether it gets cut down ;)

        foo.append(s);

        assertThat(foo.toString(), is("foobar"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(3, 6, 3)));
    }

    @Test
    public void append_stringBuffer() {
        IASStringBuffer sb = new IASStringBuffer("bar");

        ((IASTaintInformation) THelper.get(sb)).addRange(0, 4, 3); // 4 is actually to long, so let's see whether it gets cut down ;)

        foo.append(sb);

        assertThat(foo.toString(), is("foobar"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(3, 6, 3)));
    }

    @Test
    public void append_charSequence_1() {
        // append subtype of AbstractStringBuilder
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 1, 3);
        ((IASTaintInformation) THelper.get(bar)).addRange(0, 3, 1);

        foo.append(bar, 0, 2);

        assertThat(foo.toString(), is("fooba"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 3).add(3, 5, 1)));
    }

    @Test
    public void append_charSequence_2() {
        // append an unknown implementation of CharSequence
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 1, 3);

        foo.append(HelperUtils.createCharSequence("blub"), 1, 3);

        assertThat(foo.toString(), is("foolu"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 3).done()));
    }

    @Test
    public void append_charSequence_3() {
        // append an untainted String
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 1, 3);

        foo.append("blub", 1, 4);

        assertThat(foo.toString(), is("foolub"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 3)));
    }

    @Test
    public void append_charArray() {
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, 3);
        char[] arr = new char[]{'b', 'a', 'r'};

        foo.append(arr);

        assertThat(foo.toString(), is("foobar"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 3, 3)));
    }

    @Test
    public void append_charArray_range() {
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, 3);
        char[] arr = new char[]{'b', 'a', 'r'};

        foo.append(arr, 1, 1);

        assertThat(foo.toString(), is("fooa"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 3, 3)));
    }

    @Test
    public void append_char() {
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, 3);
        foo.append('o');

        assertThat(foo.toString(), is("fooo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 3, 3)));
    }

    @Test
    public void delete() {
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 1, 3).
                addRange(1, 3, 1);

        foo.delete(1, 2);

        assertThat(foo.toString(), is("fo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 3).add(1, 2, 1)));
    }

    @Test
    public void deleteCharAt() {
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 1, 3).
                addRange(1, 3, 1);

        foo.deleteCharAt(1);

        assertThat(foo.toString(), is("fo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 3).add(1, 2, 1)));
    }

    @Test
    public void replace_1() {
        IASStringBuilder sb = new IASStringBuilder("hello world!");

        ((IASTaintInformation) THelper.get(sb)).addRange(0, 12, 3);
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, 1);

        sb.replace(2, 3, foo.toIASString());

        assertThat(sb.toString(), is("hefoolo world!"));
        MatcherAssert.assertThat(sb, TaintMatcher.taintEquals(RangeChainer.range(0, 2, 3).add(2, 5, 1).add(5, 14, 3)));
    }

    @Test
    public void replace_2() {
        IASStringBuilder sb = new IASStringBuilder("hello world!");

        ((IASTaintInformation) THelper.get(sb)).addRange(0, 12, 3);

        sb.replace(1, 11, new IASString(""));

        assertThat(sb.toString(), is("h!"));
        MatcherAssert.assertThat(sb, TaintMatcher.taintEquals(RangeChainer.range(0, 2, 3)));
    }

    @Test
    public void substring() {
        IASStringBuilder sb = new IASStringBuilder("hello world!");

        ((IASTaintInformation) THelper.get(sb)).addRange(0, 8, 3).addRange(8, 12, 1);

        IASString sub1 = sb.substring(1);
        IASString sub2 = sb.substring(7, 9);

        assertThat(sub1.toString(), is("ello world!"));
        MatcherAssert.assertThat(sub1, TaintMatcher.taintEquals(RangeChainer.range(0, 7, 3).add(7, 11, 1)));

        assertThat(sub2.toString(), is("or"));
        MatcherAssert.assertThat(sub2, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 3).add(1, 2, 1)));
    }

    @Test
    public void insert_charArray_partially() {
        char[] ar = "bar".toCharArray();

        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, 3);

        foo.insert(1, ar, 1, 2);

        assertThat(foo.toString(), is("faroo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 3).add(3, 5, 3)));
    }

    @Test
    public void insert_string_1() {
        IASString str = new IASString("bar");

        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, 3);
        ((IASTaintInformation) THelper.get(str)).addRange(0, 3, 1);

        foo.insert(1, str);

        assertThat(foo.toString(), is("fbaroo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 3).add(1, 4, 1).add(4, 6, 3)));
    }

    @Test
    public void insert_string_2() {
        IASString str = new IASString("bar");

        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, 1);

        foo.insert(1, str);

        assertThat(foo.toString(), is("fbaroo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 1).add(4, 6, 1)));
    }

    @Test
    public void insert_charArray() {
        char[] ar = new IASString("bar").toCharArray();

        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, 1);

        foo.insert(1, ar);

        assertThat(foo.toString(), is("fbaroo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 1).add(4, 6, 1)));
    }

    @Test
    public void insert_charSequence_1() {
        // insert subtype of AbstractStringBuilder
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, 1);
        ((IASTaintInformation) THelper.get(bar)).addRange(0, 3, 2);

        foo.insert(1, bar, 0, 2);

        assertThat(foo.toString(), is("fbaoo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 1).add(1, 3, 2).add(3, 5, 1)));
    }

    @Test
    public void insert_charSequence_2() {
        // insert an unknown implementation of CharSequence
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, 3);
        IASStringBuffer foo2 = new IASStringBuffer(foo);

        foo.insert(0, HelperUtils.createCharSequence("blub"));
        foo2.insert(1, HelperUtils.createCharSequence("blub"));

        assertThat(foo.toString(), is("blubfoo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(4, 7, 3)));

        assertThat(foo2.toString(), is("fbluboo"));
        MatcherAssert.assertThat(foo2, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 3).add(5, 7, 3)));
    }

    @Test
    public void insert_charSequence_3() {
        // insert an untainted String
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 1, 3);

        foo.insert(3, "blub", 0, 4); // we need 0 and 4 as parameters, even if they are meaningless, but otherwise compile would take insert(offset, String) instead

        assertThat(foo.toString(), is("fooblub"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 3)));
    }

    @Test
    public void insert_charSequence_4() {
        // insert an empty range into an untainted StringBuilder
        foo.insert(0, "blub", 0, 0);

        assertThat(foo.toString(), is("foo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintUninitialized());
    }

    @Test
    public void insert_char() {
        // insert an untainted String
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, 3);

        IASStringBuffer foo2 = new IASStringBuffer(foo);

        foo.insert(0, 'u');
        foo2.insert(1, 'u');

        assertThat(foo.toString(), is("ufoo"));
        MatcherAssert.assertThat(foo, TaintMatcher.taintEquals(RangeChainer.range(1, 4, 3)));

        assertThat(foo2.toString(), is("fuoo"));
        MatcherAssert.assertThat(foo2, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 3).add(2, 4, 3)));
    }

    @Test
    public void reverse_1() {
        // just Unicode characters contained in BMP
        IASStringBuilder sb = new IASStringBuilder("hello world!");

        ((IASTaintInformation) THelper.get(sb)).addRange(0, 8, 3).addRange(8, 12, 1);

        sb.reverse();

        assertThat(sb.toString(), is("!dlrow olleh"));
        MatcherAssert.assertThat(sb, TaintMatcher.taintEquals(RangeChainer.range(0, 4, 1).add(4, 12, 3)));
    }

    @Test
    public void reverse_2() {
        // now with characters not contained in BMP (and therefore represented by a surrogate pair)
        // these surrogate pairs, as a result, are taken as one unit for the process of reversing
        // For an example of such a character: https://www.compart.com/de/unicode/U+10000
        IASStringBuilder sb1 = new IASStringBuilder("a\uD800\uDC00b");
        IASStringBuilder sb2 = new IASStringBuilder("a\uD800\uDC00b");
        IASStringBuilder sb3 = new IASStringBuilder("a\uD800\uDC00b");
        IASStringBuilder sb4 = new IASStringBuilder("a\uD800\uDC00b");

        ((IASTaintInformation) THelper.get(sb1)).addRange(0, 2, 3);
        ((IASTaintInformation) THelper.get(sb2)).addRange(2, 4, 3);
        ((IASTaintInformation) THelper.get(sb3)).addRange(0, 2, 3).addRange(2, 4, 1);
        ((IASTaintInformation) THelper.get(sb4)).addRange(0, 3, 3);

        sb1.reverse();
        sb2.reverse();
        sb3.reverse();
        sb4.reverse();

        assertThat(sb1.toString(), is("b\uD800\uDC00a"));
        MatcherAssert.assertThat(sb1, TaintMatcher.taintEquals(RangeChainer.range(1, 2, 3).add(3, 4, 3)));

        assertThat(sb2.toString(), is("b\uD800\uDC00a"));
        MatcherAssert.assertThat(sb2, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 3).add(2, 3, 3)));

        assertThat(sb3.toString(), is("b\uD800\uDC00a"));
        MatcherAssert.assertThat(sb3, TaintMatcher.taintEquals(RangeChainer.range(0, 1, 1).add(1, 2, 3).add(2, 3, 1).add(3, 4, 3)));

        assertThat(sb4.toString(), is("b\uD800\uDC00a"));
        MatcherAssert.assertThat(sb4, TaintMatcher.taintEquals(RangeChainer.range(1, 4, 3)));
    }
}

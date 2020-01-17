package de.tubs.cs.ias.asm_test.taintaware.range;

import de.tubs.cs.ias.asm_test.taintaware.range.testHelper.THelper;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.EnableJUnit4MigrationSupport;

import static de.tubs.cs.ias.asm_test.taintaware.range.testHelper.HelperUtils.createCharSequence;
import static de.tubs.cs.ias.asm_test.taintaware.range.testHelper.RangeChainer.range;
import static de.tubs.cs.ias.asm_test.taintaware.range.testHelper.TaintMatcher.taintEquals;
import static de.tubs.cs.ias.asm_test.taintaware.range.testHelper.TaintMatcher.taintUninitialized;
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
public class StringBufferTest {
    private IASStringBuffer foo = null;
    private IASStringBuffer bar = null;

    @BeforeEach
    public void setup() {
        foo = new IASStringBuffer("foo");
        bar = new IASStringBuffer("bar");
    }

    @Test
    public void toString_1() {
        THelper.get(foo).addRange(1, 2, (short) 0);

        IASString str = foo.toIASString();

        assertThat(str.toString(), is("foo"));
        assertThat(str, taintEquals(range(1, 2, 0)));
    }

    @Test
    public void toString_2() {
        assertThat(foo.toIASString(), taintUninitialized());
    }

    @Test
    public void setLength_1() {
        IASStringBuffer sb = new IASStringBuffer("Hello World!");

        THelper.get(sb).appendRanges(range(0, 4, 0).add(4, 6, 1).add(6, 12, 2).done());

        sb.setLength(5);

        assertThat(sb.toString(), is("Hello"));
        assertThat(sb, taintEquals(range(0, 4, 0).add(4, 5, 1)));
    }

    @Test
    public void setLength_2() {
        // Range of size 0 should be removed
        THelper.get(foo).appendRanges(range(0, 2, 0).add(2, 3, 1).done());

        foo.setLength(2);

        assertThat(foo.toString(), is("fo"));
        assertThat(foo, taintEquals(range(0, 2, 0)));
    }

    @Test
    public void setLength_3() {
        // Until now range will get end < start (end: 1, start: 2), which is absolutely wrong
        THelper.get(foo).appendRanges(range(0, 1, 0).add(2, 3, 1).done());

        foo.setLength(1);

        assertThat(foo.toString(), is("f"));
        assertThat(foo, taintEquals(range(0, 1, 0)));
    }

    @Test
    public void setCharAt() {
        THelper.get(foo).addRange(0, 3, (short) 0);
        foo.setCharAt(1, 'l');

        assertThat(foo.toString(), is("flo"));
        assertThat(foo, taintEquals(range(0, 1, 0).add(2, 3, 0).done()));
    }

    @Test
    public void append_string() {
        IASString s = new IASString("bar");

        THelper.get(s).addRange(0, 4, (short) 0); // 4 is actually to long, so let's see whether it gets cut down ;)

        foo.append(s);

        assertThat(foo.toString(), is("foobar"));
        assertThat(foo, taintEquals(range(3, 6, 0)));
    }

    @Test
    public void append_stringBuffer() {
        IASStringBuffer sb = new IASStringBuffer("bar");

        THelper.get(sb).addRange(0, 4, (short) 0); // 4 is actually to long, so let's see whether it gets cut down ;)

        foo.append(sb);

        assertThat(foo.toString(), is("foobar"));
        assertThat(foo, taintEquals(range(3, 6, 0)));
    }

    @Test
    public void append_charSequence_1() {
        // append subtype of AbstractStringBuilder
        THelper.get(foo).addRange(0, 1, (short) 0);
        THelper.get(bar).addRange(0, 3, (short) 1);

        foo.append(bar, 0, 2);

        assertThat(foo.toString(), is("fooba"));
        assertThat(foo, taintEquals(range(0, 1, 0).add(3, 5, 1).done()));
    }

    @Test
    public void append_charSequence_2() {
        // append an unknown implementation of CharSequence
        THelper.get(foo).addRange(0, 1, (short) 0);

        foo.append(createCharSequence("blub"), 1, 3);

        assertThat(foo.toString(), is("foolu"));
        assertThat(foo, taintEquals(range(0, 1, 0).done()));
    }

    @Test
    public void append_charSequence_3() {
        // append an untainted String
        THelper.get(foo).addRange(0, 1, (short) 0);

        foo.append("blub", 1, 4);

        assertThat(foo.toString(), is("foolub"));
        assertThat(foo, taintEquals(range(0, 1, 0)));
    }

    @Test
    public void append_charArray() {
        THelper.get(foo).addRange(0, 3, (short) 0);
        char[] arr = new char[]{'b', 'a', 'r'};

        foo.append(arr);

        assertThat(foo.toString(), is("foobar"));
        assertThat(foo, taintEquals(range(0, 3, 0)));
    }

    @Test
    public void append_charArray_range() {
        THelper.get(foo).addRange(0, 3, (short) 0);
        char[] arr = new char[]{'b', 'a', 'r'};

        foo.append(arr, 1, 1);

        assertThat(foo.toString(), is("fooa"));
        assertThat(foo, taintEquals(range(0, 3, 0)));
    }

    @Test
    public void append_char() {
        foo.getTaintInformation().addRange(0, 3, (short) 0);
        foo.append('o');

        assertThat(foo.toString(), is("fooo"));
        assertThat(foo, taintEquals(range(0, 3, 0)));
    }

    @Test
    public void delete() {
        THelper.get(foo).addRange(0, 1, (short) 0).
                addRange(1, 3, (short) 1);

        foo.delete(1, 2);

        assertThat(foo.toString(), is("fo"));
        assertThat(foo, taintEquals(range(0, 1, 0).add(1, 2, 1).done()));
    }

    @Test
    public void deleteCharAt() {
        THelper.get(foo).addRange(0, 1, (short) 0).
                addRange(1, 3, (short) 1);

        foo.deleteCharAt(1);

        assertThat(foo.toString(), is("fo"));
        assertThat(foo, taintEquals(range(0, 1, 0).add(1, 2, 1).done()));
    }

    @Test
    public void replace_1() {
        IASStringBuffer sb = new IASStringBuffer("hello world!");

        THelper.get(sb).addRange(0, 12, (short) 0);
        THelper.get(foo).addRange(0, 3, (short) 1);

        sb.replace(2, 3, foo.toIASString());

        assertThat(sb.toString(), is("hefoolo world!"));
        assertThat(sb, taintEquals(range(0, 2, 0).add(2, 5, 1).add(5, 14, 0)));
    }

    @Test
    public void replace_2() {
        IASStringBuffer sb = new IASStringBuffer("hello world!");

        THelper.get(sb).addRange(0, 12, (short) 0);

        sb.replace(1, 11, new IASString(""));

        assertThat(sb.toString(), is("h!"));
        assertThat(sb, taintEquals(range(0, 2, 0)));
    }

    @Test
    public void substring() {
        IASStringBuffer sb = new IASStringBuffer("hello world!");

        THelper.get(sb).addRange(0, 8, (short) 0).addRange(8, 12, (short) 1);

        IASString sub1 = sb.substring(1);
        IASString sub2 = sb.substring(7, 9);

        assertThat(sub1.toString(), is("ello world!"));
        assertThat(sub1, taintEquals(range(0, 7, 0).add(7, 11, 1)));

        assertThat(sub2.toString(), is("or"));
        assertThat(sub2, taintEquals(range(0, 1, 0).add(1, 2, 1)));
    }

    @Test
    public void insert_charArray_partially() {
        char[] ar = "bar".toCharArray();

        THelper.get(foo).addRange(0, 3, (short) 0);

        foo.insert(1, ar, 1, 2);

        assertThat(foo.toString(), is("faroo"));
        assertThat(foo, taintEquals(range(0, 1, 0).add(3, 5, 0)));
    }

    @Test
    public void insert_string_1() {
        IASString str = new IASString("bar");

        THelper.get(foo).addRange(0, 3, (short) 0);
        THelper.get(str).addRange(0, 3, (short) 1);

        foo.insert(1, str);

        assertThat(foo.toString(), is("fbaroo"));
        assertThat(foo, taintEquals(range(0, 1, 0).add(1, 4, 1).add(4, 6, 0)));
    }

    @Test
    public void insert_string_2() {
        IASString str = new IASString("bar");

        THelper.get(foo).addRange(0, 3, (short) 0);

        foo.insert(1, str);

        assertThat(foo.toString(), is("fbaroo"));
        assertThat(foo, taintEquals(range(0, 1, 0).add(4, 6, 0)));
    }

    @Test
    public void insert_charArray() {
        char[] ar = new IASString("bar").toCharArray();

        THelper.get(foo).addRange(0, 3, (short) 0);

        foo.insert(1, ar);

        assertThat(foo.toString(), is("fbaroo"));
        assertThat(foo, taintEquals(range(0, 1, 0).add(4, 6, 0)));
    }

    @Test
    public void insert_charSequence_1() {
        // insert subtype of AbstractStringBuilder
        THelper.get(foo).addRange(0, 3, (short) 0);
        THelper.get(bar).addRange(0, 3, (short) 1);

        foo.insert(1, bar, 0, 2);

        assertThat(foo.toString(), is("fbaoo"));
        assertThat(foo, taintEquals(range(0, 1, 0).add(1, 3, 1).add(3, 5, 0)));
    }

    @Test
    public void insert_charSequence_2() {
        // insert an unknown implementation of CharSequence
        THelper.get(foo).addRange(0, 3, (short) 0);
        IASStringBuffer foo2 = new IASStringBuffer(foo);

        foo.insert(0, createCharSequence("blub"));
        foo2.insert(1, createCharSequence("blub"));

        assertThat(foo.toString(), is("blubfoo"));
        assertThat(foo, taintEquals(range(4, 7, 0)));

        assertThat(foo2.toString(), is("fbluboo"));
        assertThat(foo2, taintEquals(range(0, 1, 0).add(5, 7, 0)));
    }

    @Test
    public void insert_charSequence_3() {
        // insert an untainted String
        THelper.get(foo).addRange(0, 1, (short) 0);

        foo.insert(3, "blub", 0, 4); // we need 0 and 4 as parameters, even if they are meaningless, but otherwise compile would take insert(offset, String) instead

        assertThat(foo.toString(), is("fooblub"));
        assertThat(foo, taintEquals(range(0, 1, 0)));
    }

    @Test
    public void insert_charSequence_4() {
        // insert an empty range into an untainted StringBuffer
        foo.insert(0, "blub", 0, 0);

        assertThat(foo.toString(), is("foo"));
        assertThat(foo, taintUninitialized());
    }

    @Test
    public void insert_char() {
        // insert an untainted String
        THelper.get(foo).addRange(0, 3, (short) 0);

        IASStringBuffer foo2 = new IASStringBuffer(foo);

        foo.insert(0, 'u');
        foo2.insert(1, 'u');

        assertThat(foo.toString(), is("ufoo"));
        assertThat(foo, taintEquals(range(1, 4, 0)));

        assertThat(foo2.toString(), is("fuoo"));
        assertThat(foo2, taintEquals(range(0, 1, 0).add(2, 4, 0)));
    }

    @Test
    public void reverse_1() {
        // just Unicode characters contained in BMP
        IASStringBuffer sb = new IASStringBuffer("hello world!");

        THelper.get(sb).addRange(0, 8, (short) 0).addRange(8, 12, (short) 1);

        sb.reverse();

        assertThat(sb.toString(), is("!dlrow olleh"));
        assertThat(sb, taintEquals(range(0, 4, 1).add(4, 12, 0)));
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

        THelper.get(sb1).addRange(0, 2, (short) 0);
        THelper.get(sb2).addRange(2, 4, (short) 0);
        THelper.get(sb3).addRange(0, 2, (short) 0).addRange(2, 4, (short) 1);
        THelper.get(sb4).addRange(0, 3, (short) 0);

        sb1.reverse();
        sb2.reverse();
        sb3.reverse();
        sb4.reverse();

        assertThat(sb1.toString(), is("b\uD800\uDC00a"));
        assertThat(sb1, taintEquals(range(1, 2, 0).add(3, 4, 0)));

        assertThat(sb2.toString(), is("b\uD800\uDC00a"));
        assertThat(sb2, taintEquals(range(0, 1, 0).add(2, 3, 0)));

        assertThat(sb3.toString(), is("b\uD800\uDC00a"));
        assertThat(sb3, taintEquals(range(0, 1, 1).add(1, 2, 0).add(2, 3, 1).add(3, 4, 0)));

        assertThat(sb4.toString(), is("b\uD800\uDC00a"));
        assertThat(sb4, taintEquals(range(1, 4, 0)));
    }
}

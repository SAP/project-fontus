package de.tubs.cs.ias.asm_test.taintaware.range;

import de.tubs.cs.ias.asm_test.TaintStringHelper;
import de.tubs.cs.ias.asm_test.taintaware.range.testHelper.THelper;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.EnableJUnit4MigrationSupport;

import java.util.Locale;

import static de.tubs.cs.ias.asm_test.taintaware.range.testHelper.HelperUtils.createCharSequence;
import static de.tubs.cs.ias.asm_test.taintaware.range.testHelper.RangeChainer.range;
import static de.tubs.cs.ias.asm_test.taintaware.range.testHelper.TaintMatcher.taintEquals;
import static de.tubs.cs.ias.asm_test.taintaware.range.testHelper.TaintMatcher.taintUninitialized;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@EnableJUnit4MigrationSupport
public class StringTest {
    private final static IASTaintSource SAMPLE_SOURCE = IASTaintSource.TS_CS_UNKNOWN_ORIGIN;
    private IASString foo = null;
    private IASString bar = null;

    @BeforeEach
    public void initStrings() {
        // Because literals are pooled but we need isolation for tests.
        this.foo = new IASString("foo");
        this.bar = new IASString("bar");
    }

//    @Test
//    public void constructor_fromOtherString() {
//        IASString s = new IASString(new char[]{'f', 'o', 'o'});
//
//        assertTrue(s.isTainted());
//
//        s.getTaintInformation().addRange(0, 2, (short) 0);
//
//        assertThat(s.isTainted(), is(true));
//
//        IASString s2 = new IASString(s);
//
//        assertThat(s2, not(sameInstance(s)));
//        assertEquals(s, s2);
//        assertThat(s2.getTaintInformation().getAllRanges(), is(range(0, 2, 0).done()));
//    }
//
//    @Test
//    public void stringLiteralsAreTaintableToo() {
//        IASString s = new IASString("foo bar");
//
//        assertThat(s.isTainted(), is(false));
//
//        // Here we have a IASString literal without an TaintInformation instance, therefore getTaintInformation() creates it
//        s.getTaintInformation().addRange(1, 2, (short) 0);
//
//        assertFalse(s.isUninitialized());
//        assertThat(s.isTainted(), is(true));
//
//        IASString s2 = new IASString(s);
//        assertThat(s2.getTaintInformation().getAllRanges(), is(range(1, 2, 0).done()));
//        // TaintInformation instances are not shared between different strings, but ranges are
//        assertThat(s2.getTaintInformation(), not(sameInstance(s.getTaintInformation())));
//        assertThat(s2.getTaintInformation().getAllRanges().get(0), sameInstance(s.getTaintInformation().getAllRanges().get(0)));
//    }
//
//    @Test
//    public void constructor_stringFromStringBuilder() {
//        IASStringBuilder sB = new IASStringBuilder("foo");
//
//        sB.getTaintInformation().addRange(1, 2, SAMPLE_SOURCE);
//
//        IASString s = new IASString(sB);
//
//        assertThat(sB.getTaintInformation().getAllRanges(), equalTo(s.getTaintInformation().getAllRanges()));
//    }
//
//    @Test
//    public void constructor_stringFromStringBuilder_sbNotTainted() {
//        IASStringBuilder sB = new IASStringBuilder("foo");
//
//        IASString s = new IASString(sB);
//
//        assertThat(THelper.isUninitialized(sB), is(true));
//        assertThat(THelper.isUninitialized(s), is(true));
//    }
//
//    @Test
//    public void constructor_stringFromStringBuffer() {
//        IASStringBuffer sB = new IASStringBuffer("foo");
//
//        THelper.get(sB).addRange(1, 2, SAMPLE_SOURCE);
//
//        IASString s = new IASString(sB);
//
//        assertThat(THelper.get(sB).getAllRanges(), equalTo(THelper.get(s).getAllRanges()));
//    }

    @Test
    public void substring_1() {
        // substring(int beginIndex)

        THelper.get(foo).addRange(0, 2, (short) 0);

        IASString s1 = foo.substring(1);

        assertThat(s1, taintEquals(range(0, 1, 0).done()));

        // A substring created from an untainted region should not have its taint field initialized
        IASString s2 = foo.substring(2);

//        assertThat(s2, taintUninitialized());
        assertFalse(s2.isTainted());
    }

    @Test
    public void substring_2() {
        THelper.get(new IASString("foobar")).addRange(1, 4, (short) 0);

        IASString s = new IASString("foobar");
        THelper.get(s).addRange(1, 4, (short) 0);
        IASString s1 = s.substring(0, 2);
        assertThat(THelper.get(s1).getAllRanges(), equalTo(range(1, 2, 0).done()));

        // A substring created from an untainted region should not have its taint field initialized
        IASString s2 = s.substring(4, 6);

        // assertThat(s2, taintUninitialized());
        assertFalse(s2.isTainted());

        // zero-length substring
        IASString s3 = s.substring(1, 1);

        assertThat(s3.toString(), is(""));
        // assertThat(s3, taintUninitialized());
        assertFalse(s3.isTainted());
    }

    @Test
    public void concat_1() {
        THelper.get(foo).addRange(0, 1, (short) 0);
        THelper.get(bar).addRange(1, 2, (short) 0);

        IASString s = foo.concat(bar);

        // TODO Here we could also test interning, can't we?

        assertThat(THelper.get(s).getAllRanges(), equalTo(range(0, 1, 0).add(4, 5, 0).done()));
    }

    @Test
    public void concat_2() {
        THelper.get(bar).addRange(1, 2, (short) 0);

        IASString s = foo.concat(bar);

        assertThat(THelper.get(s).getAllRanges(), equalTo(range(4, 5, 0).done()));
    }

    @Test
    public void concat_3() {
        THelper.get(foo).addRange(0, 1, (short) 0);

        IASString s = foo.concat(bar);

        assertThat(THelper.get(s).getAllRanges(), equalTo(range(0, 1, 0).done()));
    }

    @Test
    @Ignore
    public void replace() {
        THelper.get(foo).addRange(0, 1, (short) 0);

        IASString s1 = foo.replace('f', 'p');
        IASString s2 = foo.replace('o', 'e');
        IASString s3 = foo.replace(' ', 'z');

        IASString s4 = bar.replace('a', 'i');


        assertThat(s1.toString(), equalTo("poo"));
        assertThat(s1, taintEquals(range(0, 1, IASTaintSource.TS_CHAR_UNKNOWN_ORIGIN).done()));

        assertThat(s2.toString(), equalTo("fee"));
        // Every replaced char gets its own ranges...
        assertThat(THelper.get(s2).getAllRanges(), equalTo(range(0, 1, 0).add(1, 2, IASTaintSource.TS_CHAR_UNKNOWN_ORIGIN).add(2, 3, IASTaintSource.TS_CHAR_UNKNOWN_ORIGIN).done()));
        // .. but usually the adjacent ranges get merged on retrieval
        assertThat(s2, taintEquals(range(0, 1, 0).add(1, 3, IASTaintSource.TS_CHAR_UNKNOWN_ORIGIN).done()));

        assertThat(s3.toString(), equalTo(s3));
        assertThat(s3, taintEquals(range(0, 1, 0).done()));

        assertThat(s4.toString(), equalTo("bir"));
        assertThat(s4, taintEquals(range(1, 2, IASTaintSource.TS_CHAR_UNKNOWN_ORIGIN).done()));
    }

    @Test
    public void replaceFirst_ignoredTainting_regression_1() {
        IASString s1 = new IASString("hellllo");
        IASString s2 = new IASString("zz");
        IASString s3 = new IASString("ll");

        THelper.get(s2).addRange(0, 2, (short) IASTaintSource.TS_CS_UNKNOWN_ORIGIN.getId());

        IASString s = s1.replaceFirst(s3, s2);

        assertEquals("hellllo", s1.toString());
        assertEquals("zz", s2.toString());
        assertEquals("ll", s3.toString());
        assertEquals("hezzllo", s.toString());

        assertTrue(THelper.isUninitialized(s1));
        assertThat(s2, taintEquals(range(0, 2, (short) IASTaintSource.TS_CS_UNKNOWN_ORIGIN.getId())));
        assertTrue(THelper.isUninitialized(s3));
        assertThat(s, taintEquals(range(2, 4, (short) IASTaintSource.TS_CS_UNKNOWN_ORIGIN.getId())));
    }

    @Test
    public void boolTaintConstructorRegressionTest() {
        IASString s = new IASString("Hallo Welt!", false);
        assertTrue(s.isUninitialized());

        IASString s2 = new IASString("Hallo Welt 2!", true);
        assertThat(s2, taintEquals(range(0, s2.length(), IASTaintSource.TS_CS_UNKNOWN_ORIGIN)));
    }

    @Test
    public void replaceFirst_ignoredTainting_regression_2() {
        IASString s1 = new IASString("hellllo");
        IASString s2 = new IASString("zz");
        IASString s3 = new IASString("ll");

        THelper.get(s1).addRange(2, 4, (short) IASTaintSource.TS_CS_UNKNOWN_ORIGIN.getId());

        IASString s = s1.replaceFirst(s3, s2);

        assertEquals("hellllo", s1.toString());
        assertEquals("zz", s2.toString());
        assertEquals("ll", s3.toString());
        assertEquals("hezzllo", s.toString());

        assertThat(s1, taintEquals(range(2, 4, (short) IASTaintSource.TS_CS_UNKNOWN_ORIGIN.getId())));
        assertTrue(THelper.isUninitialized(s2));
        assertTrue(THelper.isUninitialized(s3));
        assertTrue(THelper.isUninitialized(s));
    }

    @Test
//    @Ignore
    public void replaceFirst() {
        // Primary test cases for this are located in PatternTest#replaceFirst
        THelper.get(foo).addRange(1, 3, (short) 0);
        THelper.get(bar).addRange(1, 2, (short) 1);

        IASString s = foo.replaceFirst(new IASString("o+"), bar);

        assertThat(s.toString(), equalTo("fbar"));
        assertThat(s, taintEquals(range(2, 3, 1).done()));

        IASString s2 = foo.replaceFirst(new IASString("o*"), bar);

        assertThat(s2.toString(), equalTo("barfoo"));
        assertThat(s2, taintEquals(range(1, 2, 1).add(4, 6, 0).done()));
    }

    @Test
    @Ignore
    public void replaceAll() {
        // Primary test cases for this are located in PatternTest#replaceAll
        IASString foofoo = new IASString("foofoo");

        THelper.get(foofoo).addRange(0, 4, (short) 0);
        THelper.get(bar).addRange(3, 5, (short) 1);

        IASString s = foofoo.replaceAll(new IASString("o+"), bar);

        assertThat(s.toString(), equalTo("fbarfbar"));
        assertThat(s, taintEquals(range(0, 1, 0).add(1, 4, 1).add(4, 5, 0).add(5, 8, 1).done()));
    }

    @Test
//    @Ignore
    public void replace_CharSequence() {
        // Primary test cases for this are located in PatternTest#replace
        // As there are no real modifications to this method in String.java we more or less test
        // whether the CharSequence-augmentation works fine
        THelper.get(foo).addRange(0, 3, (short) 0);
        THelper.get(bar).addRange(0, 3, (short) 1);

        IASString s1 = foo.replace("fo", bar);

        IASStringBuilder builder = new IASStringBuilder(bar); // builder inherits the taint from bar
        IASString s2 = foo.replace("fo", bar);

        IASStringBuffer buffer = new IASStringBuffer(bar); // the same applies to buffer
        IASString s3 = foo.replace("fo", buffer);

        assertThat(s1.toString(), equalTo("baro"));
        assertThat(s2.toString(), equalTo("baro"));
        assertThat(s3.toString(), equalTo("baro"));

        // We expect replace() to be able to handle taint of String, StringBuilder and StringBuffer
        assertThat(s1, taintEquals(range(0, 3, 1).add(3, 4, 0).done()));
        assertThat(s2, taintEquals(range(0, 3, 1).add(3, 4, 0).done()));
        assertThat(s3, taintEquals(range(0, 3, 1).add(3, 4, 0).done()));

        // For other implementations of CharSequence it shall use a generic taint information
        CharSequence myCharSequence = createCharSequence(bar.toString());
        IASString s4 = foo.replace("fo", myCharSequence);

//        assertThat(THelper.isUninitialized(myCharSequence), is(false));
        assertThat(s4.toString(), equalTo("baro"));
        assertThat(s4, taintEquals(range(3, 4, 0).done()));
    }

    @Test
    public void split_fastpath() {
        // for the non-fastpath code see test cases located in PatternTest#split()
        // Should be covered without modifications because String#substring() is used internally
        IASString s = new IASString("hello:world:okay");

        THelper.get(s).addRange(5, 6, (short) 0).addRange(12, 16, (short) 1); // ":" and "okay"

        IASString[] arr = s.split(new IASString(":"));
        IASString[] expected = new IASString[]{new IASString("hello"), new IASString("world"), new IASString("okay")};

        assertThat(arr, equalTo(expected));
        assertFalse(arr[0].isTainted());
        assertFalse(arr[1].isTainted());
        assertThat(arr[2], taintEquals(range(0, 4, 1).done()));
    }

    @Test
    public void split_limit() {
        IASString a = new IASString("a,b,c");
        THelper.get(a).addRange(0, 3, (short) 1);

        IASString[] splitted = a.split(new IASString(","), 2);

        // Content assertions
        assertEquals("a", splitted[0].toString());
        assertEquals("b,c", splitted[1].toString());

        // Taint assertions
        assertThat(splitted[0], taintEquals(range(0, 1, 1)));
        assertThat(splitted[1], taintEquals(range(0, 1, 1)));
    }

    @Test
//    @Ignore
    public void join() {
        // join(CharSequence delimiter, CharSequence... elements) and String join(CharSequence delimiter, Iterable<? extends CharSequence> elements)
        // handled through CharSequence-Augmentation

        // Using Strings (these implement TaintAware and are therefore not modified by the CharSequence-Augmentation)
        IASString s1 = IASString.join(",", "a", "b", "c");

        assertThat(s1.toString(), equalTo("a,b,c"));
        assertFalse(s1.isTainted());
//        assertThat(s1, taintUninitialized());

        CharSequence delimiter = createCharSequence(",");
        CharSequence a = new IASString("a", true);
        CharSequence b = createCharSequence("b");

        IASString s2 = IASString.join(delimiter, a, b, "c");

        assertThat(b.toString(), equalTo("b"));
        assertThat(s2, taintEquals(range(0, 1, IASTaintSource.TS_CS_UNKNOWN_ORIGIN).done()));
        assertThat(s2.toString(), equalTo("a,b,c"));

        // TODO Not yet working because StringJoiner doesn't use CharSequence#toString, it uses a StringBuilder and its append method
        // that copies char by char from the CharSequence. So StringBuilder#append needs to be augmented first
//        assertThat(s2, not(taintUninitialized()));
    }

    @Test
    // Compile encoding issues with AdoptOpenJDK on Windows
    @Ignore
    public void toLowerCase() {
        // For examples of characters that "grow" when lowercasing them see "ConditionalSpecialCasing.java"
        Locale lithuanian = new Locale("lt");
        assertThat("\u00CCb".toLowerCase(lithuanian), is("\u0069\u0307\u0300b"));

        IASString ltUC = new IASString("\u00CC");
        THelper.get(ltUC).addRange(0, 1, (short) 1);
        THelper.get(foo).addRange(0, 3, (short) 2);
        THelper.get(bar).addRange(2, 3, (short) 3);

//        IASString in = "ß".concat(ltUC).concat("B");
        IASString in = new IASString("ß").concat(ltUC).concat(foo).concat(ltUC).concat(bar);

        assertThat(in.toString(), is("ß\u00CCfoo\u00CCbar"));
        assertThat(in, taintEquals(
                range(1, 2, 1)
                        .add(2, 5, 2)
                        .add(5, 6, 1)
                        .add(8, 9, 3).done()));

        IASString lc = in.toLowerCase(lithuanian);

        assertThat(lc.toString(), is("ß\u0069\u0307\u0300foo\u0069\u0307\u0300bar"));
        assertThat(lc, taintEquals(range(1, 4, 1).add(4, 7, 2).add(7, 10, 1).add(12, 13, 3).done()));

        assertThat("B".toLowerCase(), taintUninitialized());

        // This is a special case because the first case where we try to get the ranges from the start of
        // the string until the first growing char needs to handle an zero-sized range - this wasn't done in the first take
        // (not necessary any longer, getRanges() not returns an empty list for zero-sized startIndex == endIndex)
        assertThat(ltUC.toLowerCase(lithuanian), taintEquals(range(0, 3, 1).done()));
    }

    @Test
    // Compile encoding issues with AdoptOpenJDK on Windows
    @Ignore
    public void toUpperCase() {
        assertThat("ß".toUpperCase(), is("SS"));

        IASString sharpS = new IASString("ß");
        THelper.get(sharpS).addRange(0, 1, (short) 1);
        THelper.get(bar).addRange(2, 3, (short) 3);

//        IASString in = "ß".concat(ltUC).concat("B");
        IASString in = new IASString("ß").concat(sharpS).concat(sharpS).concat(bar);

        assertThat(in, is("ßßßbar"));
        assertThat(in, taintEquals(range(1, 3, 1).add(5, 6, 3).done()));

        IASString uc = in.toUpperCase();

        assertThat(uc, is("SSSSSSBAR"));
        assertThat(uc, taintEquals(range(2, 6, 1).add(8, 9, 3).done()));

        assertThat("ß".toUpperCase(), taintUninitialized());
    }

    @Test
    public void trim() {
        // trim uses substring() internally
        IASString ws = new IASString(" ");
        THelper.get(ws).addRange(0, 1, (short) 1);

        IASString in = ws.concat(foo).concat(ws);

        assertThat(in, taintEquals(range(0, 1, 1).add(4, 5, 1).done()));

        IASString trimmed1 = in.trim();

//        assertThat(trimmed1, taintUninitialized());

        IASString trimmed2 = in.concat(foo).trim();

        assertThat(trimmed2, taintEquals(range(3, 4, 1).done()));
    }

    @Test
    public void replaceAll_simple() {
        IASString in = new IASString("hellofoobarfoo!");

        THelper.get(in).addRange(0, 15, (short) 1);
        THelper.get(bar).addRange(0, 2, (short) 2);

        IASString out = in.replaceAll(foo, bar);

        assertThat(out.toString(), is("hellobarbarbar!"));
        assertThat(out, taintEquals(range(0, 5, 1).add(5, 7, 2).add(8, 11, 1).add(11, 13, 2).add(14, 15, 1).done()));
    }

    @Test
    public void replaceAll_escapedChar() {
        IASString in = new IASString("hellofoobarfoo!");
        IASString replacement = new IASString("ba\\r");

        THelper.get(in).addRange(0, 15, (short) 1);
        THelper.get(replacement).addRange(0, 2, (short) 2);

        IASString out = in.replaceAll(foo, replacement);

        assertThat(out.toString(), is("hellobarbarbar!"));
        assertThat(out, taintEquals(range(0, 5, 1).add(5, 7, 2).add(8, 11, 1).add(11, 13, 2).add(14, 15, 1).done()));
    }

    @Test
    public void replaceAll_backreference() {
        IASString in = new IASString("hellofoofoo!");
        IASString regex = new IASString("f((o)\\2)");
        IASString replacement = new IASString("f$1bar");

        THelper.get(in).addRange(0, 12, (short) 1);
        THelper.get(regex).addRange(0, 3, (short) 2);
        THelper.get(replacement).addRange(0, 6, (short) 3);

        IASString out = in.replaceAll(regex, replacement);

        assertThat(out.toString(), is("hellofoobarfoobar!"));
        assertThat(out, taintEquals(
                range(0, 5, 1)
                        .add(5, 6, 3)
                        .add(6, 8, 1)
                        .add(8, 12, 3)
                        .add(12, 14, 1)
                        .add(14, 17, 3)
                        .add(17, 18, 1).done()));
    }

    @Test
    public void replace_simple() {
        // replace does the same as replaceAll, but treating the needle and the
        // replacement strings as literals (so no special regex functionality)
        IASString in = new IASString("hellofoobarfoo!");

        THelper.get(in).addRange(0, 15, (short) 1);
        THelper.get(bar).addRange(0, 2, (short) 2);

        IASString out = in.replace(foo, bar);

        assertThat(out.toString(), is("hellobarbarbar!"));
        assertThat(out, taintEquals(
                range(0, 5, 1)
                        .add(5, 7, 2)
                        .add(8, 11, 1)
                        .add(11, 13, 2)
                        .add(14, 15, 1).done()));
    }

    @Test
    public void replace_charSequence() {
        // replace calls toIASString() on the given replacement CharSequence, therefore triggering behaviour added by the CharSequenceInstrumenter
        // in case we are replacing with a CharSequence implementation that is not taint-aware out-of-the box
        IASString in = new IASString("hellofoobarfoo!");
        CharSequence replacement = createCharSequence("bar");

        THelper.get(in).addRange(0, 15, (short) 1);

        IASString out = in.replace(foo, replacement);

        assertThat(out.toString(), is("hellobarbarbar!"));
        assertThat(out, taintEquals(range(0, 5, 1).add(8, 11, 1).add(14, 15, 1).done()));
    }

}
package com.sap.fontus.taintaware.range;

import com.sap.fontus.taintaware.range.testHelper.HelperUtils;
import com.sap.fontus.taintaware.range.testHelper.RangeChainer;
import com.sap.fontus.taintaware.range.testHelper.THelper;
import com.sap.fontus.taintaware.range.testHelper.TaintMatcher;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASStringBuffer;
import com.sap.fontus.taintaware.unified.IASStringBuilder;
import org.hamcrest.MatcherAssert;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.EnableJUnit4MigrationSupport;

import java.util.Locale;

import static com.sap.fontus.taintaware.range.testHelper.RangeChainer.range;
import static com.sap.fontus.taintaware.range.testHelper.TaintMatcher.taintEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

@EnableJUnit4MigrationSupport
@SuppressWarnings("ALL")
// David: As I didn't write this Code and don't want to mess with it I suppressed the warnings.
// TODO: Work out whether we can adapt it to the style of the remaining project?
public class StringTest {
    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    private final static IASTaintSource SAMPLE_SOURCE = IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN;
    private final static IASTaintMetadata md = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterTaintSource("dummy"));
    private final static IASTaintMetadata md2 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterTaintSource("dummy2"));
    private final static IASTaintMetadata md3 = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterTaintSource("dummy3"));
    private IASString foo = null;
    private IASString bar = null;

    @BeforeEach
    public void initStrings() {
        // Because literals are pooled but we need isolation for tests.
        this.foo = new IASString("foo");
        this.bar = new IASString("bar");
    }

    @Test
    public void replaceRegression1() {
        IASString source = new IASString("Hello, Hello, Hello");
        IASString target = new IASString("Hello");
        IASString replacement = new IASString("World");

        IASString result = source.replace(target, replacement);

        assertEquals("World, World, World", result.getString());
        assertFalse(result.isTainted());
    }

    @Test
    public void replaceRegression2() {
        IASString source = new IASString("Hello, Hello, Hello", true);
        IASString target = new IASString("Hello");
        IASString replacement = new IASString("World");

        IASString result = source.replace(target, replacement);

        assertEquals("World, World, World", result.getString());
        assertTrue(result.isTainted());
    }

    @Test
    public void replaceRegression3() {
        IASString source = new IASString("Hello, Hello, Hello");
        IASString target = new IASString("Hello", true);
        IASString replacement = new IASString("World");

        IASString result = source.replace(target, replacement);

        assertEquals("World, World, World", result.getString());
        assertFalse(result.isTainted());
    }

    @Test
    public void replaceRegression4() {
        IASString source = new IASString("Hello, Hello, Hello");
        IASString target = new IASString("Hello");
        IASString replacement = new IASString("World", true);

        IASString result = source.replace(target, replacement);

        assertEquals("World, World, World", result.getString());
        assertTrue(result.isTainted());
    }


//    @Test
//    public void constructor_fromOtherString() {
//        IASString s = new IASString(new char[]{'f', 'o', 'o'});
//
//        assertTrue(s.isTainted());
//
//        s.getTaintInformation().addRange(0, 2, 3);
//
//        assertThat(s.isTainted(), is(true));
//
//        IASString s2 = new IASString(s);
//
//        assertThat(s2, not(sameInstance(s)));
//        assertEquals(s, s2);
//        assertThat(s2.getTaintInformation().getTaintRanges(), is(range(0, 2, 3).done()));
//    }
//
//    @Test
//    public void stringLiteralsAreTaintableToo() {
//        IASString s = new IASString("foo bar");
//
//        assertThat(s.isTainted(), is(false));
//
//        // Here we have a IASString literal without an TaintInformation instance, therefore getTaintInformation() creates it
//        s.getTaintInformation().addRange(1, 2, 3);
//
//        assertFalse(s.isUninitialized());
//        assertThat(s.isTainted(), is(true));
//
//        IASString s2 = new IASString(s);
//        assertThat(s2.getTaintInformation().getTaintRanges(), is(range(1, 2, 3).done()));
//        // TaintInformation instances are not shared between different strings, but ranges are
//        assertThat(s2.getTaintInformation(), not(sameInstance(s.getTaintInformation())));
//        assertThat(s2.getTaintInformation().getTaintRanges().get(0), sameInstance(s.getTaintInformation().getTaintRanges().get(0)));
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
//        assertThat(sB.getTaintInformation().getTaintRanges(), equalTo(s.getTaintInformation().getTaintRanges()));
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
//        assertThat(THelper.get(sB).getTaintRanges(), equalTo(THelper.get(s).getTaintRanges()));
//    }

    @Test
    void test_subSequence_1() {
        IASString s = new IASString("Hello World!", true);
        assert s.isTainted();

        IASString s1 = (IASString) s.subSequence(2, 7);
        IASString s2 = (IASString) s.subSequence(5, 5);

        assertEquals("llo W", s1.toString());
        MatcherAssert.assertThat(s1, TaintMatcher.taintEquals(range(0, 5, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)));
        assertEquals("", s2.toString());
        assertFalse(THelper.get(s2).isTainted());
    }

    @Test
    void test_subSequence_2() {
        IASString s = new IASString("Hello World!", false);
        assert !s.isTainted();

        ((IASTaintInformation) THelper.get(s)).addRange(2, 7, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN);

        IASString s1 = (IASString) s.subSequence(0, 1);
        IASString s2 = (IASString) s.subSequence(0, 5);
        IASString s3 = (IASString) s.subSequence(5, 9);
        IASString s4 = (IASString) s.subSequence(8, 12);

        assertFalse(s1.isTainted());
        MatcherAssert.assertThat(s2, TaintMatcher.taintEquals(range(2, 5, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)));
        MatcherAssert.assertThat(s3, TaintMatcher.taintEquals(range(0, 2, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)));
        assertFalse(s4.isTainted());
    }

    @Test
    public void substring_1() {
        // substring(int beginIndex)

        ((IASTaintInformation) THelper.get(foo)).addRange(0, 2, md);

        IASString s1 = foo.substring(1);

        MatcherAssert.assertThat(s1, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md).done()));

        // A substring created from an untainted region should not have its taint field initialized
        IASString s2 = foo.substring(2);

//        assertThat(s2, taintUninitialized());
        assertFalse(s2.isTainted());
    }

    @Test
    public void substring_2() {
        ((IASTaintInformation) THelper.get(new IASString("foobar"))).addRange(1, 4, md);

        IASString s = new IASString("foobar");
        ((IASTaintInformation) THelper.get(s)).addRange(1, 4, md);
        IASString s1 = s.substring(0, 2);
        assertThat(THelper.get(s1).getTaintRanges(s1.length()).getTaintRanges(), equalTo(RangeChainer.range(1, 2, md).done()));

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
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 1, md);
        ((IASTaintInformation) THelper.get(bar)).addRange(1, 2, md);

        IASString s = foo.concat(bar);

        // TODO Here we could also test interning, can't we?

        assertThat(((IASTaintInformation) THelper.get(s)).getTaintRanges().getTaintRanges(), equalTo(RangeChainer.range(0, 1, md).add(4, 5, md).done()));
    }

    @Test
    public void concat_2() {
        ((IASTaintInformation) THelper.get(bar)).addRange(1, 2, md);

        IASString s = foo.concat(bar);

        assertThat(((IASTaintInformation) THelper.get(s)).getTaintRanges().getTaintRanges(), equalTo(RangeChainer.range(4, 5, md).done()));
    }

    @Test
    public void concat_3() {
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 1, md);

        IASString s = foo.concat(bar);

        assertThat(((IASTaintInformation) THelper.get(s)).getTaintRanges().getTaintRanges(), equalTo(RangeChainer.range(0, 1, md).done()));
    }

    @Test
    @Ignore
    public void replace() {
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 1, md);

        IASString s1 = foo.replace('f', 'p');
        IASString s2 = foo.replace('o', 'e');
        IASString s3 = foo.replace(' ', 'z');

        IASString s4 = bar.replace('a', 'i');


        assertThat(s1.toString(), equalTo("poo"));
        MatcherAssert.assertThat(s1, TaintMatcher.taintEquals(range(0, 1, IASTaintSourceRegistry.MD_CHAR_UNKNOWN_ORIGIN).done()));

        assertThat(s2.toString(), equalTo("fee"));
        // Every replaced char gets its own ranges...
        assertThat(THelper.get(s2).getTaintRanges(s2.length()), equalTo(RangeChainer.range(0, 1, md).add(1, 2, IASTaintSourceRegistry.MD_CHAR_UNKNOWN_ORIGIN).add(2, 3, IASTaintSourceRegistry.MD_CHAR_UNKNOWN_ORIGIN).done()));
        // .. but usually the adjacent ranges get merged on retrieval
        MatcherAssert.assertThat(s2, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md).add(1, 3, IASTaintSourceRegistry.MD_CHAR_UNKNOWN_ORIGIN).done()));

        assertThat(s3.toString(), equalTo(s3));
        MatcherAssert.assertThat(s3, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md).done()));

        assertThat(s4.toString(), equalTo("bir"));
        MatcherAssert.assertThat(s4, TaintMatcher.taintEquals(range(1, 2, IASTaintSourceRegistry.MD_CHAR_UNKNOWN_ORIGIN).done()));
    }

    @Test
    public void replaceFirst_ignoredTainting_regression_1() {
        IASString s1 = new IASString("hellllo");
        IASString s2 = new IASString("zz");
        IASString s3 = new IASString("ll");

        ((IASTaintInformation) THelper.get(s2)).addRange(0, 2, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN);

        IASString s = s1.replaceFirst(s3, s2);

        assertEquals("hellllo", s1.toString());
        assertEquals("zz", s2.toString());
        assertEquals("ll", s3.toString());
        assertEquals("hezzllo", s.toString());

        assertTrue(THelper.isUninitialized(s1));
        MatcherAssert.assertThat(s2, TaintMatcher.taintEquals(range(0, 2, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)));
        assertTrue(THelper.isUninitialized(s3));
        MatcherAssert.assertThat(s, TaintMatcher.taintEquals(range(2, 4, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)));
    }

    @Test
    public void boolTaintConstructorRegressionTest() {
        IASString s = new IASString("Hallo Welt!", false);
        assertTrue(s.isUninitialized());

        IASString s2 = new IASString("Hallo Welt 2!", true);
        MatcherAssert.assertThat(s2, TaintMatcher.taintEquals(range(0, s2.length(), IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)));
    }

    @Test
    public void replaceFirst_ignoredTainting_regression_2() {
        IASString s1 = new IASString("hellllo");
        IASString s2 = new IASString("zz");
        IASString s3 = new IASString("ll");

        ((IASTaintInformation) THelper.get(s1)).addRange(2, 4, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN);

        IASString s = s1.replaceFirst(s3, s2);

        assertEquals("hellllo", s1.toString());
        assertEquals("zz", s2.toString());
        assertEquals("ll", s3.toString());
        assertEquals("hezzllo", s.toString());

        MatcherAssert.assertThat(s1, TaintMatcher.taintEquals(range(2, 4, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)));
        assertTrue(THelper.isUninitialized(s2));
        assertTrue(THelper.isUninitialized(s3));
        assertFalse(THelper.get(s).isTainted());
    }

    @Test
    public void testIntern() {
        String s1 = new String("Hello World!");
        String s2 = new String("Hello World!");
        IASString iasString1 = new IASString(s1);
        IASString iasString2 = new IASString(s2);

        IASString iasString3 = iasString1.intern();
        IASString iasString4 = iasString2.intern();

        assertNotSame(s1, s2);
        assertNotSame(iasString1, iasString2);
        assertEquals(iasString1, iasString3);
        assertEquals(iasString1, iasString4);

        assertEquals(s1, iasString1.getString());
        assertEquals(s1, iasString2.getString());
        assertEquals(s1, iasString3.getString());
        assertEquals(s1, iasString4.getString());
    }

    @Test
//    @Ignore
    public void replaceFirst() {
        // Primary test cases for this are located in PatternTest#replaceFirst
        ((IASTaintInformation) THelper.get(foo)).addRange(1, 3, md);
        ((IASTaintInformation) THelper.get(bar)).addRange(1, 2, md2);

        IASString s = foo.replaceFirst(new IASString("o+"), bar);

        assertThat(s.toString(), equalTo("fbar"));
        MatcherAssert.assertThat(s, TaintMatcher.taintEquals(RangeChainer.range(2, 3, md2).done()));

        IASString s2 = foo.replaceFirst(new IASString("o*"), bar);

        assertThat(s2.toString(), equalTo("barfoo"));
        MatcherAssert.assertThat(s2, TaintMatcher.taintEquals(RangeChainer.range(1, 2, md2).add(4, 6, md).done()));
    }

    @Test
    @Ignore
    public void replaceAll() {
        // Primary test cases for this are located in PatternTest#replaceAll
        IASString foofoo = new IASString("foofoo");

        ((IASTaintInformation) THelper.get(foofoo)).addRange(0, 4, md);
        ((IASTaintInformation) THelper.get(bar)).addRange(3, 5, md2);

        IASString s = foofoo.replaceAll(new IASString("o+"), bar);

        assertThat(s.toString(), equalTo("fbarfbar"));
        MatcherAssert.assertThat(s, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md).add(1, 4, md2).add(4, 5, md).add(5, 8, md2).done()));
    }

    @Test
//    @Ignore
    public void replace_CharSequence() {
        // Primary test cases for this are located in PatternTest#replace
        // As there are no real modifications to this method in String.java we more or less test
        // whether the CharSequence-augmentation works fine
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, md);
        ((IASTaintInformation) THelper.get(bar)).addRange(0, 3, md2);

        IASString s1 = foo.replace("fo", bar);

        IASStringBuilder builder = new IASStringBuilder(bar); // builder inherits the taint from bar
        IASString s2 = foo.replace("fo", bar);

        IASStringBuffer buffer = new IASStringBuffer(bar); // the same applies to buffer
        IASString s3 = foo.replace("fo", buffer);

        assertThat(s1.toString(), equalTo("baro"));
        assertThat(s2.toString(), equalTo("baro"));
        assertThat(s3.toString(), equalTo("baro"));

        // We expect replace() to be able to handle taint of String, StringBuilder and StringBuffer
        MatcherAssert.assertThat(s1, TaintMatcher.taintEquals(RangeChainer.range(0, 3, md2).add(3, 4, md).done()));
        MatcherAssert.assertThat(s2, TaintMatcher.taintEquals(RangeChainer.range(0, 3, md2).add(3, 4, md).done()));
        MatcherAssert.assertThat(s3, TaintMatcher.taintEquals(RangeChainer.range(0, 3, md2).add(3, 4, md).done()));

        // For other implementations of CharSequence it shall use a generic taint information
        CharSequence myCharSequence = HelperUtils.createCharSequence(bar.toString());
        IASString s4 = foo.replace("fo", myCharSequence);

//        assertThat(THelper.isUninitialized(myCharSequence), is(false));
        assertThat(s4.toString(), equalTo("baro"));
        MatcherAssert.assertThat(s4, TaintMatcher.taintEquals(RangeChainer.range(3, 4, md).done()));
    }

    @Test
    public void split_fastpath() {
        // for the non-fastpath code see test cases located in PatternTest#split()
        // Should be covered without modifications because String#substring() is used internally
        IASString s = new IASString("hello:world:okay");

        ((IASTaintInformation) THelper.get(s)).addRange(5, 6, md).addRange(12, 16, md2); // ":" and "okay"

        IASString[] arr = s.split(new IASString(":"));
        IASString[] expected = new IASString[]{new IASString("hello"), new IASString("world"), new IASString("okay")};

        assertThat(arr, equalTo(expected));
        assertFalse(arr[0].isTainted());
        assertFalse(arr[1].isTainted());
        MatcherAssert.assertThat(arr[2], TaintMatcher.taintEquals(RangeChainer.range(0, 4, md2).done()));
    }

    @Test
    public void split_limit() {
        IASString a = new IASString("a,b,c");
        ((IASTaintInformation) THelper.get(a)).addRange(0, 3, md);

        IASString[] splitted = a.split(new IASString(","), 2);

        // Content assertions
        assertEquals("a", splitted[0].toString());
        assertEquals("b,c", splitted[1].toString());

        // Taint assertions
        MatcherAssert.assertThat(splitted[0], TaintMatcher.taintEquals(RangeChainer.range(0, 1, md)));
        MatcherAssert.assertThat(splitted[1], TaintMatcher.taintEquals(RangeChainer.range(0, 1, md)));
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

        CharSequence delimiter = HelperUtils.createCharSequence(",");
        CharSequence a = new IASString("a", true);
        CharSequence b = HelperUtils.createCharSequence("b");

        IASString s2 = IASString.join(delimiter, a, b, "c");

        assertThat(b.toString(), equalTo("b"));
        MatcherAssert.assertThat(s2, TaintMatcher.taintEquals(range(0, 1, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN).done()));
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
        ((IASTaintInformation) THelper.get(ltUC)).addRange(0, 1, md);
        ((IASTaintInformation) THelper.get(foo)).addRange(0, 3, md2);
        ((IASTaintInformation) THelper.get(bar)).addRange(2, 3, md3);

//        IASString in = "ß".concat(ltUC).concat("B");
        IASString in = new IASString("ß").concat(ltUC).concat(foo).concat(ltUC).concat(bar);

        assertThat(in.toString(), is("ß\u00CCfoo\u00CCbar"));
        MatcherAssert.assertThat(in, TaintMatcher.taintEquals(
                RangeChainer.range(1, 2, md)
                        .add(2, 5, md2)
                        .add(5, 6, md)
                        .add(8, 9, md3).done()));

        IASString lc = in.toLowerCase(lithuanian);

        assertThat(lc.toString(), is("ß\u0069\u0307\u0300foo\u0069\u0307\u0300bar"));
        MatcherAssert.assertThat(lc, TaintMatcher.taintEquals(RangeChainer.range(1, 4, md).add(4, 7, md2).add(7, 10, md).add(12, 13, md3).done()));

        MatcherAssert.assertThat("B".toLowerCase(), TaintMatcher.taintUninitialized());

        // This is a special case because the first case where we try to get the ranges from the start of
        // the string until the first growing char needs to handle an zero-sized range - this wasn't done in the first take
        // (not necessary any longer, getRanges() not returns an empty list for zero-sized startIndex == endIndex)
        assertThat(ltUC.toLowerCase(lithuanian), TaintMatcher.taintEquals(RangeChainer.range(0, 3, md).done()));
    }

    @Test
    // Compile encoding issues with AdoptOpenJDK on Windows
    @Ignore
    public void toUpperCase() {
        assertThat("ß".toUpperCase(), is("SS"));

        IASString sharpS = new IASString("ß");
        ((IASTaintInformation) THelper.get(sharpS)).addRange(0, 1, md);
        ((IASTaintInformation) THelper.get(bar)).addRange(2, 3, md3);

//        IASString in = "ß".concat(ltUC).concat("B");
        IASString in = new IASString("ß").concat(sharpS).concat(sharpS).concat(bar);

        assertThat(in, is("ßßßbar"));
        MatcherAssert.assertThat(in, TaintMatcher.taintEquals(RangeChainer.range(1, 3, md).add(5, 6, md3).done()));

        IASString uc = in.toUpperCase();

        assertThat(uc, is("SSSSSSBAR"));
        MatcherAssert.assertThat(uc, TaintMatcher.taintEquals(RangeChainer.range(2, 6, md).add(8, 9, md3).done()));

        MatcherAssert.assertThat("ß".toUpperCase(), TaintMatcher.taintUninitialized());
    }

    @Test
    public void trim() {
        // trim uses substring() internally
        IASString ws = new IASString(" ");
        ((IASTaintInformation) THelper.get(ws)).addRange(0, 1, md);

        IASString in = ws.concat(foo).concat(ws);

        MatcherAssert.assertThat(in, TaintMatcher.taintEquals(RangeChainer.range(0, 1, md).add(4, 5, md).done()));

        IASString trimmed1 = in.trim();

//        assertThat(trimmed1, taintUninitialized());

        IASString trimmed2 = in.concat(foo).trim();

        MatcherAssert.assertThat(trimmed2, TaintMatcher.taintEquals(RangeChainer.range(3, 4, md).done()));
    }

    @Test
    public void replaceAll_simple() {
        IASString in = new IASString("hellofoobarfoo!");

        ((IASTaintInformation) THelper.get(in)).addRange(0, 15, md);
        ((IASTaintInformation) THelper.get(bar)).addRange(0, 2, md2);

        IASString out = in.replaceAll(foo, bar);

        assertThat(out.toString(), is("hellobarbarbar!"));
        MatcherAssert.assertThat(out, TaintMatcher.taintEquals(RangeChainer.range(0, 5, md).add(5, 7, md2).add(8, 11, md).add(11, 13, md2).add(14, 15, md).done()));
    }

    @Test
    public void replaceAll_escapedChar() {
        IASString in = new IASString("hellofoobarfoo!");
        IASString replacement = new IASString("ba\\r");

        ((IASTaintInformation) THelper.get(in)).addRange(0, 15, md);
        ((IASTaintInformation) THelper.get(replacement)).addRange(0, 2, md2);

        IASString out = in.replaceAll(foo, replacement);

        assertThat(out.toString(), is("hellobarbarbar!"));
        MatcherAssert.assertThat(out, TaintMatcher.taintEquals(RangeChainer.range(0, 5, md).add(5, 7, md2).add(8, 11, md).add(11, 13, md2).add(14, 15, md).done()));
    }

    @Test
    public void replaceAll_backreference() {
        IASString in = new IASString("hellofoofoo!");
        IASString regex = new IASString("f((o)\\2)");
        IASString replacement = new IASString("f$1bar");

        ((IASTaintInformation) THelper.get(in)).addRange(0, 12, md);
        ((IASTaintInformation) THelper.get(regex)).addRange(0, 3, md2);
        ((IASTaintInformation) THelper.get(replacement)).addRange(0, 6, md3);

        IASString out = in.replaceAll(regex, replacement);

        assertThat(out.toString(), is("hellofoobarfoobar!"));
        MatcherAssert.assertThat(out, TaintMatcher.taintEquals(
                RangeChainer.range(0, 5, md)
                        .add(5, 6, md3)
                        .add(6, 8, md)
                        .add(8, 12, md3)
                        .add(12, 14, md)
                        .add(14, 17, md3)
                        .add(17, 18, md).done()));
    }

    @Test
    public void replace_simple() {
        // replace does the same as replaceAll, but treating the needle and the
        // replacement strings as literals (so no special regex functionality)
        IASString in = new IASString("hellofoobarfoo!");

        ((IASTaintInformation) THelper.get(in)).addRange(0, 15, md);
        ((IASTaintInformation) THelper.get(bar)).addRange(0, 2, md2);

        IASString out = in.replace(foo, bar);

        assertThat(out.toString(), is("hellobarbarbar!"));
        MatcherAssert.assertThat(out, TaintMatcher.taintEquals(
                RangeChainer.range(0, 5, md)
                        .add(5, 7, md2)
                        .add(8, 11, md)
                        .add(11, 13, md2)
                        .add(14, 15, md).done()));
    }

    @Test
    public void replace_charSequence() {
        // replace calls toIASString() on the given replacement CharSequence, therefore triggering behaviour added by the CharSequenceInstrumenter
        // in case we are replacing with a CharSequence implementation that is not taint-aware out-of-the box
        IASString in = new IASString("hellofoobarfoo!");
        CharSequence replacement = HelperUtils.createCharSequence("bar");

        ((IASTaintInformation) THelper.get(in)).addRange(0, 15, md);

        IASString out = in.replace(foo, replacement);

        assertThat(out.toString(), is("hellobarbarbar!"));
        MatcherAssert.assertThat(out, TaintMatcher.taintEquals(RangeChainer.range(0, 5, md).add(8, 11, md).add(14, 15, md).done()));
    }

}
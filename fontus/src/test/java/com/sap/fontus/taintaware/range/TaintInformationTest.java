package com.sap.fontus.taintaware.range;

/**
 * Created by d059349 on 18.07.17.
 */
@SuppressWarnings("ALL")
// David: As I didn't write this Code and don't want to mess with it I suppressed the warnings.
// TODO: Work out whether we can adapt it to the style of the remaining project?
public class TaintInformationTest {
//    @Test
//    public void createRange() {
//        // Check whether TaintInformation and DebugTaintInformation create the right ranges
////        IASTaintInformation instance = new IASTaintInformation();
////        instance.addRange(0, 1, 0);
////
////        assertThat(instance.getRange(0), instanceOf(IASTaintRange.class));
////
////        instance = new DebugTaintInformation();
////        instance.addRange(0, 1, 0);
////        IASTaintRange range = instance.getRange(0);
////
////        assertThat(range, instanceOf(DebugTaintRange.class));
////        StackTraceElement[] trace = ((DebugTaintRange) range).getDebugInfo();
////        assertThat(trace[trace.length - 1].toString(), containsString(""));
//    }
//
//    @Test
//    public void addRange_splittingAnExistingRange_differentTaintSource() {
//        IASTaintInformation tI = new IASTaintInformation();
//
//        // Different TaintSource; this distinction affects only the internal processing
//        // in getRanges() (i.e. this test case tests multiple things...)
//        tI.addRange(0, 200, 0);
//        tI.addRange(50, 150, 1);
//
//        List<IASTaintRange> expected = new ArrayList<>();
//        expected.add(new IASTaintRange(0, 50, 0));
//        expected.add(new IASTaintRange(50, 150, 1));
//        expected.add(new IASTaintRange(150, 200, 0));
//
//        assertThat(tI.getRanges(0, 190), equalTo(expected));
//    }
//
//    @Test
//    public void addRange_splittingAnExistingRange_sameTaintSource() {
//        IASTaintInformation tI = new IASTaintInformation();
//
//        // Same TaintSource
//        tI.addRange(0, 200, 0);
//        tI.addRange(100, 199, 0);
//
//        List<IASTaintRange> expected = new ArrayList<>();
//        expected.add(new IASTaintRange(0, 199, 0)); // the last of the third ranges goes from 199
//        // to 200, 199 > 190 -> we therefore do not merge this one
//
//        assertThat(tI.getRanges(0, 190, true), equalTo(expected));
//
//        expected.set(0, new IASTaintRange(0, 200, 0));
//        assertThat(tI.getRanges(0, 200, true), equalTo(expected));
//
//        tI.addRange(100, 300, 0);
//
//        expected.set(0, new IASTaintRange(0, 300, 0));
//        assertThat(tI.getRanges(0, 300, true), equalTo(expected));
//    }
//
//    @Test
//    public void addRange_insertionOrderDoesntMatter() {
//        // both halfs get shrinked down to zero, so we actually override it completely
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(0, 10, 0);
//        tI.addRange(20, 30, 1);
//
//        IASTaintInformation tI2 = new IASTaintInformation();
//
//        tI2.addRange(20, 30, 1);
//        tI2.addRange(0, 10, 0);
//
//        List<IASTaintRange> expected = new ArrayList<>();
//        expected.add(new IASTaintRange(0, 10, 0));
//        expected.add(new IASTaintRange(20, 30, 1));
//
//        assertThat(expected, equalTo(tI.getRanges(0, 200)));
//        assertThat(expected, equalTo(tI2.getRanges(0, 100)));
//    }
//
//
//    @Test
//    public void addRange_splittingAnExistingRangeShrinkingItDownToZero() {
//        // both halfs get shrinked down to zero, so we actually override it completely
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(0, 100, 0);
//        tI.addRange(0, 100, 1);
//
//        List<IASTaintRange> expected = new ArrayList<>();
//        expected.add(new IASTaintRange(0, 100, 1));
//
//        assertThat(expected, equalTo(tI.getRanges(0, 200)));
//    }
//
//    @Test
//    public void addRange_coveringExistingRangeCompletely() {
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(50, 100, 0);
//        tI.addRange(0, 150, 1);
//
//        List<IASTaintRange> expected = new ArrayList<>();
//        expected.add(new IASTaintRange(0, 150, 1));
//
//        assertThat(expected, equalTo(tI.getRanges(0, 200)));
//    }
//
//    @Test
//    public void addRange_partiallyCoveringAnExistingRange() {
//        // starts in "free" area
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(50, 100, 0);
//        tI.addRange(0, 60, 1);
//
//        List<IASTaintRange> expected = new ArrayList<>();
//        expected.add(new IASTaintRange(0, 60, 1));
//        expected.add(new IASTaintRange(60, 100, 0));
//
//        assertThat(expected, equalTo(tI.getRanges(0, 200)));
//    }
//
//    @Test
//    public void addRange_coveringANeighbourCompletely1() {
//        // ends in "free" area between two ranges
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(50, 100, 0);
//        tI.addRange(0, 110, 1);
//        tI.addRange(200, 300, 2);
//
//        List<IASTaintRange> expected = new ArrayList<>();
//        expected.add(new IASTaintRange(0, 110, 1));
//        expected.add(new IASTaintRange(200, 300, 2));
//
//        assertThat(expected, equalTo(tI.getRanges(0, 201))); // getRanges' endIndex is exclusive
//    }
//
//    @Test
//    public void addRange_coveringANeighbourCompletely2() {
//        // ends in "free" area (NOT between two ranges)
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(50, 100, 0);
//        tI.addRange(0, 110, 1);
//
//        List<IASTaintRange> expected = new ArrayList<>();
//        expected.add(new IASTaintRange(0, 110, 1));
//
//        assertThat(expected, equalTo(tI.getRanges(0, 200)));
//    }
//
//    @Test
//    public void getRanges_mergedRangesOnlyIfActuallyAdjacent() {
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(0, 10, 0);
//        tI.addRange(10, 20, 0);
//        tI.addRange(21, 30, 0);
//
//        List<IASTaintRange> expected = new ArrayList<>();
//        expected.add(new IASTaintRange(0, 20, 0));
//        expected.add(new IASTaintRange(21, 30, 0));
//
//        assertThat(expected, equalTo(tI.getRanges(0, 200, true)));
//    }
//
//    @Test
//    public void getRanges_mergedRangesOnlyIfActuallyAdjacentButNotInCaseOfDebugTaintInformation() {
//        DebugTaintInformation tI = new DebugTaintInformation();
//
//        tI.addRange(5, 10, 0);
//        tI.addRange(10, 20, 0);
//        tI.addRange(21, 30, 0);
//
//        List<IASTaintRange> expected = new ArrayList<>();
//
//        assertThat(tI.getRanges(0, 200).size(), equalTo(3));
//    }
//
//    @Test
//    public void getRanges_emptyness() {
//        IASTaintInformation tI = new IASTaintInformation();
//
//        assertThat(tI.getRanges(0).size(), is(0));
//    }
//
//    @Test
//    public void getRanges_freeAreaAndExclusiveEndIndex() {
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(10, 15, 0);
//
//        // getRanges's endIndex is exclusive
//        assertThat(tI.getRanges(0, 10).size(), is(0));
//        // its startIndex is inclusive
//        assertThat(tI.getRanges(10, 11).size(), is(1));
//    }
//
//    @Test
//    public void getRanges_stripEmtpyRanges() {
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(10, 10, 0);
//        tI.addRange(11, 12, 1);
//        tI.addRange(15, 15, 2);
//        tI.addRange(20, 30, 3);
//
//        assertThat(tI.getAllRanges(), equalTo(range(11, 12, 1).add(20, 30, 3).done()));
//    }
//
//    @Test
//    public void getAllRanges() {
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(0, 10, TaintSource.TS_STRING_CREATED_FROM_CHAR_ARRAY);
//        tI.addRange(10, 20, TaintSource.TS_STRING_CREATED_FROM_CHAR_ARRAY);
//        tI.addRange(20, 30, TaintSource.TS_STRING_CREATED_FROM_CHAR_ARRAY);
//
//        assertThat(tI.getAllRanges(), is(range(0, 30, TaintSource.TS_STRING_CREATED_FROM_CHAR_ARRAY).done()));
//    }
//
//    @Test
//    public void getSingleRange() {
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(0, 10, TaintSource.TS_STRING_CREATED_FROM_CHAR_ARRAY);
//        tI.addRange(10, 20, TaintSource.TS_STRING_CREATED_FROM_CHAR_ARRAY);
//        tI.addRange(20, 30, TaintSource.TS_STRING_CREATED_FROM_CHAR_ARRAY);
//
//        assertThat(CatchException.catchException(() -> {
//            tI.getSingleRange(-2);
//        }).getMessage(), is("startIndex -2 is below 0!"));
//        assertThat(tI.getSingleRange(40), nullValue());
//        assertThat(tI.getSingleRange(10), is(new IASTaintRange(10, 20, TaintSource.TS_STRING_CREATED_FROM_CHAR_ARRAY.getId())));
//    }
//
//    @Test
//    public void addRange_emptyRange() {
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(0, 0, TaintSource.getOrCreateInstance("my_source", TaintSourceSeverityLevel.ACTUAL_SOURCE));
//
//        assertThat(tI.size(), equalTo(0));
//    }
//
//    @Test
//    public void isTainted() {
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(0, 10, 0);
//
//        assertThat(tI.isTainted(0), equalTo(true));
//        assertThat(tI.isTainted(11), equalTo(false));
//        assertThat(tI.isTainted(), equalTo(true));
//
//        tI.addRange(0, 10, TaintSource.getOrCreateInstance("SANITIZING_SOURCE1", TaintSourceSeverityLevel.SANITIZATION_FUNCTION));
//
//        assertThat(tI.isTainted(), equalTo(true));
//    }
//
//    @Test
//    public void isDangerous() {
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(0, 10, TaintSource.getOrCreateInstance("SANITIZING_SOURCE2", TaintSourceSeverityLevel.SANITIZATION_FUNCTION));
//
//        assertThat(tI.isDangerous(0), equalTo(false));
//        assertThat(tI.isDangerous(11), equalTo(false));
//        assertThat(tI.isDangerous(), equalTo(false));
//
//        tI.addRange(0, 2, TaintSource.getOrCreateInstance("DANGEROUS_SOURCE", TaintSourceSeverityLevel.ACTUAL_SOURCE));
//
//        assertThat(tI.isDangerous(2, 20), equalTo(false));
//        assertThat(tI.isDangerous(), equalTo(true));
//    }
//
//    @Test
//    public void copy() {
//        IASTaintInformation tI = new IASTaintInformation();
//
//        IASTaintInformation copy = tI.copy();
//
//        assertThat(copy, instanceOf(IASTaintInformation.class));
//
//        DebugTaintInformation debugTI = new DebugTaintInformation();
//
//        copy = debugTI.copy();
//
//        assertThat(copy, instanceOf(DebugTaintInformation.class));
//    }
//
//    @Test
//    public void adjustRanges() {
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(0, 10, 0);
//        tI.addRange(10, 20, 1);
//        tI.addRange(20, 30, 2);
//
//        List<IASTaintRange> ranges = tI.getRanges(0, 30);
//
//        IASTaintInformation.adjustRanges(ranges, 5, 25);
//
//        assertThat(ranges, equalTo(range(0, 5, 0).add(5, 15, 1).add(15, 20, 2).done()));
//    }
//
//    @Test
//    public void adjustRanges_justOneRange() {
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(0, 10, 0);
//
//        List<IASTaintRange> ranges = tI.getRanges(0, 30);
//
//        IASTaintInformation.adjustRanges(ranges, 5, 25);
//
//        assertThat(ranges, equalTo(range(0, 5, 0).done()));
//    }
//
//    @Test
//    public void removeTaintForRange_1() {
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(0, 10, 0);
//        tI.addRange(10, 20, 1);
//        tI.addRange(20, 30, 2);
//
//        tI.removeTaintForRange(15, 18);
//
//        assertThat(tI.getAllRanges(), equalTo(
//                range(0, 10, 0).
//                        add(10, 15, 1).
//                        add(18, 20, 1).
//                        add(20, 30, 2).done()));
//    }
//
//    @Test
//    public void removeTaintForRange_2() {
//        // This time we also use the leftshift-feature of removeTaintForRange()
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(0, 10, 0);
//        tI.addRange(10, 20, 1);
//        tI.addRange(20, 30, 2);
//
//        tI.removeTaintForRange(15, 18, true);
//
//        assertThat(tI.getAllRanges(), equalTo(
//                range(0, 10, 0).
//                        add(10, 17, 1).
//                        add(17, 27, 2).done()));
//    }
//
//    @Test
//    public void reversed() {
//        IASTaintInformation tI = new IASTaintInformation();
//
//        tI.addRange(0, 10, 0);
//        tI.addRange(10, 20, 1);
//        tI.addRange(20, 30, 2);
//
//        IASTaintInformation reversedTI = tI.reversed(32);
//
//        ArrayList<IASTaintRange> expected = range(2, 12, 2).add(12, 22, 1).add(22, 32, 0).done();
//
//        assertThat(tI, not(sameInstance(reversedTI)));
//
//        assertThat(reversedTI.getAllRanges(), equalTo(expected));
//    }
}

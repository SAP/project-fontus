package com.sap.fontus.sanitizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import org.junit.Test;

/**
 * JUnit tests for sanitization of possible xss attacks
 */
public class XssSanitizationTest {

    /**
     * Html attribute name tests
     */
    @Test
    public void testHtmlAttributeNameTainted_1() {
        String taintedString = "<html> <body> <img src=\"test.jpg\" width=\"200\" height=\"100\"> </body> </html>";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(19, 22, new IASTaintSource("dummy", 1234)); // src is tainted
        String encodedString = Sanitization.sanitizeHtml(taintedString, ranges);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed BUT the attribute name is tainted!
        // Never allow untrusted data in attribute names -> return null
        assertNull(encodedString);
    }

    @Test
    public void testHtmlAttributeNameTainted_2() {
        String taintedString = "<html> <body> <img src=\"test.jpg\" width=\"200\" height=\"100\"> </body> </html>";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(34, 39, new IASTaintSource("dummy", 1234)); // width is tainted
        String encodedString = Sanitization.sanitizeHtml(taintedString, ranges);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed BUT the attribute name is tainted!
        // Never allow untrusted data in attribute names -> return null
        assertNull(encodedString);
    }

    /**
     * Html attribute value tests
     */
    @Test
    public void testHtmlAttributeValueTainted_1() {
        String taintedString = "<html> <body> <img src=\"test.jpg\" width=\"200\" height=\"100\"> </body> </html>";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(24, 32, new IASTaintSource("dummy", 1234)); // test.jpg is tainted;
        String encodedString = Sanitization.sanitizeHtml(taintedString, ranges);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed and attribute values are allowed to be
        // tainted
        assertNotNull(encodedString);
        assertEquals("<html> <body> <img src=\"test.jpg\" width=\"200\" height=\"100\"> </body> </html>",
                encodedString);
    }

    @Test
    public void testHtmlAttributeValueTainted_2() {
        String taintedString = "<html> <body> <img src=\"test.jpg\" width=\"200\" height=\"100\"> </body> </html>";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(41, 44, new IASTaintSource("dummy", 1234)); // 200 is tainted
        String encodedString = Sanitization.sanitizeHtml(taintedString, ranges);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed and attribute values are allowed to be
        // tainted
        assertNotNull(encodedString);
        assertEquals("<html> <body> <img src=\"test.jpg\" width=\"200\" height=\"100\"> </body> </html>",
                encodedString);
    }

    @Test
    public void testHtmlAttributeValueTainted_3() {
        String taintedString = "<html> <body> <img src=\"t&st.jpg\" width=\"200\" height=\"100\"> </body> </html>";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(24, 32, new IASTaintSource("dummy", 1234)); // t&st.jpg is tainted
        String encodedString = Sanitization.sanitizeHtml(taintedString, ranges);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed and attribute values are allowed to be
        // tainted
        assertNotNull(encodedString);
        assertEquals("<html> <body> <img src=\"t&amp;st.jpg\" width=\"200\" height=\"100\"> </body> </html>",
                encodedString);
    }

    @Test
    public void testHtmlAttributeValueTainted_4() {
        String taintedString = "<html> <body> <img src=\"t&st.jpg\" width=\"200\" height=\"100\"> </body> </html>";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(24, 32, new IASTaintSource("dummy", 1234)); // t&st.jpg is tainted
        ranges.setTaint(41, 44, new IASTaintSource("dummy", 1234)); // 200 is tainted
        String encodedString = Sanitization.sanitizeHtml(taintedString, ranges);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed and attribute values are allowed to be
        // tainted
        assertNotNull(encodedString);
        assertEquals("<html> <body> <img src=\"t&amp;st.jpg\" width=\"200\" height=\"100\"> </body> </html>",
                encodedString);
    }

    /**
     * Html text comment test
     */
    @Test
    public void testHtmlCommentTainted() {
        String taintedString = "<html> <body> <!-- This is a comment --> <img src=\"test.jpg\" width=\"200\" height=\"100\"> </body> </html>";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(18, 37, new IASTaintSource("dummy", 1234)); // This is a comment is tainted
        String encodedString = Sanitization.sanitizeHtml(taintedString, ranges);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed BUT the text comment is tainted!
        // Never allow untrusted data in text comments -> return null
        assertNull(encodedString);
    }

    /**
     * Html text content tests
     */
    @Test
    public void testHtmlTextContentTainted_1() {
        String taintedString = "<html> <body> hello world </body> </html>";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(14, 25, new IASTaintSource("dummy", 1234)); // hello world is tainted
        String encodedString = Sanitization.sanitizeHtml(taintedString, ranges);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed and text content is allowed to be
        // tainted
        assertNotNull(encodedString);
        assertEquals("<html> <body> hello world </body> </html>", encodedString);
    }

    @Test
    public void testHtmlTextContentTainted_2() {
        String taintedString = "<html> <body> Hello & Bye! </body> </html>";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(14, 26, new IASTaintSource("dummy", 1234)); // Hello & Bye! is tainted
        String encodedString = Sanitization.sanitizeHtml(taintedString, ranges);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed and text content is allowed to be
        // tainted
        assertNotNull(encodedString);
        assertEquals("<html> <body> Hello &amp; Bye! </body> </html>", encodedString);
    }

    @Test
    public void testHtmlTextContentTainted_3() {
        String taintedString = "<html> <body> Hello & Bye! </body> </html>";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(14, 19, new IASTaintSource("dummy", 1234)); // Hello is tainted
        String encodedString = Sanitization.sanitizeHtml(taintedString, ranges);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed and text content is allowed to be
        // tainted
        assertNotNull(encodedString);
        assertEquals("<html> <body> Hello & Bye! </body> </html>", encodedString);
    }

    /**
     * Css string tests
     */
    @Test
    public void testCssStringTainted_1() {
        // inline css string
        String taintedString = "<html> <body> <h1 style=\"color:red;\">Red Heading</h1> </body> </html>";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(25, 35, new IASTaintSource("dummy", 1234)); // color:red; is tainted
        String encodedString = Sanitization.sanitizeHtml(taintedString, ranges);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed and inline css in html is allowed to be
        // tainted
        assertNotNull(encodedString);
        assertEquals("<html> <body> <h1 style=\"color:red;\">Red Heading</h1> </body> </html>", encodedString);
    }

    @Test
    public void testCssStringTainted_2() {
        // missing quotation in inline css string
        String taintedString = "<html> <body> <h1 style=color:red;>Red Heading</h1> </body> </html>";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(24, 34, new IASTaintSource("dummy", 1234)); // color:red; is tainted
        String encodedString = Sanitization.sanitizeHtml(taintedString, ranges);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed and inline css in html is allowed to be
        // tainted
        assertNotNull(encodedString);
        assertEquals("<html> <body> <h1 style=\"color:red;\">Red Heading</h1> </body> </html>", encodedString);
    }

    @Test
    public void testCssStringTainted_3() {
        // internal css string
        String taintedString = "<html> <style> body {color: red;} </style> <body> hello </body> </html>";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(15, 33, new IASTaintSource("dummy", 1234)); // body {color:red;} is tainted
        String encodedString = Sanitization.sanitizeHtml(taintedString, ranges);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed andinternal css in html is allowed to be
        // tainted
        assertNotNull(encodedString);
        assertEquals("<html> <style> body {color: red;} </style> <body> hello </body> </html>", encodedString);
    }

}

package com.sap.fontus.sanitizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import com.sap.fontus.taintaware.shared.IASTaintRange;
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
        IASTaintRange r = new IASTaintRange(19, 22, new IASTaintSource("dummy", 1234)); // src is tainted
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(r);
        String encodedString = Sanitization.sanitizeHtml(taintedString, ranges);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed BUT the attribute name is tainted!
        // Never allow untrusted data in attribute names -> return null
        assertNull(encodedString);
    }

    @Test
    public void testHtmlAttributeNameTainted_2() {
        String taintedString = "<html> <body> <img src=\"test.jpg\" width=\"200\" height=\"100\"> </body> </html>";
        IASTaintRange r = new IASTaintRange(34, 39, new IASTaintSource("dummy", 1234)); // width is tainted
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(r);
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
        IASTaintRange r = new IASTaintRange(24, 32, new IASTaintSource("dummy", 1234)); // test.jpg is tainted
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(r);
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
        IASTaintRange r = new IASTaintRange(41, 44, new IASTaintSource("dummy", 1234)); // 200 is tainted
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(r);
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
        IASTaintRange r = new IASTaintRange(24, 32, new IASTaintSource("dummy", 1234)); // t&st.jpg is tainted
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(r);
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
        IASTaintRange r1 = new IASTaintRange(24, 32, new IASTaintSource("dummy", 1234)); // t&st.jpg is tainted
        IASTaintRange r2 = new IASTaintRange(41, 44, new IASTaintSource("dummy", 1234)); // 200 is tainted
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(r1);
        ranges.add(r2);
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
        IASTaintRange r = new IASTaintRange(18, 37, new IASTaintSource("dummy", 1234)); // This is a comment is tainted
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(r);
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
        IASTaintRange r = new IASTaintRange(14, 25, new IASTaintSource("dummy", 1234)); // hello world is tainted
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(r);
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
        IASTaintRange r = new IASTaintRange(14, 26, new IASTaintSource("dummy", 1234)); // Hello & Bye! is tainted
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(r);
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
        IASTaintRange r = new IASTaintRange(14, 19, new IASTaintSource("dummy", 1234)); // Hello is tainted
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(r);
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
        IASTaintRange r = new IASTaintRange(25, 35, new IASTaintSource("dummy", 1234)); // color:red; is tainted
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(r);
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
        IASTaintRange r = new IASTaintRange(24, 34, new IASTaintSource("dummy", 1234)); // color:red; is tainted
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(r);
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
        IASTaintRange r = new IASTaintRange(15, 33, new IASTaintSource("dummy", 1234)); // body {color:red;} is tainted
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(r);
        String encodedString = Sanitization.sanitizeHtml(taintedString, ranges);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed andinternal css in html is allowed to be
        // tainted
        assertNotNull(encodedString);
        assertEquals("<html> <style> body {color: red;} </style> <body> hello </body> </html>", encodedString);
    }

    /**
     * Css URL tests
     */
    @Test
    public void testCssUrlTainted_1() {

    }

    /**
     * Uri tests
     */
    @Test
    public void testUriTainted_1() {

    }

    /**
     * Uri Component tests
     */
    @Test
    public void testUriComponentTainted_1() {

    }

    /**
     * XML attribute name tests
     */
    @Test
    public void testXmlAttributeNameTainted_1() {

    }

    /**
     * XML attribute content tests
     */
    @Test
    public void testXmlAttributeContentTainted_1() {

    }

    /**
     * XML text content tests
     */
    @Test
    public void testXmlTextContentTainted_1() {

    }

    /**
     * XML comment tests
     */
    @Test
    public void testXmlCommentTainted_1() {

    }

    /**
     * XHtml attribute name tests
     */
    @Test
    public void testXHtmlAttributeNameTainted_1() {

    }

    /**
     * XHtml attribute content tests
     */
    @Test
    public void testXHtmlAttributeContentTainted_1() {

    }

    /**
     * XHtml text content tests
     */
    @Test
    public void testXHtmlTextContentTainted_1() {

    }

    /**
     * XHtml comment tests
     */
    @Test
    public void testXHtmlCommentTainted_1() {

    }

    /**
     * CDATA tests
     */
    @Test
    public void testCdataTainted_1() {

    }

    /**
     * Java tests
     */
    @Test
    public void testJavaTainted_1() {

    }

    /**
     * JavaScript tests
     */
    @Test
    public void testJavaScriptTainted_1() {

    }

    /**
     * Mixed context tests
     */
    @Test
    public void testMixed_1() {
    }
}
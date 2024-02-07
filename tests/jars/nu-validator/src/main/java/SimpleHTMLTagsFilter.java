import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import java.io.StringReader;
import java.util.Set;

public class SimpleHTMLTagsFilter {
    public static final Set<String> blockTags = Set
            .of("address", "blockquote", "br", "dir", "div", "dl", "fieldset", "form", "h1", "h2", "h3", "h4", "h5", "h6", "hr", "noframes", "noscript", "ol", "p", "pre", "table", "ul", "li");
    public static final Set<String> toBeSkippedTags = Set
            .of("script", "style");

    public String filter(String original) {
        if (original == null) return null;
        if (original.isEmpty()) return "";

        try {
            HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
            HTMLHandler contentHandler = new HTMLHandler(original.length());
            parser.setContentHandler(contentHandler);
            parser.parse(new InputSource(new StringReader(original)));
            String text = contentHandler.toString();
            text = text.replace('\u00a0', ' ');

            return text;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class HTMLHandler extends DefaultHandler {
        private boolean collect = true;
        private boolean consumeBlanck = false;
        private final LimitedContentWriter content;

        public HTMLHandler(int size) {
            content = new LimitedContentWriter(size, Integer.MAX_VALUE);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            String elem = localName.toLowerCase();
            if ("script".equals(elem)) {
                collect = false;
                // add a single whitespace before each block element but only if not there is not already a whitespace there
            } else if ("li".equals(elem)) {
                content.append(" ");
            } else if ("br".equals(elem)) {
                content.append(" ");
            } else if (blockTags.contains(elem) && content.length() > 0 && content.charAt(content.length() - 1) != ' ') {
                consumeBlanck = true;
            }
        }

        @Override
        public void characters(char[] chars, int offset, int length) {
            if (collect) {
                if (consumeBlanck) {
                    if (content.length() > 0 && content.charAt(content.length() - 1) != ' ' && length > 0 && chars[offset] != ' ') {
                        content.append(' ');
                    }
                    consumeBlanck = false;
                }
                content.write(chars, offset, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            String elem = localName.toLowerCase();
            if ("script".equals(elem)) {
                collect = true;
            } else if ("li".equals(elem) || "p".equals(elem)) {
                content.append(" ");
            } else if (blockTags.contains(elem) && content.length() > 0 && content.charAt(content.length() - 1) != ' ') {
                consumeBlanck = true;
            }
        }

        @Override
        public String toString() {
            return content.toString();
        }
    }
}

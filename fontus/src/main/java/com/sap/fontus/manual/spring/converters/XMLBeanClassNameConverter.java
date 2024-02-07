package com.sap.fontus.manual.spring.converters;

import com.sap.fontus.Constants;
import com.sap.fontus.taintaware.unified.IASString;
import org.objectweb.asm.Type;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class XMLBeanClassNameConverter {
    private XMLBeanClassNameConverter() {
    }

    public static Document replaceStringClass(Document document) {
        replaceStringClassInNode(document);
        return document;
    }

    private static void replaceStringClassInNode(Node node) {
        if (node.hasAttributes()) {
            replaceStringClassInAttributes(node.getAttributes());
        }

        for (int i = 0; i < node.getChildNodes().getLength(); i++){
            Node child = node.getChildNodes().item(i);
            replaceStringClassInNode(child);
        }
    }

    private static void replaceStringClassInAttributes(NamedNodeMap attributes) {
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            if (Constants.STRING_FULL_NAME.equals(attribute.getNodeValue())) {
                attribute.setNodeValue(Type.getInternalName(IASString.class));
            }
        }
    }
}

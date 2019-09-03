package ar.edu.unlp.sedici.dspace.identifier.doi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrossrefHelper {

    private static final Logger log = LoggerFactory.getLogger(CrossrefHelper.class);

    /**
     * Please use the {@code getElementsFromPath} instead this, unless necessary.
     * Get all nodes objects within XML data tied to the specified XPATH expression.
     * @return a list of all nodes objects tied to XPATH or @null if the XPATH does not match.
     */
    public static List<?> getNodesFromPath(Element root, String xpath_expression, String ns_prefix, String ns_uri) {
        List<?> targetNodes = null;
        XPath xpathDOI;
        Document doc;
        try {
            xpathDOI = XPath.newInstance(xpath_expression);
            xpathDOI.addNamespace(ns_prefix, ns_uri);
            if(root.getDocument() != null) {
                doc = root.getDocument();
            } else {
                doc = new Document(root);
            }
            targetNodes = xpathDOI.selectNodes(doc);
        } catch (JDOMException e) {
            log.error("Incorrect XPATH expression!! Please check it. (XPATH =" + xpath_expression  + ")",  e.getMessage());
            //continue the normal code and return @null if this excepcion is raised...
        }
        return targetNodes;
    }

    /**
     * Get an Element node object within XML data tied to the specified XPATH expression.
     */
    public static List<Element> getElementsFromPath(Element root, String xpath_expression, String ns_prefix, String ns_uri) {
        List<?> nodes = getNodesFromPath(root, xpath_expression, ns_prefix, ns_uri);
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        List<Element> elements = new ArrayList<Element>();
        for (Object node : nodes) {
            elements.add((Element) node);
        }
        return elements;
    }

    /**
     * Get an Element node object within XML data tied to the specified XPATH expression.
     */
    public static Element getElementFromPath(Element root, String xpath_expression, String ns_prefix, String ns_uri) {
        Element element = null;
        List<?> nodes = getNodesFromPath(root, xpath_expression, ns_prefix, ns_uri);
        if(nodes != null && !nodes.isEmpty()) {
            element = (Element)(nodes.get(0));
        }
        return element;
    }

    /**
     * Get an Attribute node object within XML data tied to the specified XPATH expression.
     */
    public static Attribute getAttributeFromPath(Element root, String xpath_expression, String ns_prefix, String ns_uri) {
        Attribute attribute = null;
        List<?> nodes = getNodesFromPath(root, xpath_expression, ns_prefix, ns_uri);
        if(nodes != null && !nodes.isEmpty()) {
            attribute = (Attribute)(nodes.get(0));
        }
        return attribute;
    }
    
    /**
     * Get an Element within XML data tied to the specified XPATH expression. The namespace URI and prefix are considered empty.
     * @return the Element node object tied to XPATH or @null if the XPATH does not match.
     */
    public static Element getElementFromPath(Element root, String xpath_expression) {
        return getElementFromPath(root, xpath_expression, "", "");
    }
    /**
     * Get an Attribute within XML data tied to the specified XPATH expression. The namespace URI and prefix are considered empty.
     * @return the Attribute node object tied to XPATH or @null if the XPATH does not match.
     */
    public static Attribute getAttributeFromPath(Element root, String xpath_expression) {
        return getAttributeFromPath(root, xpath_expression, "", "");
    }
    

    /**
     * Convert a target String in XML format to an JDom Documento object.
     * @param XMLString     the String to convert
     * @return a Document based on XMLString parameter content. Return null in case of empty or null for XMLString.
     * @throws JDOMException     when cannot parse XMLString
     */
    public static Document parseXMLContent(String XMLString) throws JDOMException {
        if (XMLString == null){
            return null;
        }
        try {
            SAXBuilder builder = new SAXBuilder();
            InputStream stream = new ByteArrayInputStream(XMLString.getBytes("UTF-8"));
            return builder.build(stream);
        } catch (IOException e) {
            throw new RuntimeException("Got an IOException while reading from a string?!", e);
        }
    }
    
}

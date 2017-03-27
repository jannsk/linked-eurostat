package loader;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ontologycentral.estatwrap.webapp.Listener;

import net.sf.saxon.dom.DOMNodeList;
import net.sf.saxon.lib.NamespaceConstant;

public class TableOfContentsLoader {
	Logger _log = Logger.getLogger(this.getClass().getName());
	
	private Document toc;
	private NamespaceContext context;
	
	public TableOfContentsLoader() {
		FileLoader tocLoader;
		try {
			tocLoader = new FileLoader(new URL(Listener.URI_PREFIX + "?file=table_of_contents.xml"));
			toc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(tocLoader.get());
			context = new NamespaceContextMap("nt", "urn:eu.europa.ec.eurostat.navtree");
		} catch (Exception e) {
			_log.info("Cannot initialize TableOfContentsLoader");
			e.printStackTrace();
			System.exit(1);
		}
		
	}
	
	public ArrayList<HashMap<String, String>> getAll() {
		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		DOMNodeList matches = null;
		try {
			matches = executeXPathQuery("//nt:leaf");
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int j = 0; j < matches.getLength(); j++) {
			Node match = matches.item(j);
			HashMap<String, String> map = new HashMap<String, String>();
			NodeList childs = match.getChildNodes();
			for (int k = 0; k < childs.getLength(); k++) {
				Node item = childs.item(k);
				if (item.getNodeName().equals("nt:code")) {
					map.put("code", item.getTextContent().trim());
				}
				if (item.getNodeName().equals("nt:title") && item.getAttributes().getNamedItem("language").getTextContent().equals("en")) {
					map.put("title", escapeXml(item.getTextContent().split(" per ")[0].split(" by ")[0].trim()));
				}
				if (item.getNodeName().equals("nt:lastUpdate")) {
					map.put("lastUpdate", escapeXml(item.getTextContent().trim()));
				}	
			}
			if (map.size() == 3) {
				result.add(map);
			}
		}
		return result;
	}
	
	public String getTitle(String dsId) {
		DOMNodeList matches = null;
		try {
			matches = executeXPathQuery("//nt:leaf/nt:code[text()=\""+dsId+"\"]/../nt:title[@language=\"en\"]/text()");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		if (matches.getLength() > 0) {
			Node node = matches.item(0);
			return escapeXml(node.getTextContent().split(" per ")[0].split(" by ")[0].trim());
		} else {
			return "";
		}
	}
	
	public String getDescription(String dsId) {
		DOMNodeList matches = null;
		try {
			matches = executeXPathQuery("//nt:leaf/nt:code[text()=\""+dsId+"\"]/../nt:shortDescription[@language=\"en\"]/text()");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		if (matches.getLength() > 0) {
			Node node = matches.item(0);
			if (node.getTextContent() == null) {
				return "";
			}
			return escapeXml(node.getTextContent());
		} else {
			return "";
		}
	}
	
	public String getUnit(String dsId) {
		DOMNodeList matches = null;
		try {
			matches = executeXPathQuery("//nt:leaf/nt:code[text()=\""+dsId+"\"]/../nt:unit[@language=\"en\"]/text()");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if (matches.getLength() > 0) {
			Node node = matches.item(0);
			return escapeXml(node.getTextContent());
		} else {
			return null;
		}
	}
	
	public String getSMDXDownloadLink(String dsId) {
		DOMNodeList matches = null;
		try {
			matches = executeXPathQuery("//nt:leaf/nt:code[text()=\""+dsId+"\"]/../nt:downloadLink[@format=\"sdmx\"]/text()");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if (matches.getLength() > 0) {
			Node node = matches.item(0);
			return node.getTextContent();
		} else {
			return null;
		}
	}
	
	private DOMNodeList executeXPathQuery(String xPathQuery) throws XPathFactoryConfigurationException, XPathExpressionException {
		System.setProperty("javax.xml.xpath.XPathFactory:" + NamespaceConstant.OBJECT_MODEL_SAXON, "net.sf.saxon.xpath.XPathFactoryImpl");
        XPathFactory xPathFactory = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
        XPath xPath = xPathFactory.newXPath();
        xPath.setNamespaceContext(context);
        
        _log.info("xPath Query: "+xPathQuery);
        
        XPathExpression xPathExpression = xPath.compile(xPathQuery);
        DOMNodeList matches = (DOMNodeList) xPathExpression.evaluate(toc, XPathConstants.NODESET);
//        _log.info("Results: "+matches.getLength());
        return matches;
	}
	
	private String escapeXml(String text) {
		StringBuilder escapedXML = new StringBuilder();
	    for (int i = 0; i < text.length(); i++) {
	        char c = text.charAt(i);
	        switch (c) {
	        case '<':
	            escapedXML.append("&lt;");
	            break;
	        case '>':
	            escapedXML.append("&gt;");
	            break;
	        case '\"':
	            escapedXML.append("&quot;");
	            break;
	        case '&':
	            escapedXML.append("&amp;");
	            break;
	        case '\'':
	            escapedXML.append("&apos;");
	            break;
	        default:
	            if (c > 0x7e) {
	                escapedXML.append("&#" + ((int) c) + ";");
	            } else
	                escapedXML.append(c);
	        }
	    }
	    return escapedXML.toString();
	}
	
	
}

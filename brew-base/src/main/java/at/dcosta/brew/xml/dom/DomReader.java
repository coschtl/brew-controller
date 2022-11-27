package at.dcosta.brew.xml.dom;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import at.dcosta.brew.util.IOUtils;

public class DomReader {
	private at.dcosta.brew.xml.dom.Document document;

	public at.dcosta.brew.xml.dom.Document read(File file)
			throws ParserConfigurationException, SAXException, IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			return read(in);
		} finally {
			IOUtils.close(in);
		}
	}

	public at.dcosta.brew.xml.dom.Document read(InputStream in)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		db = dbf.newDocumentBuilder();
		Document domDoc = db.parse(in);
		Element domRroot = domDoc.getDocumentElement();
		document = new at.dcosta.brew.xml.dom.Document(domRroot.getTagName());
		parse(domRroot, document.getRootElement());
		return document;

	}

	private void addTextNode(StringBuilder text, Node aktNode) {
		String textString = text.toString().trim();
		if (textString.length() > 0) {
			aktNode.addChild(new Text(textString));
			text.setLength(0);
		}
	}

	private Node createElement(Element domElement) {
		at.dcosta.brew.xml.dom.Element element = new at.dcosta.brew.xml.dom.Element(domElement.getTagName());
		NamedNodeMap attributes = domElement.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			org.w3c.dom.Node attrib = attributes.item(i);
			element.addAttribute(attrib.getNodeName(), attrib.getNodeValue());
		}
		return element;
	}

	private void parse(Element domElement, Node aktNode) {
		NodeList childNodes = domElement.getChildNodes();
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < childNodes.getLength(); i++) {
			org.w3c.dom.Node domChildNode = childNodes.item(i);
			if (domChildNode instanceof Element) {
				Element domChildElement = (Element) domChildNode;
				addTextNode(text, aktNode);
				Node child = createElement(domChildElement);
				aktNode.addChild(child);
				parse(domChildElement, child);
			} else if (domChildNode instanceof CharacterData) {
				text.append(((CharacterData) domChildNode).getData());
			}
		}
		addTextNode(text, aktNode);
	}

}

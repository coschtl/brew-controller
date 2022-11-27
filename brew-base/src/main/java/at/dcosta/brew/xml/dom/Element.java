package at.dcosta.brew.xml.dom;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Element extends Node {

	private final String name;
	private final Map<String, String> attributes;

	public Element(String name) {
		this(name, new LinkedHashMap<String, String>());
	}

	public Element(String name, Map<String, String> attributes) {
		this.name = name;
		this.attributes = attributes;
	}

	public Element addAttribute(String name, int value) {
		return addAttribute(name, Integer.toString(value));
	}

	public Element addAttribute(String name, String value) {
		attributes.put(name, value);
		return this;
	}

	@Override
	public Element addChild(Node child) {
		return (Element) super.addChild(child);
	}

	public String getAttribute(String name) {
		return attributes.get(name);
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public Iterator<Element> getElementIterator() {

		final Iterator<Node> nodeIterator = getChildren().iterator();

		return new Iterator<Element>() {

			private Element next;

			@Override
			public boolean hasNext() {
				moveToNext();
				return next != null;
			}

			@Override
			public Element next() {
				moveToNext();
				Element nextElement = next;
				next = null;
				return nextElement;
			}

			private void moveToNext() {
				while (next == null && nodeIterator.hasNext()) {
					Node nextNode = nodeIterator.next();
					if (nextNode instanceof Element) {
						next = (Element) nextNode;
					}
				}
			}
		};
	}

	public Element getFirstChild(String name) {
		Iterator<Element> it = getElementIterator();
		if (!it.hasNext()) {
			return null;
		}
		Element child = it.next();
		if (name == null ||name.equals(child.getName() )) {
			return child;
		}
		while (it.hasNext()) {
			child = it.next();
			if (name.equals(child.getName())) {
				return child;
			}
		}
		return null;
	}
	
	public String getName() {
		return name;
	}
	
	public Text getText() {
		if (!getChildren().isEmpty()) {
			return (Text) getChildren().iterator().next();
		}
		return null;
	}

	public boolean hasChildElements() {
		return getElementIterator().hasNext();
	}

	@Override
	public String toString() {
		return name;
	}
}

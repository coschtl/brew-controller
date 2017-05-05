package at.dcosta.brew.xml.dom;

public class Document {

	private Element root;

	public Document(String rootElementName) {
		root = new Element(rootElementName);
	}

	public Element addElement(Element element) {
		root.addChild(element);
		return element;
	}
	
	public Element getRootElement() {
		return root;
	}

	public void accept(DomWriter visitor) {
		visitor.visit(root);
	}

}

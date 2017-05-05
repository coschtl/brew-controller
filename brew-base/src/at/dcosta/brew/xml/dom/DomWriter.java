package at.dcosta.brew.xml.dom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map.Entry;

public class DomWriter {

	private StringBuilder xml;
	private int depth;
	private String indentationString = "\t";

	public void setIndentationString(String indentationString) {
		this.indentationString = indentationString;
	}

	public DomWriter() {
		xml = new StringBuilder();
	}

	private void visit(Document document) {
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
		document.accept(this);
	}

	public void write(Document document, File file) throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		write(document, out);
		out.close();
	}

	public void write(Document document, OutputStream out) throws IOException {
		visit(document);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		writer.write(xml.toString());
		writer.flush();
		writer.close();
	}

	public void visit(Node node) {
		if (node instanceof Text) {
			visit((Text) node);
		} else if (node instanceof Element) {
			visit((Element) node);
		} else {
			node.accept(this);
		}
	}

	public void visit(Text text) {
		xml.append(text.getText());
	}

	public void visit(Element element) {
		indent();
		startElement(element.getName());
		Iterator<Entry<String, String>> it = element.getAttributes().entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> attribute = it.next();
			addAttribute(attribute.getKey(), attribute.getValue());
		}
		closeElement();
		depth++;
		element.accept(this);
		depth--;
		if (element.hasChildElements()) {
			indent();
		}
		endElement(element.getName());
	}


	private DomWriter addAttribute(String name, String value) {
		xml.append(' ').append(name).append("=\"").append(value).append("\"");
		return this;
	}

	private DomWriter startElement(String name) {
		xml.append('<').append(name);
		return this;
	}

	private DomWriter closeElement() {
		xml.append('>');
		return this;
	}

	private void indent() {
		xml.append("\r\n");
		for (int i = 0; i < depth; i++) {
			xml.append(indentationString);
		}
	}

	private DomWriter endElement(String name) {
		xml.append("</").append(name).append('>');
		return this;
	}

}

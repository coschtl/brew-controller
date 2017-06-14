package at.dcosta.brew.xml.dom;

import java.util.ArrayList;
import java.util.List;

public class Node {
	
	private final List<Node> children;
	
	public Node() {
		this.children = new ArrayList<>();
	}
	
	public void accept(DomWriter visitor) {
		for (Node child : getChildren()) {
			visitor.visit(child);
		}
	}
	
	public Node addChild(Node child) {
		children.add( child);
		return  this;
	}
	
	public List<Node> getChildren() {
		return children;
	}
	
}

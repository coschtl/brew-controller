package at.dcosta.brew.xml.dom;

public class Text extends Node {

	private final String text;

	public Text( String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public String getTextTrim() {
		return text.trim();
	}
	
	@Override
	public String toString() {
		return text;
	}
	
}

package at.dcosta.brew.server;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class RestStringList implements Serializable {

	private static final long serialVersionUID = 1L;

	public RestStringList() {
		// jaxb
	}

	public RestStringList(List<String> list) {
		this.list = list;
	}

	@XmlElement
	private List<String> list;

	public List<String> getList() {
		return list;
	}

	public void setList(List<String> list) {
		this.list = list;
	}

}

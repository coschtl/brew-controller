package at.dcosta.brew.server;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class ChartData {
	@XmlElement
	private List<String> labels;
	@XmlElement
	private List<String> data;

	public ChartData() {
		labels = new ArrayList<String>();
		data = new ArrayList<String>();
	}

	public void addDataValue(String dataValue) {
		data.add(dataValue);
	}

	public void addLabel(String label) {
		labels.add(label);
	}

}

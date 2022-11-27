package at.dcosta.brew.server;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;

public class ChartPoint {
	@XmlElement
	private Date x;
	@XmlElement
	private String y;

	public Date getX() {
		return x;
	}

	public String getY() {
		return y;
	}

	public void setX(Date x) {
		this.x = x;
	}

	public void setY(String y) {
		this.y = y;
	}

}

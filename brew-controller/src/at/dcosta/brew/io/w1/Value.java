package at.dcosta.brew.io.w1;

public class Value {

	private double value;

	public synchronized double getValue() {
		return value;
	}

	public synchronized void setValue(double value) {
		this.value = value;
	}

}

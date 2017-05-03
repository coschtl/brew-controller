package at.dcosta.brew.server.gui;

public final class JspUtil {

	public static String getOnOffImageName(Boolean state, String prefix, String suffix) {
		StringBuilder b = new StringBuilder(prefix);
		if (state != null && state.booleanValue()) {
			b.append("ON");
		} else {
			b.append("OFF");
		}
		b.append(suffix);
		return b.toString();
	}

	public static String getOnOffPngImageName(Boolean state, String prefix) {
		return getOnOffImageName(state, prefix, ".png");
	}

	public static String getValue(Double value, String scale) {
		if (value == null) {
			return "";
		}
		return getValue(value.doubleValue(), scale);
	}
	public static String getValue(double value, String scale) {
		return new StringBuilder().append(value).append("&nbsp;").append(scale).toString();
	}

}

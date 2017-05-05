package at.dcosta.brew.util;

import java.util.Calendar;

public class StringUtil {

	public static String getDateTimeString( boolean appendMillis) {
		StringBuilder b = new StringBuilder();
		Calendar cal = Calendar.getInstance();
		b.append(getFixedString(cal.get(Calendar.YEAR), 4));
		b.append(getFixedString(cal.get(Calendar.MONTH) + 1, 2));
		b.append(getFixedString(cal.get(Calendar.DAY_OF_MONTH), 2));
		b.append("_");
		b.append(getFixedString(cal.get(Calendar.HOUR_OF_DAY), 2));
		b.append(getFixedString(cal.get(Calendar.MINUTE), 2));
		b.append(getFixedString(cal.get(Calendar.SECOND), 2));
		if (appendMillis) {
			b.append("_");
			b.append(cal.get(Calendar.MILLISECOND));
		}
		return b.toString();
	}

	public static String getFixedString(int number, int digits) {
		StringBuilder b = new StringBuilder();
		String numberString = Integer.toString(number);
		int toAdd = digits - numberString.length();
		for (int i = 0; i < toAdd; i++) {
			b.append('0');
		}
		return b.append(numberString).toString();
	}
}

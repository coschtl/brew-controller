package at.dcosta.brew.util;

public class FileUtil {
	
	public static String getFilename(String prefix, String suffix, boolean appendMillis) {
		StringBuilder b = new StringBuilder(prefix);
		b.append(StringUtil.getDateTimeString(false));
		b.append(suffix);
		return b.toString();
	}

}

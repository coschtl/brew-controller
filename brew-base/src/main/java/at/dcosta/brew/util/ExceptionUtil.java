package at.dcosta.brew.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public final class ExceptionUtil {

	public static String toString(Exception e) {
		StringBuilder b = new StringBuilder();
		b.append(e.getClass().getName()).append(": ").append(e.getMessage()).append("\n\nStacktrace:\n");
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bout);
		e.printStackTrace(ps);
		ps.close();
		b.append(new String(bout.toByteArray()));
		return b.toString();
	}
}

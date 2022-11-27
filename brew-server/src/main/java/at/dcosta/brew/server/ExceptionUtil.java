package at.dcosta.brew.server;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

public final class ExceptionUtil {

	public static String getStacktrace(Exception ex) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bos);
		ex.printStackTrace(ps);
		ps.flush();
		ps.close();
		return new String(bos.toByteArray(), Charset.forName("utf-8"));
	}

}

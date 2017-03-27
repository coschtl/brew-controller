package at.dcosta.brew.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public class IOUtils {

	public static void close(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (Exception e) {
			}
		}
	}

	public static void close(OutputStream out) {
		if (out != null) {
			try {
				out.close();
			} catch (Exception e) {
			}
		}
	}

	public static void close(Reader r) {
		if (r != null) {
			try {
				r.close();
			} catch (Exception e) {
			}
		}
	}

	public static void close(Writer w) {
		if (w != null) {
			try {
				w.close();
			} catch (Exception e) {
			}
		}
	}

}

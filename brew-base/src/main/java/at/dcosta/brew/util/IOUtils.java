package at.dcosta.brew.util;

import java.io.Closeable;

public class IOUtils {

	public static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
			}
		}
	}

}

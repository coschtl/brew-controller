package at.dcosta.brew.util;

public final class ThreadUtil {

	public static final int SLEEP_MILLIS_DEFAULT = 100;

	public static final long ONE_SECOND = 1000l;
	public static final long ONE_MINUTE = ONE_SECOND * 60l;
	public static final long ONE_HOUR = ONE_MINUTE * 60l;
	public static final long TEN_HOURS = ONE_HOUR * 10l;

	public static void sleepDefaultMillis() {
		sleepMillis(SLEEP_MILLIS_DEFAULT);
	}

	public static void sleepMillis(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// ignore
		}
	}

	public static void sleepMinutes(long minutes) {
		sleepMillis(ONE_MINUTE * minutes);
	}

	public static void sleepSeconds(long seconds) {
		sleepMillis(ONE_SECOND * seconds);
	}

}

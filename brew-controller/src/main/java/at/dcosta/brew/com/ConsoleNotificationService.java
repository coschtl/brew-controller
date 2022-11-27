package at.dcosta.brew.com;

public class ConsoleNotificationService implements Notifier {

	@Override
	public long getIgnoreSameSubjectTimeoutMillis() {
		return 1000l * 5;
	}

	@Override
	public void sendNotification(Notification notification) {
		System.out.println(notification);
	}

}

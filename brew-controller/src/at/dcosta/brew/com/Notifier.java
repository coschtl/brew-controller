package at.dcosta.brew.com;

public interface Notifier {

	public long getIgnoreSameSubjectTimeoutMillis();

	public void sendNotification(Notification notification);

}

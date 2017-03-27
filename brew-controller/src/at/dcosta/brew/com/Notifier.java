package at.dcosta.brew.com;

public interface Notifier {
	
	public void sendNotification(NotificationType notificationType, String subject, String message);

}

package at.dcosta.brew.com;

public interface NotificationService {
	
	public void sendNotification(NotificationType notificationType, String subject, String message);

}

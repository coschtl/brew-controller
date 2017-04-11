package at.dcosta.brew.com;

import java.util.HashMap;
import java.util.Map;

public class NotificationService {

	private final Notifier[] notifiers;
	private final Map<String, Long> subject2SendTime;

	public NotificationService(int cookbookEntryId) {
		// TODO: read the requested NotificationServices from the configuration
		// notifiers = new Notifier[] { new MailNotificationService(), new
		// JournalNotificationService(cookbookEntryId) };
		notifiers = new Notifier[] { new ConsoleNotificationService() };
		subject2SendTime = new HashMap<>();
	}

	public Notifier[] getNotificationServices() {
		return notifiers;
	}

	public void sendNotification(Notification notification) {
		Long lastNotification = subject2SendTime.get(notification.getSubject());
		long now = System.currentTimeMillis();
		for (Notifier notifier : notifiers) {
			if (lastNotification != null
					&& lastNotification.longValue() + notifier.getIgnoreSameSubjectTimeoutMillis() > now) {
				continue;
			}
			notifier.sendNotification(notification);
		}
		subject2SendTime.put(notification.getSubject(), now);
	}

	public void sendNotification(NotificationType notificationType, String subject, String message) {
		sendNotification(new Notification(notificationType, subject, message));
	}

}

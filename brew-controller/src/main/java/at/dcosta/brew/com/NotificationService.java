package at.dcosta.brew.com;

import java.util.HashMap;
import java.util.Map;

public class NotificationService {

	private final Notifier[] notifiers;
	private final Map<String, Long> subject2SendTime;

	public NotificationService(int brewId) {
		this(new Notifier[] { new ConsoleNotificationService(), new JournalNotificationService(brewId),
				new TelegramNotificationService() });
	}

	protected NotificationService(Notifier... notifiers) {
		this.notifiers = notifiers;
		subject2SendTime = new HashMap<>();
	}

	public void sendNotification(Exception e) {
		sendNotification(new Notification(e));
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

	public void sendNotification(NotificationType notificationType, String id, Object... variables) {
		sendNotification(new Notification(notificationType, id, variables));
	}

}

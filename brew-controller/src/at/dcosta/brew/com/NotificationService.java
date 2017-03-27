package at.dcosta.brew.com;

public class NotificationService implements Notifier {

	private Notifier[] notifiers;

	private NotificationService(int cookbookEntryId) {
		// TODO: read the requested NotificationServices from the configuration
		notifiers = new Notifier[] { new MailNotificationService(), new JournalNotificationService(cookbookEntryId) };
	}

	public Notifier[] getNotificationServices() {
		return notifiers;
	}

	@Override
	public void sendNotification(NotificationType notificationType, String subject, String message) {
		for (Notifier notifier : notifiers) {
			notifier.sendNotification(notificationType, subject, message);
		}
	}

}

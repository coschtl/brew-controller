package at.dcosta.brew.com;

import at.dcosta.brew.db.Journal;

public class JournalNotificationService implements Notifier {

	private int cookbookEntryId;
	private Journal journal = new Journal();

	public JournalNotificationService(int cookbookEntryId) {
		this.cookbookEntryId = cookbookEntryId;
	}

	@Override
	public long getIgnoreSameSubjectTimeoutMillis() {
		return 1000l;
	}

	@Override
	public void sendNotification(Notification notification) {
		if (notification.getNotificationType() == NotificationType.WARNING
				|| notification.getNotificationType() == NotificationType.ERROR) {
			journal.addEntry(cookbookEntryId, notification.getNotificationType().toString(), notification.getMessage());
		}
	}

}

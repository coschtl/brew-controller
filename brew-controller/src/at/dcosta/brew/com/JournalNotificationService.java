package at.dcosta.brew.com;

import at.dcosta.brew.db.Journal;

public class JournalNotificationService implements Notifier {

	private int cookbookEntryId;
	private Journal journal = new Journal();

	public JournalNotificationService(int cookbookEntryId) {
		this.cookbookEntryId = cookbookEntryId;
	}

	@Override
	public void sendNotification(NotificationType notificationType, String subject, String message) {
		if (notificationType == NotificationType.WARNING || notificationType == NotificationType.ERROR) {
			journal.addEntry(cookbookEntryId, notificationType.toString(), message);
		}
	}

}

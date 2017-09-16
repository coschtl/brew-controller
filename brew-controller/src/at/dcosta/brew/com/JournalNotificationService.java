package at.dcosta.brew.com;

import at.dcosta.brew.db.Journal;

public class JournalNotificationService implements Notifier {

	private int brewId;
	private Journal journal = new Journal();

	public JournalNotificationService(int brewId) {
		this.brewId = brewId;
	}

	@Override
	public long getIgnoreSameSubjectTimeoutMillis() {
		return 1000l;
	}

	@Override
	public void sendNotification(Notification notification) {
		if (notification.getNotificationType() == NotificationType.WARNING
				|| notification.getNotificationType() == NotificationType.ERROR) {
			journal.addEntry(brewId, notification.getNotificationType().toString(), notification.getMessage());
		}
	}

}

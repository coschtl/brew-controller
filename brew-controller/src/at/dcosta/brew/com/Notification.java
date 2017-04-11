package at.dcosta.brew.com;

import java.text.DateFormat;
import java.util.Date;

public class Notification {

	public static DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

	private final NotificationType notificationType;
	private final String subject;
	private final String message;
	private final Date notificationTime;

	public Notification(NotificationType notificationType, String subject, String message) {
		this.notificationType = notificationType;
		this.subject = subject;
		this.message = message;
		this.notificationTime = new Date();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Notification other = (Notification) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (notificationType != other.notificationType)
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		return true;
	}

	public String getMessage() {
		return message;
	}

	public Date getNotificationTime() {
		return notificationTime;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public String getSubject() {
		return subject;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((notificationType == null) ? 0 : notificationType.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return new StringBuilder().append(DATE_FORMAT.format(getNotificationTime())).append(": ")
				.append(getNotificationType().toString()).append(": ").append(getSubject()).append(": ")
				.append(getMessage()).toString();
	}

}

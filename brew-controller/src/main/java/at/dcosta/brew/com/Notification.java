package at.dcosta.brew.com;

import java.text.DateFormat;
import java.util.Date;

import at.dcosta.brew.msg.I18NTexts;
import at.dcosta.brew.msg.I18NTexts.BundleMessage;
import at.dcosta.brew.msg.IdBasedMessage;
import at.dcosta.brew.msg.NotificationTexts;
import at.dcosta.brew.util.ExceptionUtil;

public class Notification implements IdBasedMessage {

	private static final long serialVersionUID = 1L;

	private final NotificationType notificationType;
	private final Date notificationTime;

	private final String id;
	private final String subject, message;

	public Notification(Exception exception) {
		this.notificationType = NotificationType.ERROR;
		this.id = null;
		this.notificationTime = new Date();
		this.subject = NotificationTexts.getMessage("fatalSystemError").getMessage();
		this.message = ExceptionUtil.toString(exception);
	}

	public Notification(NotificationType notificationType, String id, Object... variables) {
		this.notificationType = notificationType;
		this.id = id;
		this.notificationTime = new Date();
		BundleMessage bt = NotificationTexts.getMessage(id, variables);
		this.subject = bt.getSubject();
		this.message = bt.getMessage();
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
		if (id != other.id)
			return false;
		if (notificationType != other.notificationType)
			return false;
		return true;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
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
	public String toString() {
		DateFormat df = I18NTexts.getDateTimeFormat(DateFormat.MEDIUM);
		return new StringBuilder().append(df.format(getNotificationTime())).append(": ")
				.append(getNotificationType().toString()).append(": ").append(getSubject()).append(": ")
				.append(getMessage()).toString();
	}

}

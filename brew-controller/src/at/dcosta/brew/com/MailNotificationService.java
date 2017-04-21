package at.dcosta.brew.com;

import static at.dcosta.brew.Configuration.MAIL_ACCOUNT;
import static at.dcosta.brew.Configuration.MAIL_PASSWORD;
import static at.dcosta.brew.Configuration.MAIL_RECIPIENTS;
import static at.dcosta.brew.Configuration.MAIL_USER;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import at.dcosta.brew.Configuration;
import at.dcosta.brew.ConfigurationException;
import at.dcosta.brew.util.ThreadManager;

public class MailNotificationService implements Notifier {

	static final Logger LOGGER = Logger.getLogger(MailNotificationService.class.getName());

	private Configuration config;

	public MailNotificationService() {
		this.config = Configuration.getInstance();
	}

	@Override
	public long getIgnoreSameSubjectTimeoutMillis() {
		return 1000l * 5;
	}

	@Override
	public void sendNotification(Notification notification) {

		ThreadManager.getInstance().newThread(new Runnable() {

			@Override
			public void run() {
				try {
					Message msg = new MimeMessage(getGMailSession());

					String[] recipients = config.getStringArray(MAIL_RECIPIENTS);
					InternetAddress[] addressesTo = new InternetAddress[recipients.length];
					for (int i = 0; i < recipients.length; i++) {
						addressesTo[i] = new InternetAddress(recipients[i]);
					}
					msg.setRecipients(Message.RecipientType.TO, addressesTo);
					msg.setFrom(new InternetAddress(config.getString(MAIL_USER)));

					msg.setSubject(notification.getNotificationType().toString() + ": " + notification.getSubject());
					msg.setContent(
							notification.getMessage() + "\n\nNotificationTime: "
									+ Notification.DATE_FORMAT.format(notification.getNotificationTime()),
							"text/plain");
					Transport.send(msg);
				} catch (Exception e) {
					System.out.println("can not sent Mail-Message: " + e.toString());
					LOGGER.log(Level.SEVERE, "can not sent Mail-Message: " + e.toString(), e);
				}
			}

		}, "sendMail").start();
	}

	private Session getGMailSession() {

		return Session.getInstance(getMailProperties(), new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(config.getString(MAIL_USER), config.getString(MAIL_PASSWORD));
			}
		});
	}

	private Properties getMailProperties() {
		String account = config.getString(MAIL_ACCOUNT);
		if (!"gmail".equalsIgnoreCase(account)) {
			throw new ConfigurationException("Unknown email account: " + account);
		}

		Properties props = new Properties();

		// Zum Empfangen
		props.setProperty("mail.pop3.host", "pop.gmail.com");
		props.setProperty("mail.pop3.user", config.getString(MAIL_USER));
		props.setProperty("mail.pop3.password", config.getString(MAIL_PASSWORD));
		props.setProperty("mail.pop3.port", "995");
		props.setProperty("mail.pop3.auth", "true");
		props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

		// Zum Senden
		props.setProperty("mail.smtp.host", "smtp.gmail.com");
		props.setProperty("mail.smtp.auth", "true");
		props.setProperty("mail.smtp.port", "465");
		props.setProperty("mail.smtp.socketFactory.port", "465");
		props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.setProperty("mail.smtp.socketFactory.fallback", "false");

		return props;
	}

}

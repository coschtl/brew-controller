package at.dcosta.brew.com;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import at.dcosta.brew.Configuration;
import at.dcosta.brew.util.ThreadManager;

public class MailNotificationService {

	static final Logger LOGGER = Logger.getLogger(MailNotificationService.class.getName());

	private Configuration config;

	public MailNotificationService(Configuration config) {
		this.config = config;
	}

	public Session getGMailSession(String user, String password) {
		final Properties props = new Properties();

		// Zum Empfangen
		props.setProperty("mail.pop3.host", "pop.gmail.com");
		props.setProperty("mail.pop3.user", user);
		props.setProperty("mail.pop3.password", password);
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

		return Session.getInstance(props, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(props.getProperty("mail.pop3.user"),
						props.getProperty("mail.pop3.password"));
			}
		});
	}

	public void sendNotification(String recipient, String subject, String message) throws MessagingException {
		String user = "pi.brauerei@gmail.com";
		String password = "bierBrauen";
		// String user = config.getMailUser();
		// String password = config.getMailPassword();

		ThreadManager.getInstance().newThread(new Runnable() {

			@Override
			public void run() {
				try {
					Message msg = new MimeMessage(getGMailSession(user, password));

					InternetAddress addressTo = new InternetAddress(recipient);
					msg.setRecipient(Message.RecipientType.TO, addressTo);
					msg.setFrom(new InternetAddress(user));

					msg.setSubject(subject);
					msg.setContent(message, "text/plain");
					Transport.send(msg);
				} catch (Exception e) {
					System.out.println("can not sent Mail-Message: " + e.toString());
					LOGGER.log(Level.SEVERE, "can not sent Mail-Message: " + e.toString(), e);
				}
			}

		}).start();
	}

}

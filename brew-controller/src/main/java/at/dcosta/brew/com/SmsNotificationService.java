package at.dcosta.brew.com;

import static at.dcosta.brew.Configuration.SMS_ACCOUNT;
import static at.dcosta.brew.Configuration.SMS_RECIPIENTS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import at.dcosta.brew.Configuration;
import at.dcosta.brew.ConfigurationException;
import at.dcosta.brew.util.IOUtils;
import at.dcosta.brew.util.ThreadManager;

public class SmsNotificationService implements Notifier {

	static final Logger LOGGER = Logger.getLogger(SmsNotificationService.class.getName());

	private Configuration config;
	private boolean test = true;

	public SmsNotificationService() {
		this.config = Configuration.getInstance();
	}

	@Override
	public long getIgnoreSameSubjectTimeoutMillis() {
		return 1000l * 5;
	}

	@Override
	public void sendNotification(final Notification notification) {

		ThreadManager.getInstance().newThread(new Runnable() {

			@Override
			public void run() {
				BufferedReader reader = null;
				try {
					String[] recipients = config.getStringArray(SMS_RECIPIENTS);
					String message = notification.getNotificationType().toString() + ": " + notification.getSubject()
							+ "\n" + (notification.getMessage());
					for (String recipient : recipients) {
						URL url = new URL(getSmsUrl(recipient, message));
						reader = new BufferedReader(new InputStreamReader(url.openStream()));
						StringBuilder b = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							b.append(line).append('\n');
						}
						String response = b.toString();
						String[] responseParts = response.split("\\s");
						if (responseParts.length != 3 || !"OK".equalsIgnoreCase(responseParts[0])) {
							throw new RuntimeException("Response was " + response);
						}
						if (Double.parseDouble(responseParts[2]) < 1.0) {
							LOGGER.log(Level.WARNING, "SMS-gateway credit is low: " + responseParts[2]);
						}
						LOGGER.info("SMS notification sent: " + response);
					}
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "can not sent SMS-Message: " + e.toString(), e);
				} finally {
					IOUtils.close(reader);
				}
			}

		}, "sendSms").start();
	}

	private void append(String name, String value, StringBuilder b) {
		b.append('&').append(name).append('=').append(value);
	}

	private String getSmsUrl(String recipient, String message) {
		String account = config.getString(SMS_ACCOUNT);
		if (!"budgetsms".equalsIgnoreCase(account)) {
			throw new ConfigurationException("Unknown sms account: " + account);
		}

		StringBuilder url = new StringBuilder("https://api.budgetsms.net/");
		if (test) {
			url.append("test");
		} else {
			url.append("send");
		}
		url.append("sms/?credit=1");
		append("username", "pi.brauerei", url);
		append("userid", "16111", url);
		append("handle", "0250405904dfd1dc9f8ba8380f977530", url);
		append("from", "S2Brauerei", url);
		append("to", recipient, url);
		try {
			append("msg", URLEncoder.encode(message, "utf-8"), url);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url.toString();
	}

}

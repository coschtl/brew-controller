package at.dcosta.brew.msg;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import at.dcosta.brew.Configuration;

public abstract class I18NTexts {

	public static class BundleMessage implements IdBasedMessage {
		private static final long serialVersionUID = 1L;
		private final String id;
		private final String subject;
		private final String message;

		public BundleMessage(String id, String s, Object... variables) {
			this.id = id;
			if (s == null) {
				subject = null;
				message = null;
				return;
			}
			int pos = s.indexOf('|');
			if (pos < 1) {
				subject = null;
			} else {
				subject = s.substring(0, pos).trim();
				s = s.substring(pos + 1).trim();
			}
			if (variables == null || variables.length == 0) {
				message = s;
			} else {
				message = MessageFormat.format(s, variables);
			}
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getMessage() {
			return message;
		}

		public String getSubject() {
			return subject;
		}

	}

	public static final Locale LOCALE;

	static {
		Locale locale = null;
		try {
			String language = Configuration.getInstance().getString(Configuration.MESSAGE_LANGUAGE);
			locale = new Locale(language);
		} catch (Exception e) {
			System.out.println(e);
			locale = new Locale("DE");
		}
		LOCALE = locale;
	}

	public static DateFormat getDateFormat(int format) {
		return DateFormat.getDateInstance(format, LOCALE);
	}

	public static DateFormat getDateTimeFormat(int format) {
		return DateFormat.getDateTimeInstance(format, format, LOCALE);
	}

	public static DateFormat getTimeFormat(int format) {
		return DateFormat.getTimeInstance(format, LOCALE);
	}

	private final ResourceBundle messages;

	protected I18NTexts(String messageFileName) {
		messages = ResourceBundle.getBundle("META-INF/" + messageFileName, LOCALE,
				ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
	}

	protected BundleMessage get(String key, Object... variables) {
		if (key == null) {
			return null;
		}
		String s = null;
		try {
			s = messages.getString(key);
		} catch (MissingResourceException e) {
			throw new MissingResourceException(
					"Can't locate key inside message file of class " + getClass().getSimpleName(), getClass().getName(),
					key);
		}
		return new BundleMessage(new StringBuilder().append(getIdPrefix()).append('.').append(key).toString(), s,
				variables);
	}

	protected abstract char getIdPrefix();

}

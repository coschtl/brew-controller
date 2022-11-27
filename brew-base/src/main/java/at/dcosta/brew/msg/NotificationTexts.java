package at.dcosta.brew.msg;

public class NotificationTexts extends I18NTexts {

	private static final NotificationTexts INSTANCE = new NotificationTexts();

	public static BundleMessage getMessage(String key, Object... variables) {
		return INSTANCE.get(key, variables);
	}

	private NotificationTexts() {
		super("notificationTexts");
	}

	@Override
	protected char getIdPrefix() {
		return 'n';
	}

}

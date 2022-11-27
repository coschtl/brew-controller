package at.dcosta.brew.msg;

public class JournalTexts extends I18NTexts {

	private static final JournalTexts INSTANCE = new JournalTexts();

	public static BundleMessage getMessage(String key, Object... variables) {
		return INSTANCE.get(key, variables);
	}

	private JournalTexts() {
		super("journalTexts");
	}

	@Override
	protected char getIdPrefix() {
		return 'j';
	}

}

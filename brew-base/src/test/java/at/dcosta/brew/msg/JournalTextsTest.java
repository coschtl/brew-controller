package at.dcosta.brew.msg;

import org.junit.Test;

public class JournalTextsTest {

	@Test
	public void test() {
		System.out.println(JournalTexts.getMessage("hello"));
		System.out.println(JournalTexts.getMessage("hello", "hello"));
		System.out.println(JournalTexts.getMessage("enOnly"));
		System.out.println(JournalTexts.getMessage("how", 4));
	}

}

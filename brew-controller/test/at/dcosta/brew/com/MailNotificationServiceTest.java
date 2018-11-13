package at.dcosta.brew.com;

import org.junit.Test;

import at.dcosta.brew.AbstractConfigBasedTest;
import at.dcosta.brew.util.ThreadManager;

public class MailNotificationServiceTest extends AbstractConfigBasedTest {

	@Test
	public void test() throws Exception {
		Notification notification = new Notification(NotificationType.INFO, "restFinished", 20, 64);
		new MailNotificationService().sendNotification(notification);
		ThreadManager.getInstance().waitForAllThreadsToComplete();
	}

}

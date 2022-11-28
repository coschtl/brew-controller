package at.dcosta.brew.com;

import at.dcosta.brew.Configuration;
import at.dcosta.brew.util.ThreadManager;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TelegramNotificationService implements Notifier {

    static final Logger LOGGER = Logger.getLogger(TelegramNotificationService.class.getName());
    private final String token;
    private final String chatId;

    public TelegramNotificationService() {
        Configuration cfg = Configuration.getInstance();
        this.token = cfg.getString("telegram.token");
        this.chatId = cfg.getString("telegram.chatId");
    }

    @Override
    public long getIgnoreSameSubjectTimeoutMillis() {
        return 1000L * 5;
    }

    public static void main(String[] args) {
        new TelegramNotificationService().sendNotification(null);
    }

    @Override
    public void sendNotification(final Notification notification) {

        ThreadManager.getInstance().newThread(new Runnable() {

            @Override
            public void run() {
                String message = notification.getNotificationType().toString()
                        + ": "
                        + notification.getSubject()
                        + "\n" + (notification.getMessage());

                try (CloseableHttpClient client = HttpClientBuilder.create().setConnectionTimeToLive(5, TimeUnit.SECONDS).build()) {
                    URIBuilder builder = new URIBuilder("https://api.telegram.org")
                            .setPath("/bot" + token + "/sendMessage")
                            .addParameter("chat_id", chatId)
                            .addParameter("text", message);

                    HttpGet get = new HttpGet(builder.build());
                    try (CloseableHttpResponse response = client.execute(get)) {
                        if (response.getStatusLine().getStatusCode() != 200) {
                            ByteArrayOutputStream bout = new ByteArrayOutputStream();
                            response.getEntity().writeTo(bout);
                            throw new RuntimeException(bout.toString());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("can not sent Telegram-Message: " + e);
                    LOGGER.log(Level.SEVERE, "can not sent Telegram-Message: " + e, e);
                }
            }
        }, "sendtelegramMessage").start();
    }
}

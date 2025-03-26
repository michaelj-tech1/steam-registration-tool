package org.gh;
import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SubjectTerm;
import java.util.Arrays;
import java.util.Properties;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PhoneEmail {

    private static final Logger LOGGER = Logger.getLogger(PhoneEmail.class.getName());
    private final String email;
    private final String password;

    public PhoneEmail(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public void processSteamVerificationLink() {
        String link = getSteamVerificationLink();
        if (link != null) {
            try {
                String response = sendGetRequest(link);
                LOGGER.info(response);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to send GET request: ", e);
            }
        }
    }

    private String getSteamVerificationLink() {
        String link = null;
        while (link == null) {
            try (Store store = initStore(); Folder inbox = openInbox(store)) {
                Message[] messages = inbox.search(new SubjectTerm("Adding a phone number to your Steam account"));
                sortMessagesByDate(messages);
                link = findLinkInMessages(messages);

                if (link == null) {
                    LOGGER.info("Link not found. Waiting for 5 seconds before retrying...");
                    Thread.sleep(5000);
                } else {
                    LOGGER.info("Link extracted: " + link);
                }
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Interrupted during sleep: ", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error retrieving Steam verification link: ", e);
            }
        }
        return link;
    }

    private Store initStore() throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.imap.host", "outlook.office365.com");
        properties.put("mail.imap.port", "993");
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.starttls.enable", "true");

        Session session = Session.getInstance(properties);
        Store store = session.getStore("imap");
        store.connect(email, password);
        return store;
    }

    private Folder openInbox(Store store) throws MessagingException {
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        return inbox;
    }

    private void sortMessagesByDate(Message[] messages) {
        Arrays.sort(messages, (m1, m2) -> {
            try {
                return m2.getReceivedDate().compareTo(m1.getReceivedDate());
            } catch (MessagingException e) {
                LOGGER.log(Level.WARNING, "Error sorting messages: ", e);
                return 0;
            }
        });
    }

    private String findLinkInMessages(Message[] messages) throws Exception {
        for (Message message : messages) {
            String content = getTextFromMessage(message);
            String link = extractLink(content);
            if (link != null) {
                return link;
            }
        }
        return null;
    }

    private static String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return (String) message.getContent();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            return getTextFromMimeMultipart(mimeMultipart);
        }
        return "";
    }

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain") || bodyPart.isMimeType("text/html")) {
                result.append("\n").append(bodyPart.getContent());
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString().trim();
    }

    private static String extractLink(String text) {
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (word.startsWith("https://store.steampowered.com/phone/ConfirmEmailForAdd")) {
                return word;
            }
        }
        return null;
    }

    private static String sendGetRequest(String link) throws Exception {
        URL url = new URL(link);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            return content.toString();
        } finally {
            con.disconnect();
        }
    }
}

package org.gh;
import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SubjectTerm;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SteamGuardEmailFetcher {

    private static final Logger LOGGER = Logger.getLogger(SteamGuardEmailFetcher.class.getName());
    private final String email;
    private final String password;

    public SteamGuardEmailFetcher(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getSteamGuardLink() {
        String link = null;

        while (link == null) {
            try (Store store = initStore(); Folder inbox = openInbox(store)) {
                Message[] messages = inbox.search(new SubjectTerm("Disable Steam Guard Confirmation"));
                sortMessagesByDate(messages);
                link = findLinkInMessages(messages);

                if (link == null) {
                    LOGGER.info("No Steam Guard link found. Waiting for 3 seconds before retrying...");
                    Thread.sleep(3000);
                }
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Interrupted during sleep: ", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "An error occurred: ", e);
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
        properties.put("mail.imap.ssl.socketFactory.fallback", "false");

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
            String link = extractSteamGuardLink(content);
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

    private static String extractSteamGuardLink(String text) {
        Pattern pattern = Pattern.compile("https://store\\.steampowered\\.com/account/steamguarddisableverification\\?stoken=[^\"'\\s]+");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}

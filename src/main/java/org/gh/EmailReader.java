package org.gh;
import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SubjectTerm;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailReader {

    private static final Logger LOGGER = Logger.getLogger(EmailReader.class.getName());
    private final String email;
    private final String password;

    public EmailReader(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getSteamVerificationLink() {
        String link = null;
        int attempts = 0;

        while (link == null && attempts < 15) {
            attempts++;
            LOGGER.info("Attempt " + attempts + ": Checking for Steam verification email...");
            try (Store store = initStore(); Folder inbox = openInbox(store)) {
                Message[] messages = inbox.search(new SubjectTerm("New Steam Account Email Verification"));
                link = findLinkInMessages(messages);

                if (link == null) {
                    LOGGER.info("No email found. Waiting for 10 sec before next attempt...");
                    Thread.sleep(3000);
                } else {
                    LOGGER.info("Link found: " + link);
                }
            } catch (MessagingException e) {
                LOGGER.log(Level.SEVERE, "MessagingException encountered: ", e);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "InterruptedException encountered: ", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Exception encountered: ", e);
            }
        }
        return link;
    }

    private Store initStore() throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.imap.host", "outlook.office365.com");
        properties.put("mail.imap.port", "993");
        properties.put("mail.imap.ssl.enable", "true");

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
            if (word.startsWith("http") && word.contains("://")) {
                return word;
            }
        }
        return null;
    }
}

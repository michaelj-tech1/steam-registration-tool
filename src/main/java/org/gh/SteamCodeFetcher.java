package org.gh;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SubjectTerm;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SteamCodeFetcher {

    private final String email;
    private final String password;

    public SteamCodeFetcher(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String fetchSteamCode() {
        Store store = null;
        Folder inbox = null;

        try {
            Properties properties = new Properties();
            properties.put("mail.imap.host", "imap-mail.outlook.com");
            properties.put("mail.imap.port", "993");
            properties.put("mail.imap.ssl.enable", "true");
            properties.put("mail.imap.starttls.enable", "true");
            properties.put("mail.imap.ssl.socketFactory.fallback", "true");

            Session session = Session.getInstance(properties);
            store = session.getStore("imap");
            store.connect(email, password);

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.search(new SubjectTerm("Your Steam account: Access from new web or mobile device"));

            for (Message message : messages) {
                String content = getTextFromMessage(message);
                String code = extractVerificationCodeFromEmailContent(content);

                if (code != null) {
                    return code;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inbox != null && inbox.isOpen()) {
                    inbox.close(false);
                }
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return (String) message.getContent();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            return getTextFromMimeMultipart(mimeMultipart);
        }
        return "";
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append("\n").append(bodyPart.getContent());
                break;
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result.append("\n").append(html);
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    private String extractVerificationCodeFromEmailContent(String emailContent) {
        Pattern pattern = Pattern.compile("<td class=\"x_title-48 x_c-blue1 x_fw-b x_a-center\"[^>]*>([^<]*)</td>");
        Matcher matcher = pattern.matcher(emailContent);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}
package org.gh;

import javax.mail.*;
import java.util.Properties;

public class OutlookIMAPLogin {

    public static void main(String[] args) {
        String username = "vir.noemi.67@outlook.com";
        String password = "656teysye656";

        Properties properties = new Properties();
        properties.put("mail.imap.host", "outlook.office365.com");
        properties.put("mail.imap.port", "993");
        properties.put("mail.imap.ssl.enable", "true");


        Session session = Session.getInstance(properties);

        try {
            Store store = session.getStore("imap");
            store.connect(username, password);
            System.out.println("Login successful!");

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();

            int maxMessagesToPrint = Math.min(5, messages.length);

            for (int i = 0; i < maxMessagesToPrint; i++) {
                Message message = messages[i];
                Address[] fromAddresses = message.getFrom();
                System.out.println("Email " + (i+1) + ":");
                System.out.println("Subject: " + message.getSubject());
                System.out.println("From: " + (fromAddresses.length > 0 ? fromAddresses[0] : ""));
                System.out.println();
            }

            inbox.close(false);

            store.close();
        } catch (NoSuchProviderException e) {
            System.err.println("IMAP provider not found.");
            e.printStackTrace();
        } catch (MessagingException e) {
            System.err.println("Could not connect to the server.");
            e.printStackTrace();
        }
    }
}

package org.gh;

import java.util.Properties;
import javax.mail.*;

public class IMAPEmailReader {

    public static void main(String[] args) {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.print("Enter your IMAP email address (e.g., username@example.com): ");
        String email = scanner.nextLine();
        System.out.print("Enter your IMAP password: ");
        String password = scanner.nextLine();
        scanner.close();

        try {
            Properties properties = new Properties();
            properties.setProperty("mail.store.protocol", "imaps");
            properties.setProperty("mail.imap.host", "outlook.office365.com");
            properties.setProperty("mail.imap.port", "993");

            Session session = Session.getDefaultInstance(properties);

            Store store = session.getStore("imaps");
            store.connect("outlook.office365.com", email, password);

            Folder inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            for (Message message : messages) {
                System.out.println("Subject: " + message.getSubject());
                System.out.println("From: " + message.getFrom()[0]);
                System.out.println("Date: " + message.getSentDate());
                System.out.println("Content: " + message.getContent());
                System.out.println("---------------------------------------------");
            }

            inbox.close(false);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package org.gh;

import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailsReader {

    private static final String FILE_PATH = "emails.txt";

    public static Optional<String[]> getFirstEmailAndPassword() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(FILE_PATH));

            if (lines.isEmpty()) {
                return Optional.empty();
            }

            String firstLine = lines.get(0);
            String[] emailAndPassword = extractEmailAndPassword(firstLine);

            if (emailAndPassword.length != 2) {
                System.out.println("Invalid format in the file.");
                return Optional.empty();
            }

            lines.remove(0);
            Files.write(Paths.get(FILE_PATH), lines);

            return Optional.of(emailAndPassword);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static String[] extractEmailAndPassword(String line) {
        Pattern pattern = Pattern.compile("([^|:]+)[|:]([^|:]+)");
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            return new String[]{matcher.group(1).trim(), matcher.group(2).trim()};
        }

        return new String[]{};
    }

}

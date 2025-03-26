package org.gh;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Email {

        public static Map<String, String> emailRequest(String apiKey) {
                String emailPref = MainUi.getKopeechkaEmailPrefText();

                if (emailPref == null || emailPref.trim().isEmpty()) {
                        emailPref = "OUTLOOK";
                }

                while (true) {
                        try {
                                URL url = new URL("https://api.kopeechka.store/mailbox-get-email?site=steampowered.com&mail_type=" + emailPref + "&token=" + apiKey + "&password=1&regex=&subject=&investor=&soft=&type=json&api=2.0");
                                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                                con.setRequestMethod("GET");

                                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                                StringBuilder response = new StringBuilder();
                                String inputLine;
                                while ((inputLine = in.readLine()) != null) {
                                        response.append(inputLine);
                                }
                                in.close();
                                System.out.println(response);

                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode rootNode = objectMapper.readTree(response.toString());

                                if ("OK".equals(rootNode.get("status").asText())) {
                                        String email = rootNode.get("mail").asText();
                                        String id = rootNode.get("id").asText();
                                        String password = rootNode.get("password").asText();

                                        Map<String, String> result = new HashMap<>();
                                        result.put("email", email);
                                        result.put("id", id);
                                        result.put("password", password);
                                        return result;
                                }

                                Thread.sleep(5000);
                        } catch (Exception e) {
                                System.out.println(e.getMessage());
                                try {
                                        Thread.sleep(5000);
                                } catch (InterruptedException ie) {
                                        System.out.println(ie.getMessage());
                                }
                        }
                }
        }




        public static String getSteamLink(String apiKey, String id, String linkRegex) {
                int maxAttempts = 20;
                int currentAttempt = 0;

                while (currentAttempt < maxAttempts) {
                        try {
                                URL url = new URL("https://api.kopeechka.store/mailbox-get-message?id=" + id + "&token=" + apiKey + "&full=1&type=json&api=2.0");
                                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                                con.setRequestMethod("GET");

                                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                                String inputLine;
                                StringBuilder response = new StringBuilder();
                                while ((inputLine = in.readLine()) != null) {
                                        response.append(inputLine);
                                }
                                in.close();

                                System.out.println("The response: " + response);

                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode rootNode = objectMapper.readTree(response.toString());
                                String status = rootNode.get("status").asText();
                                String value = rootNode.path("value").asText();
                                System.out.println("The status is: " + status + ", The value is: " + value);

                                if (status.equals("OK")) {
                                        String fullMessage = rootNode.path("fullmessage").asText();
                                        System.out.println("Got ok response with full message: " + fullMessage);

                                        String decodedMessage = StringEscapeUtils.unescapeHtml4(fullMessage);
                                        return extractLink(decodedMessage, linkRegex);


                                } else if (status.equals("ERROR") && value.equals("WAIT_LINK")) {
                                        System.out.println("Waiting for 5 seconds");
                                        Thread.sleep(5000);
                                } else {
                                        break;
                                }

                        } catch (Exception e) {
                                System.out.println("Error: " + e.getMessage());
                                break;
                        }

                        currentAttempt++;
                }

                return null;
        }

        private static String extractLink(String htmlText, String linkRegex) {
                Pattern pattern = Pattern.compile(linkRegex);
                Matcher matcher = pattern.matcher(htmlText);

                if (matcher.find()) {
                        return matcher.group();
                }
                return null;
        }

        public static boolean cancelMailbox(String apiKey, String id) {
                try {
                        String urlString = String.format("https://api.kopeechka.store/mailbox-cancel?id=%s&token=%s&type=json&api=2.0", id, apiKey);
                        URL url = new URL(urlString);
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("GET");

                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                        }
                        in.close();

                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = objectMapper.readTree(response.toString());
                        String status = rootNode.get("status").asText();

                        return "OK".equals(status);
                } catch (Exception e) {
                        System.out.println("Error in cancelMailbox: " + e.getMessage());
                        return false;
                }
        }


        private static String extractCode(String text, String regex) {
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                        return matcher.group();
                }
                return null;
        }

        public static String reorderMailbox(String email, String apiKey) {
                try {
                        String urlString = "https://api.kopeechka.store/mailbox-reorder?site=steam.com&email="
                                + email + "&token=" + apiKey + "&password=0&subject=&regex=&type=json&api=2.0";

                        URL url = new URL(urlString);
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("GET");

                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                        }
                        in.close();
                        String responseString = response.toString();
                        System.out.println("Response: " + responseString);

                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = objectMapper.readTree(responseString);
                        String status = rootNode.path("status").asText();
                        if ("OK".equals(status)) {
                                return rootNode.path("id").asText();
                        } else {
                                System.out.println("Error: Unexpected response status");
                                return null;
                        }

                } catch (Exception e) {
                        System.out.println("Error in reorderMailbox: " + e.getMessage());
                        return null;
                }
        }
}

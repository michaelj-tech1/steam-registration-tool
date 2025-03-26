package org.gh;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FiveSimRequest {


    private static final String BASE_URL = "https://5sim.net/v1";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String[] getNumberAndCountryCode(String country, String operator, String apiKey) throws Exception {

        String product = "steam";

        URL url = new URL(BASE_URL + "/user/buy/activation/" + country + "/" + operator + "/" + product);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Accept", "application/json");

        int status = connection.getResponseCode();
        BufferedReader reader;
        if (status > 299) {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        }
        StringBuilder responseString = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            responseString.append(line);
        }
        reader.close();


        if (status != 200) {
            throw new RuntimeException("HTTP GET Request Failed with Error code : " + status);
        }

        JsonNode responseJson = objectMapper.readTree(responseString.toString());
        String phoneNumber = responseJson.get("phone").asText();
        String id = responseJson.get("id").asText();

        return new String[] {phoneNumber, id};
    }

    public static String getSMSCode(String id, String apiKey) throws Exception {
        String smsCode = null;
        int attempt = 0;
        int maxAttempts = 40;

        while (smsCode == null && attempt < maxAttempts) {
            URL url = new URL("https://5sim.net/v1/user/check/" + id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);

            int status = connection.getResponseCode();
            if (status != 200) {
                throw new RuntimeException("HTTP GET Request Failed with Error code : " + status);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseString.append(line);
            }
            reader.close();

            JsonNode responseJson = objectMapper.readTree(responseString.toString());
            if (responseJson.get("status").asText().equals("RECEIVED")) {
                JsonNode smsNode = responseJson.get("sms");
                if (smsNode.isArray() && smsNode.size() > 0) {
                    smsCode = smsNode.get(0).get("code").asText();
                } else {
                    Thread.sleep(3000);
                    attempt++;
                }
            } else {
                Thread.sleep(3000);
                attempt++;
            }
        }
        return smsCode;
    }


}

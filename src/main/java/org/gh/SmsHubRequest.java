package org.gh;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SmsHubRequest {

    private static final String BASE_URL = "https://smshub.org/stubs/handler_api.php";


    public static String[] getNumber(String apiKey, String country) throws Exception {

        String operator = "any";

        String requestUrl = BASE_URL + "?api_key=" + apiKey + "&action=getNumber&service=mt"
                + "&operator=" + operator + "&country=" + country;

        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int status = connection.getResponseCode();
        BufferedReader reader;
        if (status > 299) {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        }
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        String responseString = response.toString();


        if (responseString.equalsIgnoreCase("API_KEY_NOT_VALID")) {
            throw new Exception("The provided API key is not valid.");
        }

        if (responseString.equalsIgnoreCase("NO_NUMBERS")) {
            throw new Exception("There are currently no available numbers.");
        }

        String[] result = new String[2];

        String[] splitResponse = responseString.split(":");


        if (splitResponse.length >= 3 && splitResponse[0].equals("ACCESS_NUMBER")) {
            result[0] = splitResponse[1];
            result[1] = splitResponse[2];
        } else {
            throw new Exception("Unexpected response from SmsHub: " + responseString);
        }

        return result;
    }

    public static String getSmsCode(String id, String apiKey) throws Exception {
        String requestUrl = BASE_URL + "?api_key=" + apiKey + "&action=getStatus&id=" + id;

        int attempt = 0;
        int maxAttempts = 40;

        while (attempt < maxAttempts) {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            BufferedReader reader;
            if (status > 299) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }

            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String result = response.toString();
            if (result.startsWith("STATUS_OK:")) {
                System.out.println("Got sms code");
                return result.split(":")[1];
            } else if (result.equals("STATUS_WAIT_CODE")) {
                System.out.println("Waiting on sms code....");
                Thread.sleep(3000);
                attempt++;
            } else {
                throw new Exception("Unexpected response from SmsHub: " + result);
            }
        }
        return null;
    }


}

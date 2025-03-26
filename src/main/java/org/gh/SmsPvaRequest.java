package org.gh;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Triple;
import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class SmsPvaRequest {

    static {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
        }
    }

    public static Triple<String, String, String> getSmsNumber(String apiKey, String country) {
        while (true) {
            HttpURLConnection con = null;
            BufferedReader in = null;
            try {
                URL url = new URL("https://smspva.com/priemnik.php?metod=get_number&country=" + country + "&service=opt58&apikey=" + apiKey);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                int status = con.getResponseCode();
                System.out.println("HTTP response code: " + status);

                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                System.out.println("API response: " + response);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.toString());
                String responseCode = rootNode.get("response").asText();

                if (responseCode.equals("1")) {
                    String number = rootNode.get("number").asText();
                    String id = rootNode.get("id").asText();
                    String countryCode = rootNode.get("CountryCode").asText();

                    System.out.println("Parsed number: " + number);
                    System.out.println("Parsed ID: " + id);
                    System.out.println("Parsed country code: " + countryCode);

                    return Triple.of(number, id, countryCode);
                } else {
                    System.out.println("Bad response (" + responseCode + "). Retrying in 60 seconds...");
                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                System.out.println("Exception occurred: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) in.close();
                } catch (Exception ignored) {
                }
                if (con != null) con.disconnect();
            }
        }
    }
    public static String getSmsMessage(String id, String apiKey, String country) {
        String sms = null;
        int attempt = 0;
        int maxAttempts = 40;

        while (sms == null && attempt < maxAttempts) {
            HttpURLConnection con = null;
            BufferedReader in = null;
            try {
                URL url = new URL("https://smspva.com/priemnik.php?method=get_sms&country=" + country + "&service=opt58&id=" + id + "&apikey=" + apiKey);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println(response);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.toString());
                if (rootNode.get("response").asText().equals("1")) {
                    sms = rootNode.get("sms").asText();
                } else {
                    Thread.sleep(3000);
                    attempt++;
                }
            } catch (Exception e) {
                System.out.println("Exception occurred: " + e.getMessage());
                e.printStackTrace();
                break;
            } finally {
                try {
                    if (in != null) in.close();
                } catch (Exception ignored) {
                }
                if (con != null) con.disconnect();
            }
        }
        return sms;
    }


}

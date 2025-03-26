package org.gh;

import okhttp3.*;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class ProxyRequest{

    public static String sendGetRequestWithProxy(String link, String proxyDetails) {
        try {
            String[] parts = proxyDetails.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            String username = parts[2];
            String password = parts[3];

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            Authenticator proxyAuthenticator = new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) {
                    String credential = Credentials.basic(username, password);
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .proxy(proxy)
                    .proxyAuthenticator(proxyAuthenticator)
                    .build();

            Request request = new Request.Builder()
                    .url(link)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "Request failed with code: " + response.code();
                } else {
                    return response.body().string();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}

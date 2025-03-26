package org.gh;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
public class ProxyHandler {
    private static ProxyHandler instance = null;

    private final List<String[]> proxies;
    private final Random random;


    public ProxyHandler() {
        this.proxies = new ArrayList<>();
        this.random = new Random();
        readProxiesFromFile("proxy.txt");
    }


    public static synchronized ProxyHandler getInstance() {
        if (instance == null) {
            instance = new ProxyHandler();
        }
        return instance;
    }




    public ProxyInfo getNextProxy() {
        if (proxies.isEmpty()) {
            readProxiesFromFile("proxy.txt");
            if (proxies.isEmpty()) {
                return null;
            }
        }

        int index = random.nextInt(proxies.size());
        String[] parts = proxies.get(index);
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);
        String username = parts[2];
        String password = parts[3];
        return new ProxyInfo(host, port, username, password);
    }

    private void readProxiesFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length != 4) {
                    Main.stopAllThreadsAndCloseTabs();
                    System.err.println("Invalid proxy format: " + line);
                    continue;
                }
                proxies.add(parts);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}

package org.gh;

import com.microsoft.playwright.options.Proxy;

public class ProxyInfo {
    private final String host;
    private final int port;
    private final String username;
    private final String password;

    public ProxyInfo(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Proxy getProxy() {
        Proxy proxy = new Proxy(host + ":" + port);
        if (username != null && password != null) {
            proxy.setUsername(username);
            proxy.setPassword(password);
        }
        return proxy;
    }


//    public String getFormattedDetails() {
//        return host + ":" + port + ":" + (username != null ? username : "") + ":" + (password != null ? password : "");
//    }




}
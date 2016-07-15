package com.pacoapp.paco.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ServerAddressBuilder {

  public static String createServerUrl(String serverAddress, String path) {
    if (isLocalDevelopmentServerAddress(serverAddress)) {
      return "http://"+serverAddress+path;
    } else {
      return "https://"+serverAddress+path;
    }
  }

  public static boolean isLocalDevelopmentServerAddress(String serverAddress) {
    return serverAddress.contains("10.0.2.2") || serverAddress.contains("127.0.0.1");
  }

  public static HttpURLConnection getConnection(URL u) throws IOException {
    if (isLocalDevelopmentServerAddress(u.getHost())) {
      return (HttpURLConnection)u.openConnection();
    }
    return (HttpsURLConnection) u.openConnection();
  }

}

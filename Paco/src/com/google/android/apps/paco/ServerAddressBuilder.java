package com.google.android.apps.paco;

public class ServerAddressBuilder {

  public static String createServerUrl(String serverAddress, String path) {
    if (isLocalDevelopmentServerAddress(serverAddress)) {
      return "http://"+serverAddress+path;
    } else {
      return "https://"+serverAddress+path;
    }
  }

  private static boolean isLocalDevelopmentServerAddress(String serverAddress) {
    return serverAddress.contains("10.0.2.2");
  }

}

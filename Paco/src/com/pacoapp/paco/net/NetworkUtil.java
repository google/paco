package com.pacoapp.paco.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {
  
  public static boolean isConnected(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    return networkInfo != null && networkInfo.isConnected();
  }

  public static final String EXECUTION_ERROR = "execution_error";
  public static final String SERVER_COMMUNICATION_ERROR = "server_communication_error";
  public static final String CONTENT_ERROR = "content_error";
  public static final String RETRIEVAL_ERROR = "retrieval_error";
  public static final String SUCCESS = "success";
  public static final int INVALID_DATA_ERROR = 1003;
  public static final int SERVER_ERROR = 1004;
  public static final int NO_NETWORK_CONNECTION = 1005;
  public static final int ENABLED_NETWORK = 1;

}

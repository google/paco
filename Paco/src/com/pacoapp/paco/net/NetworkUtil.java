package com.pacoapp.paco.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.core.net.ConnectivityManagerCompat;

import com.pacoapp.paco.UserPreferences;

public class NetworkUtil {

  public static boolean isConnected(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    boolean basicNetworkOn = networkInfo != null && networkInfo.isConnected();
    if (!basicNetworkOn) {
      return false;
    }
//    if (new UserPreferences(context.getApplicationContext()).getWifiOnly()) {
//      return !ConnectivityManagerCompat.isActiveNetworkMetered(cm);
//    }
    return true;
  }

  public static final String EXECUTION_ERROR = "execution_error";
  public static final String SERVER_COMMUNICATION_ERROR = "server_communication_error";
  public static final String CONTENT_ERROR = "content_error";
  public static final String RETRIEVAL_ERROR = "retrieval_error";
  public static final String SUCCESS = "success";
  public static final int INVALID_DATA_ERROR = 1003;
  public static final int SERVER_ERROR = 1004;
  public static final int NO_NETWORK_CONNECTION = 1005;
  public static final int JOIN_ERROR = 1006;
  public static final int ENABLED_NETWORK = 1;
  public static final int UNKNOWN_HOST_ERROR = 1007;

}

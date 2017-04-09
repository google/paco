package com.pacoapp.paco.sensors.android.diagnostics;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pacoapp.paco.R;
import com.pacoapp.paco.UserPreferences;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class PacoServerReachabilityDiagnostic extends ListDiagnostic {

  public PacoServerReachabilityDiagnostic(Context context) {
    super(context.getString(R.string.diagnostic_paco_reach_type));
  }

  @Override
  public void run(final Context context) {
    new Thread(new Runnable() {

      @Override
      public void run() {
        List<String> results = Lists.newArrayList();
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
         
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                              activeNetwork.isConnectedOrConnecting();
        
        if (isConnected) {
          UserPreferences userPrefs = new UserPreferences(context);
          String host = userPrefs.getServerAddress();
          results.add(context.getString(R.string.diagnostic_server_label) + ": " + host);
          if (!Strings.isNullOrEmpty(host)) {
            boolean isReachable;
            try {
              isReachable = InetAddress.getByName(host).isReachable(5);
              results.add(context.getString(R.string.diagnostic_reachable_label) + ": " + isReachable);
            } catch (UnknownHostException e) {
              results.add(context.getString(R.string.diagnostic_reachable_label) + ": unknown host error");
            } catch (IOException e) {
              results.add(context.getString(R.string.diagnostic_reachable_label) + ": unknown error: " + e.getMessage());
              e.printStackTrace();
            }
  
          }
        } else {
          results.add(context.getString(R.string.diagnostic_reachable_label) + ": " + context.getString(R.string.diagnostic_no_network_label));
        }
        setValue(results);
      }
    }).start();
  }
  
}

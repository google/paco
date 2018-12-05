package com.pacoapp.paco.sensors.android.diagnostics;

import java.util.List;
import com.google.common.collect.Lists;
import com.pacoapp.paco.R;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkDiagnostic extends ListDiagnostic {

  public NetworkDiagnostic(Context context) {
    super(context.getString(R.string.diagnostic_network_type));
  }

  @Override
  public void run(Context context) {
    ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
     
    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    boolean isConnected = activeNetwork != null &&
                          activeNetwork.isConnectedOrConnecting();
                          
    List<String> results = Lists.newArrayList();
    setValue(results);
    
    results.add(context.getString(R.string.diagnostics_network_connected_label) + ": " + isConnected);
    
    if (!isConnected) {
      return;
    }
    int type = activeNetwork.getType();
    switch (type) {
    case ConnectivityManager.TYPE_WIFI:
        results.add(context.getString(R.string.diagnostics_network_type_label) + ": wifi");  
        break;
    case ConnectivityManager.TYPE_MOBILE:
      results.add(context.getString(R.string.diagnostics_network_type_label) + ": mobile");  
      break;
    case ConnectivityManager.TYPE_WIMAX:
      results.add(context.getString(R.string.diagnostics_network_type_label) + ": wimax");  
      break;
    case ConnectivityManager.TYPE_BLUETOOTH:
      results.add(context.getString(R.string.diagnostics_network_type_label) + ": bluetooth");  
      break;
    case ConnectivityManager.TYPE_ETHERNET:
      results.add(context.getString(R.string.diagnostics_network_type_label) + ": ethernet");  
      break;
    default:
      results.add(context.getString(R.string.diagnostics_network_type_label) + ": unknown");  
      break;
    }
    

  }

}

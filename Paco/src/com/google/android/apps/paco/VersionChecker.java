package com.google.android.apps.paco;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.google.corp.productivity.specialprojects.android.comm.Response;
import com.google.corp.productivity.specialprojects.android.comm.Response.Status;
import com.google.corp.productivity.specialprojects.android.comm.UrlContentManager;
import com.pacoapp.paco.R;

public class VersionChecker {

  static Integer getVersionCode(Context context) {
    PackageInfo pInfo = null;
    try {
      pInfo =
          context.getPackageManager().getPackageInfo((String) context.getText(R.string.app_package),
              PackageManager.GET_META_DATA);
      return pInfo.versionCode;
    } catch (NameNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static boolean checkForUpdate(Context context) {
    UrlContentManager um = null;
    try {
      UserPreferences userPrefs = new UserPreferences(context);  
      um = new UrlContentManager(context);

      Log.i(PacoConstants.TAG, "Checking version of client");
      
      Response response = um.createRequest()
      .setUrl(ServerAddressBuilder.createServerUrl(new UserPreferences(context.getApplicationContext()).getServerAddress(),"/version"))
      .addHeader("http.useragent", "Android")      
      .addHeader("paco.version", AndroidUtils.getAppVersion(context))
      .execute();
      
      
      if (response.getStatus() == Status.HTTP_OK) {
        BufferedReader reader = null;
        try {
          InputStream instream = response.getContentAsStream();
          reader = new BufferedReader(new InputStreamReader(instream));
          String line = reader.readLine().trim();
          if (line == null || line.length() == 0) {
            return false;
          }
          Integer serverVersion = Integer.parseInt(line);
          Integer thisVersion = VersionChecker.getVersionCode(context);
          return thisVersion != null && serverVersion > thisVersion;
        } catch (IOException e) {
          Log.e(PacoConstants.TAG, "Could not retrieve version # successfully", e);
        } finally {
          if (reader != null) {
            try {
              reader.close();
            } catch (IOException e) {
              Log.e(PacoConstants.TAG, "Could not close reader!?", e);
            }
          }
        }
      } else {
        Log.i(PacoConstants.TAG, "Could not retrieve version # successfully");
      }
    } finally {
      if (um != null) {
        um.cleanUp(); 
      }
    }
    return false;
  }

}

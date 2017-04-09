package com.pacoapp.paco.js.bridge;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.pacoapp.paco.sensors.android.AndroidInstalledApplications;
import com.pacoapp.paco.shared.model2.JsonConverter;

public class JavascriptPackageManager {

  private Context context;

  public JavascriptPackageManager(Context context) {
    this.context = context;
  }

  /**
   * get a list of all the short names of the installed applications
   * on the phone.
   */
  @JavascriptInterface
  public String getNamesOfInstalledApplications() {
    final List<String> namesOfInstalledApplications = new AndroidInstalledApplications(context).getNamesOfInstalledApplications();
    return JsonConverter.convertToJsonString(namesOfInstalledApplications);
  }

  /**
   * Resolves an Android package name to the name of the app, if it is visible to the user.
   * @param packageName The package name of the application
   * @return The application name, or an empty string if the package was not found
   */
  @JavascriptInterface
  public String getApplicationName(String packageName) {
    return new AndroidInstalledApplications(context).getApplicationName(packageName);
  }

  /**
   * Get a list of all applications, and the permissions that were granted to them. For packages
   * targeting SDK version 21 or lower, this means "permissions requested at install time"; for
   * packages targeting SDK 22 or newer, this means "permissions granted during runtime".
   * @return A JSON string of the format {[packageName1: [permission1, permission2]]}
   */
  @JavascriptInterface
  public String getGrantedPermissions() {
    final Map<String, List<String>> grantedPermissions = new AndroidInstalledApplications(context).getGrantedPermissions();
    return JsonConverter.convertToJsonString(grantedPermissions);
  }
}

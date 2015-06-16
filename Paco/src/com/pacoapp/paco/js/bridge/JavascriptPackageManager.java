package com.pacoapp.paco.js.bridge;

import java.io.IOException;
import java.util.List;

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
    ObjectMapper mapper = JsonConverter.getObjectMapper();
    String json = null;
    try {
      json = mapper.writeValueAsString(namesOfInstalledApplications);
    } catch (JsonGenerationException e) {

      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return json;
  }

}

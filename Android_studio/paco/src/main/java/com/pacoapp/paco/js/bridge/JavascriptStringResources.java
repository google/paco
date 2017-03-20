package com.pacoapp.paco.js.bridge;

import android.R;
import android.content.Context;
import android.webkit.JavascriptInterface;

import com.google.common.base.Strings;
import com.pacoapp.paco.ui.ExperimentExecutorCustomRendering;


/**
 * This class allows javascript code to lookup string bundles from the
 * Android bundle localization system. 
 * 
 *
 */
public class JavascriptStringResources {

  private final Context context;

  /**
   * @param Context Android context
   */
  public JavascriptStringResources(Context context) {
    this.context = context;
  }

  @JavascriptInterface
  public String getString(String stringName) {
    if (Strings.isNullOrEmpty(stringName)) {
      return "";
    }
    int idForStringResourceName = getIdForStringResourceName(stringName);
    if (idForStringResourceName == 0) {
      return "";
    }
    return context.getString(idForStringResourceName);
  }
  
  @JavascriptInterface
  public String getString(String stringName, Object formatArgs) {
    return context.getString(getIdForStringResourceName(stringName), formatArgs);
  }
  
  private int getIdForStringResourceName(String stringName) {
    return context.getResources().getIdentifier(stringName, "string", context.getPackageName());
  }
}
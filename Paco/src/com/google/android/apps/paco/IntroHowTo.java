/*
* Copyright 2011 Google Inc. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.  
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.google.android.apps.paco;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.webkit.WebView;

/**
 */
class IntroHowTo {
  private static final String PREFERENCE_HOW_TO_INTRO_SHOWN = "howto.shown";
  private static final String PREFERENCES_INTRO = "eula";

  
  public static boolean alreadyShown(Activity activity) {
    final SharedPreferences preferences = activity.getSharedPreferences(PREFERENCES_INTRO, Activity.MODE_PRIVATE);
    return preferences.getBoolean(PREFERENCE_HOW_TO_INTRO_SHOWN, false);
  }
  /**
   * Displays the EULA if necessary. This method should be called from the
   * onCreate() method of your main Activity.
   *
   * @param activity The Activity to finish if the user rejects the EULA
   */
  static void showHowto(final Activity activity) {
    if (alreadyShown(activity)) {
      return;
    }

    final SharedPreferences preferences = activity.getSharedPreferences(PREFERENCES_INTRO, Activity.MODE_PRIVATE);
    
    WebView wv = new WebView (activity);
    wv.loadUrl("file:///android_asset/intro_howto.html");
    wv.setBackgroundColor(Color.WHITE);
    wv.getSettings().setDefaultTextEncodingName("utf-8");
    
    final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
      .setTitle(R.string.intro_howto_title)
      .setCancelable(true)
      .setView(wv)
      .setPositiveButton(R.string.ok,
        new DialogInterface.OnClickListener() {          
          public void onClick(DialogInterface dialog, int which) {
            ok(activity, preferences);
          }
        });        
    builder.show();
  }

  private static void ok(Activity activity, SharedPreferences preferences) {
    preferences.edit().putBoolean(PREFERENCE_HOW_TO_INTRO_SHOWN, true).commit();
    activity.finish();
  }

  private IntroHowTo() {
  }
}

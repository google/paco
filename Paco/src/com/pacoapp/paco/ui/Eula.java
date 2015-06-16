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
package com.pacoapp.paco.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.Spanned;

import com.pacoapp.paco.PacoConstants;
import com.pacoapp.paco.R;
import com.pacoapp.paco.utils.EulaLocaleHelper;
import com.pacoapp.paco.utils.ResourceUtils;

/**
 * Displays an EULA ("End User License Agreement") that the user has to accept
 * before using the application. Your application should call
 * {@link Eula#showEula(android.app.Activity)} in the onCreate() method of the
 * first activity. If the user accepts the EULA, it will never be shown again.
 * If the user refuses, {@link android.app.Activity#finish()} is invoked on your
 * activity.
 *
 * @author MyTracks Team
 */
public class Eula {
  private static final String PREFERENCE_EULA_ACCEPTED = "eula.accepted";
  private static final String PREFERENCES_EULA = "eula";


  /**
   * Displays the EULA if necessary. This method should be called from the
   * onCreate() method of your main Activity.
   *
   * @param activity The Activity to finish if the user rejects the EULA
   */
  public static void showEula(final Activity activity) {
    final SharedPreferences preferences = activity.getSharedPreferences(PREFERENCES_EULA, Activity.MODE_PRIVATE);
    if (preferences.getBoolean(PREFERENCE_EULA_ACCEPTED, false)) {
      return;
    }

    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setTitle(R.string.eula_title);
    builder.setCancelable(true);
    builder.setPositiveButton(R.string.accept,
        new DialogInterface.OnClickListener() {

          public void onClick(DialogInterface dialog, int which) {
            accept(activity, preferences);
          }
        });
    builder.setNegativeButton(R.string.decline,
        new DialogInterface.OnClickListener() {

          public void onClick(DialogInterface dialog, int which) {
            refuse(activity);
          }
        });
    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

      public void onCancel(DialogInterface dialog) {
        refuse(activity);
      }
    });

    Integer eulaId = new EulaLocaleHelper().getLocalizedResource();
    Spanned tv = Html.fromHtml(ResourceUtils.readFile(activity, eulaId).toString());
    builder.setMessage(tv);
    builder.show();
  }


  private static void accept(Activity activity, SharedPreferences preferences) {
    preferences.edit().putBoolean(PREFERENCE_EULA_ACCEPTED, true).commit();
    Intent startIntent = new Intent(activity, WelcomeActivity.class);
    activity.startActivityForResult(startIntent, PacoConstants.WELCOME);
  }

  private static void refuse(Activity activity) {
    activity.finish();
  }

  private Eula() {
  }
}

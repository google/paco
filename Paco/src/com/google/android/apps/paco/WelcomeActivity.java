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


import java.util.Locale;

import com.pacoapp.paco.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.TextView;

/**
 * An activity that displays a welcome screen.
 *
 * @author Sandor Dornbush, Bob Evans
 */
public class WelcomeActivity extends Activity {

  static final String WELCOME_PAGE = "welcome_page";

  static class WelcomePageLocaleHelper extends AndroidLocaleHelper<String> {
    
    @Override
    protected String getEnVersion() {
      return "file:///android_asset/welcome_paco.html";
    }

    @Override
    protected String getJaVersion() {
      return "file:///android_asset/welcome_paco_ja.html";
    }

    @Override
    protected String getFiVersion() {
      return "file:///android_asset/welcome_paco_fi.html";
    }

  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    requestWindowFeature(Window.FEATURE_PROGRESS);

    setContentView(R.layout.welcome);

    WebView web = (WebView) findViewById(R.id.welcome_web);
    web.loadUrl(new WelcomePageLocaleHelper().getLocalizedResource());

    findViewById(R.id.welcome_read_later).setOnClickListener(
        new OnClickListener() {
          public void onClick(View v) {
            finish();
          }
        });

    findViewById(R.id.welcome_about).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        about();
      }
    });
  }

  /**
   * Shows the "about" dialog.
   * 
   * TODO: Add a menu option for showing the same thing.
   */
  public void about() {
    LayoutInflater li = LayoutInflater.from(this);
    View view = li.inflate(R.layout.about, null);
    TextView versionField = (TextView)view.findViewById(R.id.versionField);
    versionField.setText(getVersion());
    
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setView(view);
    builder.setPositiveButton(R.string.ok, null);
    builder.setIcon(R.drawable.arrow_icon);
    AlertDialog dialog = builder.create();
    dialog.show();
  }

  private String getVersion() {
    PackageInfo pInfo = null;
    try {
      pInfo = getPackageManager().getPackageInfo((String) getText(R.string.app_package), PackageManager.GET_META_DATA);
      return pInfo.versionName;
    } catch (NameNotFoundException e) {
      e.printStackTrace();
    }
    return "unknown";    
  }
}
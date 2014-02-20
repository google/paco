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


import com.pacoapp.paco.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * A viewer for help files.
 *
 * @author Bob Evans
 */
public class HelpActivity extends Activity {

  private WebView webView;

  class HelpActivityLocaleHelper extends AndroidLocaleHelper<String> {

    @Override
    protected String getEnVersion() {
      return "file:///android_asset/help.html";
    }
    
    @Override
    protected String getJaVersion() {
      return "file:///android_asset/help_ja.html";
    }
    
    @Override
    protected String getFiVersion() {
      return "file:///android_asset/help_fi.html";
    }

    @Override
    protected String getPtVersion() {
      return "file:///android_asset/help_pt.html";
    }

    
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    requestWindowFeature(Window.FEATURE_PROGRESS);

    setContentView(R.layout.help);

    webView = (WebView) findViewById(R.id.help_main);
    webView.setWebViewClient(new HelpWebViewClient());
    webView.loadUrl(new HelpActivityLocaleHelper().getLocalizedResource());
  }
  
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
      if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
          webView.goBack();
          return true;
      }
      return super.onKeyDown(keyCode, event);
  }
  
  private class HelpWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      return false;
    }
  }
}

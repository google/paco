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


import java.io.InputStream;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.pacoapp.paco.R;

/**
 * A viewer for help files.
 *
 * @author Bob Evans
 */
public class EulaDisplayActivity extends Activity {

  private WebView webView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    requestWindowFeature(Window.FEATURE_PROGRESS);

    setContentView(R.layout.eula);

    webView = (WebView) findViewById(R.id.eula_main);
    webView.setWebViewClient(new EulaWebViewClient());
    String eulaText = "";
    try {
      Resources res = getResources();
      InputStream in_s = res.openRawResource(new EulaLocaleHelper().getLocalizedResource());

      byte[] b = new byte[in_s.available()];
      in_s.read(b);
      eulaText = new String(b);
  } catch (Exception e) {
  }
    webView.loadData(eulaText, "text/html", "UTF-8");
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
      if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
          webView.goBack();
          return true;
      }
      return super.onKeyDown(keyCode, event);
  }

  private class EulaWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      return false;
    }
  }
}

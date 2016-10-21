package com.pacoapp.paco;

import org.joda.time.DateTime;

import android.app.Application;
import android.content.res.Configuration;
import im.delight.android.languages.Language;

public class PacoApplication extends Application {

  public static final String CUSTOM_LANGUAGE_KEY = "customLanguageKey";

  @Override
  public void onCreate() {
      super.onCreate();
      DateTime.now(); // load this early to try to circumvent joda bug
      Language.setFromPreference(this, CUSTOM_LANGUAGE_KEY);
      android.content.Context context;
//      GoogleApiClient client = new GoogleApiClient.Builder(context)
//              .addApi(Awareness.API)
//              .build();
//      client.connect();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);

      Language.setFromPreference(this, CUSTOM_LANGUAGE_KEY);
  }
}

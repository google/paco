package com.pacoapp.paco;

import im.delight.android.languages.Language;
import android.app.Application;
import android.content.res.Configuration;

public class PacoApplication extends Application {

  public static final String CUSTOM_LANGUAGE_KEY = "customLanguageKey";

  @Override
  public void onCreate() {
      super.onCreate();

      Language.setFromPreference(this, CUSTOM_LANGUAGE_KEY);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);

      Language.setFromPreference(this, CUSTOM_LANGUAGE_KEY);
  }
}

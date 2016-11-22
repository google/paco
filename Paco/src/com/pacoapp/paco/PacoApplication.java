package com.pacoapp.paco;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Application;
import android.app.PendingIntent;
import android.content.res.Configuration;
import im.delight.android.languages.Language;

public class PacoApplication extends Application {

  private static Logger Log = LoggerFactory.getLogger(PacoApplication.class);

  public static final String CUSTOM_LANGUAGE_KEY = "customLanguageKey";

  protected PendingIntent pendingIntent;

  @Override
  public void onCreate() {
    super.onCreate();
    DateTime.now(); // load this early to try to circumvent joda bug
    Language.setFromPreference(this, CUSTOM_LANGUAGE_KEY);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    Language.setFromPreference(this, CUSTOM_LANGUAGE_KEY);
  }

}

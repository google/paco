package com.pacoapp.paco.ui;

import im.delight.android.languages.Language;
import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.pacoapp.paco.PacoApplication;
import com.pacoapp.paco.R;


public class PreferencesPacoFragment extends PreferenceFragment {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
  }
  
  @Override
  public void onPause() {
    Activity activity = getActivity();
    if (activity != null) {
      Language.setFromPreference(activity, PacoApplication.CUSTOM_LANGUAGE_KEY, true);
    }
      super.onPause();
  }
}



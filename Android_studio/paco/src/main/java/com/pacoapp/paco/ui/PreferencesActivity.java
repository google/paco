package com.pacoapp.paco.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class PreferencesActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //LanguageList.setStandardOptionLabel(getString(R.string.default_locale));
    
    PreferencesPacoFragment prefFragment = new PreferencesPacoFragment();
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.replace(android.R.id.content, prefFragment);
    fragmentTransaction.commit();
  }
  
 
}
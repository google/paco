package com.pacoapp.paco.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import com.pacoapp.paco.R;

public abstract class SingleFragmentActivity extends ActionBarActivity {
  protected abstract Fragment createFragment();

  protected int getLayoutResId() {
      return R.layout.activity_fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(getLayoutResId());
      FragmentManager manager = getSupportFragmentManager();
      Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

      if (fragment == null) {
          fragment = createFragment();
          manager.beginTransaction()
              .add(R.id.fragmentContainer, fragment)
              .commit();
      }
  }
}

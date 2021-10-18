package com.pacoapp.paco.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import androidx.appcompat.app.AppCompatActivity;
import com.pacoapp.paco.R;

public abstract class SingleFragmentActivity extends AppCompatActivity {
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

package com.google.android.apps.paco;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.pacoapp.paco.R;

public class FindMyOrAllExperimentsChooserActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_find_my_or_all_experiments_chooser);
  }


  public void myExperimentsButtonClicked(View v) {
    Intent intent = new Intent(this, FindMyExperimentsActivity.class);
    startActivityForResult(intent, FindExperimentsActivity.JOIN_REQUEST_CODE);
  }

  public void allExperimentsButtonClicked(View v) {
    Intent intent = new Intent(this, FindExperimentsActivity.class);
    startActivityForResult(intent, FindExperimentsActivity.JOIN_REQUEST_CODE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == FindExperimentsActivity.JOIN_REQUEST_CODE) {
      if (resultCode == FindExperimentsActivity.JOINED_EXPERIMENT) {
        setResult(resultCode);
        finish();
      }
    }
  }
}

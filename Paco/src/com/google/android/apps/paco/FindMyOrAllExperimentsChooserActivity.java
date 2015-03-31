package com.google.android.apps.paco;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;

import com.pacoapp.paco.R;

public class FindMyOrAllExperimentsChooserActivity extends ActionBarActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_find_my_or_all_experiments_chooser);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setLogo(R.drawable.ic_launcher);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);
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
  public boolean onOptionsItemSelected(MenuItem item) {
      int id = item.getItemId();
      if (id == android.R.id.home) {
        finish();
        return true;
      }
      return super.onOptionsItemSelected(item);
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

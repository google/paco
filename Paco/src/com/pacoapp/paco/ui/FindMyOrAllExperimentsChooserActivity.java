package com.pacoapp.paco.ui;

import androidx.appcompat.app.AppCompatActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pacoapp.paco.R;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import android.view.MenuItem;
import android.view.View;

public class FindMyOrAllExperimentsChooserActivity extends AppCompatActivity {

  private static Logger Log = LoggerFactory.getLogger(FindMyOrAllExperimentsChooserActivity.class);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_find_my_or_all_experiments_chooser);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setLogo(R.drawable.ic_launcher);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));
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

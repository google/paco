package com.pacoapp.paco.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.pacoapp.paco.R;
import com.pacoapp.paco.UserPreferences;

public class SettingsActivity extends ActionBarActivity {

  private static final int RINGTONE_REQUESTCODE = 945;

  private TextView ringtoneTextView;
  private CheckBox wifiOnlyCheckBox;
  private UserPreferences userPrefs;
  private TextView accountTextView;
  private TextView serverAddressTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    userPrefs = new UserPreferences(getApplicationContext());

    ActionBar actionBar = getSupportActionBar();
    actionBar.setLogo(R.drawable.ic_launcher);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));
    actionBar.setDisplayHomeAsUpEnabled(true);

    ringtoneTextView = (TextView) findViewById(R.id.ringToneTextView);
    ringtoneTextView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        launchRingtoneChooser();
      }
    });

    wifiOnlyCheckBox = (CheckBox)findViewById(R.id.wifiCheckBox);
    wifiOnlyCheckBox.setChecked(userPrefs.getWifiOnly());
    wifiOnlyCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        userPrefs.setWifiOnly(isChecked);
      }
    });

    accountTextView = (TextView)findViewById(R.id.accountTextView);
    accountTextView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        launchAccountChooser();
      }
    });

    serverAddressTextView = (TextView)findViewById(R.id.serverAddressView);
    serverAddressTextView.setText(userPrefs.getServerAddress());
    serverAddressTextView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        userPrefs.setServerAddress(serverAddressTextView.getText().toString());
      }
    });

    Button sendLogButton = (Button)findViewById(R.id.sendPacoLogButton);
    sendLogButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        launchLogSender();
      }
    });

    Button debugEmsButton = (Button)findViewById(R.id.debugButton);
    debugEmsButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        launchDebug();
      }
    });
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

  protected void launchDebug() {
    Intent startIntent = new Intent(this, ESMSignalViewer.class);
    startActivity(startIntent);
  }

  private void launchLogSender() {
    String log = readLog();
    createEmailIntent(log);
  }


  private void createEmailIntent(String log) {
    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Paco Feedback");
    emailIntent.setType("plain/text");
    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, log);
    startActivity(emailIntent);
  }

  private String readLog() {
    StringBuilder log = new StringBuilder();
    try {
      Process process = Runtime.getRuntime().exec("logcat -d");
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = bufferedReader.readLine()) != null) {
        log.append(line).append("\n");
      }
    } catch (IOException e) {
      return null;
    }
    return log.toString();
  }

  private void launchRingtoneChooser() {
    UserPreferences userPreferences = new UserPreferences(this);
    String uri = userPreferences.getRingtoneUri();
    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, R.string.select_signal_tone);
    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
    if (uri != null) {
      intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(uri));
    } else {
      intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                      RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
    }

    startActivityForResult(intent, RINGTONE_REQUESTCODE);
  }

  @SuppressLint("NewApi")
  private void launchAccountChooser() {
    Intent intent = new Intent(this, SplashActivity.class);
    intent.putExtra(SplashActivity.EXTRA_CHANGING_EXISTING_ACCOUNT, true);
    startActivity(intent);
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//      Intent intent = AccountManager.newChooseAccountIntent(null, null, new String[] { "com.google" }, false, null,
//                                                            AbstractAuthTokenTask.AUTH_TOKEN_TYPE_USERINFO_EMAIL, null,
//                                                            null);
//      startActivityForResult(intent, SplashActivity.REQUEST_CODE_PICK_ACCOUNT);
//    } else {
//      Intent intent = new Intent(this, AccountChooser.class);
//      startActivityForResult(intent, SplashActivity.REQUEST_CODE_PICK_ACCOUNT);
//    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RINGTONE_REQUESTCODE && resultCode == RESULT_OK) {
      Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
      final UserPreferences userPreferences = new UserPreferences(this);
      if (uri != null) {
        userPreferences.setRingtoneUri(uri.toString());
        String name= data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_TITLE);
        userPreferences.setRingtoneName(name);
      } else {
        userPreferences.clearRingtone();
      }
    }
  }



  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
    ringtoneTextView.setText(userPrefs.getRingtoneName());
    accountTextView.setText(userPrefs.getSelectedAccount());
  }


}

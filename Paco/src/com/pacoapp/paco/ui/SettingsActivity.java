package com.pacoapp.paco.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

import com.pacoapp.paco.R;
import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.os.RingtoneUtil;

public class SettingsActivity extends AppCompatActivity {

  private TextView ringtoneTextView;
  private CheckBox wifiOnlyCheckBox;
  private UserPreferences userPrefs;
  private TextView accountTextView;
  private TextView serverAddressTextView;
  protected AlertDialog emailChangeAlertDialog;

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
        RingtoneUtil.launchRingtoneChooserFor(SettingsActivity.this);
      }
    });
//
//    wifiOnlyCheckBox = (CheckBox)findViewById(R.id.wifiCheckBox);
//    wifiOnlyCheckBox.setChecked(userPrefs.getWifiOnly());
//    wifiOnlyCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//      @Override
//      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        userPrefs.setWifiOnly(isChecked);
//      }
//    });

    accountTextView = (TextView)findViewById(R.id.accountTextView);
    accountTextView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
        alertDialogBuilder.setMessage(R.string.dialog_change_email_warning);
        alertDialogBuilder.setCancelable(true);

        alertDialogBuilder.setPositiveButton(
            R.string.change_email_address_button,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                  dialog.cancel();
                  launchAccountChooser();
                }
            });

        alertDialogBuilder.setNegativeButton(
            R.string.cancel_button,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

        emailChangeAlertDialog = alertDialogBuilder.create();
        emailChangeAlertDialog.show();

      }
    });

    serverAddressTextView = (TextView)findViewById(R.id.serverAddressView);
    serverAddressTextView.setText(userPrefs.getServerAddress());
//    serverAddressTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
//
//      @Override
//      public void onFocusChange(View v, boolean hasFocus) {
//        if (hasFocus) {
//          userPrefs.setServerAddress(serverAddressTextView.getText().toString());
//        }
//      }
//    });



//    Button sendLogButton = (Button)findViewById(R.id.sendPacoLogButton);
//    sendLogButton.setOnClickListener(new OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        launchLogSender();
//      }
//    });
//
//    Button debugEmsButton = (Button)findViewById(R.id.debugButton);
//    debugEmsButton.setOnClickListener(new OnClickListener() {
//
//      @Override
//      public void onClick(View v) {
//        launchDebug();
//      }
//    });
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

//  protected void launchDebug() {
//    Intent startIntent = new Intent(this, ESMSignalViewer.class);
//    startActivity(startIntent);
//  }

//  private void launchLogSender() {
//    String log = readLog();
//    createEmailIntent(log);
//  }
//
//
//  private void createEmailIntent(String log) {
//    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
//    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.email_subject_paco_feedback));
//    emailIntent.setType("plain/text");
//    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, log);
//    startActivity(emailIntent);
//  }

//  private String readLog() {
//    StringBuilder log = new StringBuilder();
//    try {
//      Process process = Runtime.getRuntime().exec("logcat -d");
//      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//
//      String line;
//      while ((line = bufferedReader.readLine()) != null) {
//        log.append(line).append("\n");
//      }
//    } catch (IOException e) {
//      return null;
//    }
//    return log.toString();
//  }

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
    if (RingtoneUtil.isOkRingtoneResult(requestCode, resultCode)) {
      RingtoneUtil.updateRingtone(data, this);
    }
  }


  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
    ringtoneTextView.setText(userPrefs.getRingtoneName());
    accountTextView.setText(userPrefs.getSelectedAccount());
  }


  @Override
  protected void onPause() {
    super.onPause();
    if (!serverAddressTextView.getText().equals(userPrefs.getServerAddress())) {
      userPrefs.setServerAddress(serverAddressTextView.getText().toString());
    }
    if (emailChangeAlertDialog != null) {
      emailChangeAlertDialog.dismiss();
      emailChangeAlertDialog = null;
    }
  }


}

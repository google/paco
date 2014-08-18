/*
* Copyright 2011 Google Inc. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.google.android.apps.paco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.pacoapp.paco.R;

/**
 *
 */
public class ExperimentManagerActivity extends Activity {

  private static final String RINGTONE_TITLE_COLUMN_NAME = "title";
  private static final String PACO_BARK_RINGTONE_TITLE = "Paco Bark";
  private static final String BARK_RINGTONE_FILENAME = "deepbark_trial.mp3";
  private static final int RINGTONE_REQUESTCODE = 945;
  private static final int ABOUT_PACO_ITEM = 3;
  private static final int DEBUG_ITEM = 4;
  private static final int SERVER_ADDRESS_ITEM = 5;
  private static final int UPDATE_ITEM = 6;
  private static final int ACCOUNT_CHOOSER_ITEM = 7;
  private static final int RINGTONE_CHOOSER_ITEM = 8;
  private static final int SEND_LOG_ITEM = 9;
  private static final int EULA_ITEM = 10;

  static final int CHECK_UPDATE_REQUEST_CODE = 0;



  private ImageButton currentExperimentsButton;
  private ExperimentProviderUtil experimentProviderUtil;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    installPacoBarkRingtone();

 // This will show the eula until the user accepts or quits the app.
    experimentProviderUtil = new ExperimentProviderUtil(this);
    Experiment experiment = getExperimentFromIntent();
    if (experiment != null) {
      //startService(new Intent(this, ServerCommunicationService.class)); // check for updated experiments. Commented for now as it may lead to a db race condition.
      Intent executorIntent = new Intent(this, ExperimentExecutor.class);
      executorIntent.putExtras(getIntent());
      executorIntent.setData(getIntent().getData());
      startActivity(executorIntent);
      finish();
    }
    Eula.showEula(this);
//
//    if (!hasRegisteredExperiments()) {
//      Intent findMyExperimentsIntent = new Intent(this, FindMyExperimentsActivity.class);
//      startActivity(findMyExperimentsIntent);
//      finish();
//    } else {
//      Intent intent = new Intent(ExperimentManagerActivity.this, RunningExperimentsActivity.class);
//      intent.setData(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI);
//      startActivity(intent);
//    }

    setContentView(R.layout.experiment_manager_main);
    currentExperimentsButton = (ImageButton) findViewById(R.id.CurrentExperimentsBtn);

    currentExperimentsButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Intent intent = new Intent(ExperimentManagerActivity.this,
            RunningExperimentsActivity.class);
        intent.setData(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI);
        startActivity(intent);
      }
    });

    ImageButton findExperimentsButton = (ImageButton) findViewById(R.id.FindExperimentsBtn);
    findExperimentsButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        startActivity(new Intent(ExperimentManagerActivity.this,
            FindMyOrAllExperimentsChooserActivity.class));
      }
    });

    ImageButton exploreDataButton = (ImageButton) findViewById(R.id.ExploreDataBtn);
    exploreDataButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        startActivity(new Intent(ExperimentManagerActivity.this,
          ExploreDataActivity.class));
      }
    });

    ImageButton createExperimentsButton = (ImageButton) findViewById(R.id.CreateExperimentBtn);
    createExperimentsButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        String homepageAddr = getResources().getString(R.string.about_weburl);

        Resources res = getResources();
        String formattedMessage = String.format(res.getString(R.string.create_experiment_instructions), homepageAddr);

        new AlertDialog.Builder(v.getContext())
            .setMessage(formattedMessage)
        		.setTitle(R.string.create_experiment)
        		.setCancelable(true)
        		.setPositiveButton(R.string.ok, new Dialog.OnClickListener() {

          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }

        }).create().show();
      }
      });


    ImageButton feedbackButton = (ImageButton)findViewById(R.id.FeedbackButton);
    feedbackButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        startActivity(new Intent(ExperimentManagerActivity.this,
                ContactOptionsActivity.class));

      }
    });

    ImageButton helpButton = (ImageButton)findViewById(R.id.HelpButton);
    helpButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        startActivity(new Intent(ExperimentManagerActivity.this,
                HelpActivity.class));

      }
    });


  }

  private Experiment getExperimentFromIntent() {
    Uri uri = getIntent().getData();
    if (uri == null) {
      return null;
    }
    return experimentProviderUtil.getExperiment(uri);
  }

  @Override
  protected void onResume() {
    super.onResume();
    //login(loginHelper);
    currentExperimentsButton.setEnabled(hasRegisteredExperiments());
  }

  private boolean hasRegisteredExperiments() {
    return experimentProviderUtil.hasJoinedExperiments();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, ABOUT_PACO_ITEM, 1, R.string.about_paco_menu_item);
    menu.add(0, DEBUG_ITEM, 7, R.string.debug_menu_item);
    menu.add(0, SERVER_ADDRESS_ITEM, 6, R.string.server_address_menu_item);
//    menu.add(0, UPDATE_ITEM, 4, R.string.check_updates_menu_item);
    menu.add(0, ACCOUNT_CHOOSER_ITEM, 3, R.string.choose_account_menu_item);
    menu.add(0, RINGTONE_CHOOSER_ITEM, 2, R.string.choose_alert_menu_item);
    menu.add(0, SEND_LOG_ITEM, 4, R.string.send_log_menu_item);
    menu.add(0, EULA_ITEM, 4, R.string.eula_menu_item);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case ABOUT_PACO_ITEM:
      launchPaco();
      return true;
    case DEBUG_ITEM:
      launchDebug();
      return true;
    case SERVER_ADDRESS_ITEM:
      launchServerConfiguration();
      return true;
    case UPDATE_ITEM:
      launchUpdateCheck();
      return true;
    case ACCOUNT_CHOOSER_ITEM:
      launchAccountChooser();
      return true;
    case RINGTONE_CHOOSER_ITEM:
      launchRingtoneChooser();
      return true;
    case SEND_LOG_ITEM:
      launchLogSender();
      return true;
    case EULA_ITEM:
      launchEula();
      return true;

    default:
      return false;
    }
  }

  private void launchEula() {
    Intent eulaIntent = new Intent(this, EulaDisplayActivity.class);
    startActivity(eulaIntent);
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
    String uri = userPreferences.getRingtone();
    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, R.string.select_signal_tone);
    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
    if (uri != null) {
      intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(uri));
    }
    else {
      intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
    }

    startActivityForResult(intent, RINGTONE_REQUESTCODE);
  }

  private void installPacoBarkRingtone() {
    UserPreferences userPreferences = new UserPreferences(this);
    if (userPreferences.hasInstalledPacoBarkRingtone()) {
      return;
    }

    File f = copyRingtoneFromAssetsToSdCard();
    if (f == null) {
      return;
    }
    ContentValues values = createBarkRingtoneDatabaseEntry(f);
    Uri uri = MediaStore.Audio.Media.getContentUriForPath(f.getAbsolutePath());
    ContentResolver mediaStoreContentProvider = getBaseContext().getContentResolver();
    Cursor existingRingtoneCursor = mediaStoreContentProvider.query(uri, null, null, null, null); // Note: i want to just retrieve MediaStore.MediaColumns.TITLE and to search on the match, but it is returning null for the TITLE value!!!
    Cursor c = mediaStoreContentProvider.query(uri, null, null, null, null);
    boolean alreadyInstalled = false;
    while (c.moveToNext()) {
      int titleColumnIndex = c.getColumnIndex(RINGTONE_TITLE_COLUMN_NAME);
      String ringtoneTitle = c.getString(titleColumnIndex);
      if (PACO_BARK_RINGTONE_TITLE.equals(ringtoneTitle)) {
        alreadyInstalled = true;
      }
    }
    existingRingtoneCursor.close();

    if (!alreadyInstalled) {
      Uri newUri = mediaStoreContentProvider.insert(uri, values);
      if (newUri != null) {
        userPreferences.setRingtone(newUri.toString());
        userPreferences.setPacoBarkRingtoneInstalled();
      }
    }


  }

  private File copyRingtoneFromAssetsToSdCard()  {
    InputStream fis = null;
    OutputStream fos = null;
    try {
      fis = getAssets().open(BARK_RINGTONE_FILENAME);

      if (fis == null) {
        return null;
      }

      File path = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath()
                           + "/Android/data/" + getPackageName() + "/");
      if (!path.exists()) {
        path.mkdirs();
      }

      File f = new File(path, BARK_RINGTONE_FILENAME);
      fos = new FileOutputStream(f);
      byte[] buf = new byte[1024];
      int len;
      while ((len = fis.read(buf)) > 0) {
        fos.write(buf, 0, len);
      }
      return f;
    } catch (FileNotFoundException e) {
      Log.e(PacoConstants.TAG, "Could not create ringtone file on sd card. Error = " + e.getMessage());
    } catch (IOException e) {
      Log.e(PacoConstants.TAG, "Either Could not open ringtone from assets. Or could not write to sd card. Error = " + e.getMessage());
      return null;
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
          Log.e(PacoConstants.TAG, "could not close sd card file handle. Error = " + e.getMessage());
        }
      }
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
          Log.e(PacoConstants.TAG, "could not close asset file handle. Error = " + e.getMessage());
        }
      }
    }
    return null;
  }

  private ContentValues createBarkRingtoneDatabaseEntry(File f) {
    ContentValues values = new ContentValues();
    values.put(MediaStore.MediaColumns.DATA, f.getAbsolutePath());
    values.put(MediaStore.MediaColumns.TITLE, PACO_BARK_RINGTONE_TITLE);
    values.put(MediaStore.MediaColumns.SIZE, f.length());
    values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
    values.put(MediaStore.Audio.Media.ARTIST, "Paco");
    // values.put(MediaStore.Audio.Media.DURATION, ""); This is not needed
    values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
    values.put(MediaStore.Audio.Media.IS_ALARM, false);
    values.put(MediaStore.Audio.Media.IS_MUSIC, false);
    return values;
  }

  private void launchServerConfiguration() {
    Intent startIntent = new Intent(this, ServerConfiguration.class);
    startActivity(startIntent);
  }

  private void launchDebug() {
    Intent startIntent = new Intent(this, ESMSignalViewer.class);
    startActivity(startIntent);
  }

  private void launchPaco() {
    Intent startIntent = new Intent(this, WelcomeActivity.class);
    startActivity(startIntent);
  }

  private void launchUpdateCheck() {
    Intent debugIntent = new Intent("com.google.android.apps.paco.UPDATE");
    startActivityForResult(debugIntent, CHECK_UPDATE_REQUEST_CODE);
  }

  private void launchAccountChooser() {
    Intent intent = new Intent(this, com.google.android.apps.paco.AccountChooser.class);
    startActivity(intent);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == CHECK_UPDATE_REQUEST_CODE && resultCode == RESULT_OK) {
      finish();
    } else if (requestCode == RINGTONE_REQUESTCODE && resultCode == RESULT_OK) {
      Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
      if (uri != null) {
        new UserPreferences(this).setRingtone(uri.toString());
      } else {
        new UserPreferences(this).clearRingtone();
      }

    }
  }


}

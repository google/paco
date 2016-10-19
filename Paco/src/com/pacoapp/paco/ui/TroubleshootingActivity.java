package com.pacoapp.paco.ui;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.pacoapp.paco.R;
import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.net.NetworkUtil;
import com.pacoapp.paco.net.SyncService;
import com.pacoapp.paco.sensors.android.diagnostics.DiagnosticReport;
import com.pacoapp.paco.sensors.android.diagnostics.DiagnosticsReporter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TroubleshootingActivity extends ActionBarActivity {

  private static final int EMAIL_INTENT_REQUEST_CODE = 1093;
  private static Logger Log = LoggerFactory.getLogger(TroubleshootingActivity.class);
  private UserPreferences userPrefs;
  private DiagnosticReport diagnosticsReport;
  private TextView resultsTextView;
  private Button forceSyncButton;
  private ExperimentProviderUtil experimentProviderUtil;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_troubleshooting);

    userPrefs = new UserPreferences(getApplicationContext());

    ActionBar actionBar = getSupportActionBar();
    actionBar.setLogo(R.drawable.ic_launcher);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));
    actionBar.setDisplayHomeAsUpEnabled(true);

    resultsTextView = (TextView)findViewById(R.id.resultsTextView2);
    final Button sendLogButton = (Button)findViewById(R.id.sendPacoLogButton);
    sendLogButton.setEnabled(false);
    sendLogButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        launchLogSenderZip();
      }
    });

    Button checkSetupButton = (Button)findViewById(R.id.checkSetupButton);
    checkSetupButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        runTests();
        sendLogButton.setEnabled(true);
      }
    });

    forceSyncButton = (Button)findViewById(R.id.forceSyncButton);
    forceSyncButton.setEnabled(false);

    forceSyncButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        startService(new Intent(TroubleshootingActivity.this, SyncService.class));
      }
    });


  }



  protected void runTests() {
    diagnosticsReport = new DiagnosticsReporter().runTests(this);
    String res = diagnosticsReport.toString();
    resultsTextView.setText(res);

    forceSyncButton.setEnabled(NetworkUtil.isConnected(this) && hasUnsyncedEvents());

  }



  private boolean hasUnsyncedEvents() {
    // TODO make diagnostics cleaner and use that value
    ExperimentProviderUtil ep = new ExperimentProviderUtil(this);
    List<Event> events = ep.getEventsNeedingUpload();
    return events != null && events.size() > 0;
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


  private void launchLogSenderZip() {
    Log.debug("Building Troubleshooting email");
    String res = getString(R.string.troubleshooting_no_diagnostics_report_message);
    if (diagnosticsReport != null) {
      res = diagnosticsReport.toString();
    }

    String zipFilePath = writeLogsIntoZip();

    experimentProviderUtil = new ExperimentProviderUtil(this);
    List<Experiment> experiments = experimentProviderUtil.getJoinedExperiments();
    String email = null;
    if (experiments != null && experiments.size() == 1) {
      email = experiments.get(0).getExperimentDAO().getContactEmail();
      if (Strings.isNullOrEmpty(email)) {
        email = experiments.get(0).getExperimentDAO().getCreator();
      }
    }

    createEmailIntentZip(res, zipFilePath);
  }

  private void createEmailIntentZip(String log, String zipFilePath) {
    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.email_subject_paco_feedback));
    emailIntent.setType("plain/text");
    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, log);
    if (!Strings.isNullOrEmpty(zipFilePath)) {
      Uri uri = Uri.fromFile(new File(zipFilePath));
      emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
    }
    Log.debug("Launching email intent");
    startActivityForResult(emailIntent, EMAIL_INTENT_REQUEST_CODE);
  }

  private String writeLogsIntoZip() {
    File logFileDir = getFilesDir(); 
    if (logFileDir.exists()) {
      File[] logFiles = logFileDir.listFiles(new FilenameFilter() {
        private Pattern logFilePattern = Pattern.compile("log.*");

        @Override
        public boolean accept(File dir, String name) {
          return logFilePattern.matcher(name).matches();
        }

      });
      if (logFiles != null) {
        try {
          File zippedLogFile = null;
          int BUFFER = 2048;
          BufferedInputStream origin = null;
          final File externalFilesDir = getExternalFilesDir(null);
          long freeSpace = externalFilesDir.getFreeSpace();
          final int spaceMinLimit = 1024 * 1000; // 1MB
          if (freeSpace >= spaceMinLimit && externalFilesDir.canWrite()) {
            zippedLogFile = new File(externalFilesDir, DateTime.now().toString() + "logs.zip");
            zippedLogFile.setReadable(true, false);
            FileOutputStream dest = new FileOutputStream(zippedLogFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[BUFFER];

            for (int i = 0; i < logFiles.length; i++) {
              final File fileName = logFiles[i];
              Log.debug("Compress: Adding: " + fileName);
              FileInputStream fi = new FileInputStream(fileName);
              origin = new BufferedInputStream(fi, BUFFER);
              ZipEntry entry = new ZipEntry(fileName.getName());
              out.putNextEntry(entry);
              int count;
              while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
              }
              origin.close();
            }
            ZipEntry entry = new ZipEntry("syslog.log");
            out.putNextEntry(entry);

            Process process = Runtime.getRuntime().exec("logcat -d");
            origin = new BufferedInputStream(process.getInputStream(), BUFFER);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
              out.write(data, 0, count);
            }
            origin.close();
            out.close();
            final String zippedLogFileAbsolutePath = zippedLogFile.getAbsolutePath();
            userPrefs.setZipLogFileUri(zippedLogFileAbsolutePath);
            return zippedLogFileAbsolutePath;
          } else {
            Log.debug("Either freespace is too low on external storage to write logs or cannot write externalDir. "
                    + "Freespace: " + freeSpace +", canWrite: " + externalFilesDir.canWrite());
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }


  @SuppressLint("NewApi")
  private void launchAccountChooser() {
    Intent intent = new Intent(this, SplashActivity.class);
    intent.putExtra(SplashActivity.EXTRA_CHANGING_EXISTING_ACCOUNT, true);
    startActivity(intent);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == EMAIL_INTENT_REQUEST_CODE) {
      String path = userPrefs.getZipLogFileUri();
      if (path != null) {
        File f = new File(path);
        if (f.exists()) {
          Log.debug("Found zipLogFile to delete: " + path);
          //f.delete();
        }
      }
    }
  }


  @Override
  protected void onResume() {
    super.onResume();
  }


  @Override
  protected void onPause() {
    super.onPause();
  }


}

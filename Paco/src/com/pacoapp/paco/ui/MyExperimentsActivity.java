/*
 * Copyright 2011 Google Inc. All Rights Reserved.
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
package com.pacoapp.paco.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.joda.time.DateTime;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.apps.paco.AccountChooser;
import com.google.android.apps.paco.AndroidEsmSignalStore;
import com.google.android.apps.paco.BeeperService;
import com.google.android.apps.paco.BroadcastTriggerReceiver;
import com.google.android.apps.paco.DownloadFullExperimentsTask;
import com.google.android.apps.paco.DownloadHelper;
import com.google.android.apps.paco.ESMSignalViewer;
import com.google.android.apps.paco.EulaDisplayActivity;
import com.google.android.apps.paco.Event;
import com.google.android.apps.paco.Experiment;
import com.google.android.apps.paco.ExperimentExecutor;
import com.google.android.apps.paco.ExperimentExecutorCustomRendering;
import com.google.android.apps.paco.ExperimentGroupPicker;
import com.google.android.apps.paco.ExperimentProviderUtil;
import com.google.android.apps.paco.FeedbackActivity;
import com.google.android.apps.paco.FindExperimentsActivity;
import com.google.android.apps.paco.FindMyOrAllExperimentsChooserActivity;
import com.google.android.apps.paco.NotificationCreator;
import com.google.android.apps.paco.Output;
import com.google.android.apps.paco.ServerConfiguration;
import com.google.android.apps.paco.SyncService;
import com.google.android.apps.paco.UserPreferences;
import com.google.android.apps.paco.WelcomeActivity;
import com.google.common.collect.Lists;
import com.google.paco.shared.model2.ExperimentGroup;
import com.google.paco.shared.util.ExperimentHelper;
import com.pacoapp.paco.R;
import com.pacoapp.paco.os.RingtoneUtil;

/**
 *
 */
public class MyExperimentsActivity extends ActionBarActivity {

  private static final int RINGTONE_REQUESTCODE = 945;
  private static final int ABOUT_PACO_ITEM = 3;
  private static final int DEBUG_ITEM = 4;
  private static final int SERVER_ADDRESS_ITEM = 5;
  private static final int ACCOUNT_CHOOSER_ITEM = 7;
  private static final int RINGTONE_CHOOSER_ITEM = 8;
  private static final int SEND_LOG_ITEM = 9;
  private static final int EULA_ITEM = 10;

  public static final int REFRESHING_EXPERIMENTS_DIALOG_ID = 1001;
  private static final int FIND_EXPERIMENTS_ITEM = 11;

  private ExperimentProviderUtil experimentProviderUtil;
  private ListView list;
  private ViewGroup mainLayout;
  public UserPreferences userPrefs;

  private List<Experiment> experiments = Lists.newArrayList();
  protected AvailableExperimentsListAdapter adapter;

  private static DownloadFullExperimentsTask experimentDownloadTask;

  @SuppressLint("NewApi")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActionBar actionBar = getSupportActionBar();
    actionBar.setLogo(R.drawable.ic_launcher);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));
    // TODO would this work if it is in the Systemchangereceiver ?
    new RingtoneUtil(this).installPacoBarkRingtone();

    mainLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_current_experiments, null);
    setContentView(mainLayout);

    userPrefs = new UserPreferences(this);
    list = (ListView) findViewById(R.id.find_experiments_list);
    list.setBackgroundColor(333);
    createListHeader();

    experimentProviderUtil = new ExperimentProviderUtil(this);


    registerForContextMenu(list);
  }

  public void reloadAdapter() {
    experiments = experimentProviderUtil.getJoinedExperiments();
    sortExperimentsByTitle(experiments);
    adapter = new AvailableExperimentsListAdapter(this, R.id.find_experiments_list, experiments);
    list.setAdapter(adapter);
  }

  public static void sortExperimentsByTitle(List<Experiment> experiments2) {
    Collections.sort(experiments2, new Comparator<Experiment>() {
      @Override
      public int compare(Experiment lhs, Experiment rhs) {
        return lhs.getExperimentDAO().getTitle().toLowerCase()
                  .compareTo(rhs.getExperimentDAO().getTitle().toLowerCase());
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (userPrefs.getSelectedAccount() == null) {
      Intent acctChooser = new Intent(this, AccountChooser.class);
      this.startActivity(acctChooser);
    }
    reloadAdapter();
  }

  private void showDataForExperiment(Experiment experiment, List<ExperimentGroup> groups) {
    Intent experimentIntent = null;
    if (groups.size() > 1) {
      experimentIntent = new Intent(MyExperimentsActivity.this, ExperimentGroupPicker.class);
      experimentIntent.putExtra(ExperimentGroupPicker.SHOULD_GO_TO_RENDER_NEXT, ExperimentGroupPicker.FEEDBACK_NEXT);
    } else {
      experimentIntent = new Intent(MyExperimentsActivity.this, FeedbackActivity.class);
      experimentIntent.putExtra(Experiment.EXPERIMENT_GROUP_NAME_EXTRA_KEY, groups.get(0).getName());
    }
    experimentIntent.putExtra(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, experiment.getExperimentDAO().getId());
    startActivity(experimentIntent);
  }

  // Visible for testing
  public void deleteExperiment(long id) {
    NotificationCreator nc = NotificationCreator.create(this);
    nc.timeoutNotificationsForExperiment(id);

    Experiment experiment = experimentProviderUtil.getExperimentByServerId(id);
    createStopEvent(experiment);

    experimentProviderUtil.deleteExperiment(experiment.getId());
    if (ExperimentHelper.shouldWatchProcesses(experiment.getExperimentDAO())) {
      BroadcastTriggerReceiver.initPollingAndLoggingPreference(this);
    }

    new AndroidEsmSignalStore(this).deleteAllSignalsForSurvey(id);

    reloadAdapter();
    startService(new Intent(MyExperimentsActivity.this, BeeperService.class));
  }

  /**
   * Creates a pacot for stopping an experiment
   *
   * @param experiment
   */
  private void createStopEvent(Experiment experiment) {
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getExperimentDAO().getId());
    event.setExperimentName(experiment.getExperimentDAO().getTitle());
    event.setExperimentVersion(experiment.getExperimentDAO().getVersion());
    event.setResponseTime(new DateTime());

    Output responseForInput = new Output();
    responseForInput.setAnswer("false");
    responseForInput.setName("joined");
    event.addResponse(responseForInput);

    experimentProviderUtil.insertEvent(event);
    startService(new Intent(this, SyncService.class));
  }

  private void editExperiment(Experiment experiment, List<ExperimentGroup> groups) {
    Intent experimentIntent = new Intent(MyExperimentsActivity.this, ScheduleListActivity.class);
    experimentIntent.putExtra(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, experiment.getExperimentDAO().getId());
    startActivity(experimentIntent);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RINGTONE_REQUESTCODE && resultCode == RESULT_OK) {
      Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
      if (uri != null) {
        new UserPreferences(this).setRingtone(uri.toString());
      } else {
        new UserPreferences(this).clearRingtone();
      }
    }

  }

  private TextView createListHeader() {
    TextView listHeader = (TextView) findViewById(R.id.ExperimentListTitle);
    String header = getString(R.string.your_current_experiments);
    listHeader.setText(header);
    listHeader.setTextSize(25);
    listHeader.setBackgroundColor(0xffdddddd);
    return listHeader;
  }

//  private TextView createRefreshHeader() {
//    TextView listHeader = (TextView) findViewById(R.id.ExperimentRefreshTitle);
//    DateTime lastRefresh = userPrefs.getJoinedExperimentListRefreshTime();
//    if (lastRefresh == null) {
//      listHeader.setVisibility(View.GONE);
//    } else {
//      String lastRefreshTime = TimeUtil.formatDateTime(lastRefresh);
//      String header = getString(R.string.last_refreshed) + ": " + lastRefreshTime;
//      listHeader.setText(header);
//      listHeader.setTextSize(15);
//    }
//    return listHeader;
//  }

  protected Dialog onCreateDialog(int id, Bundle args) {
    switch (id) {
    case REFRESHING_EXPERIMENTS_DIALOG_ID: {
      return getRefreshJoinedDialog();
    }
    case DownloadHelper.INVALID_DATA_ERROR: {
      return getUnableToJoinDialog(getString(R.string.invalid_data));
    }
    case DownloadHelper.SERVER_ERROR: {
      return getUnableToJoinDialog(getString(R.string.dialog_dismiss));
    }
    case DownloadHelper.NO_NETWORK_CONNECTION: {
      return getNoNetworkDialog();
    }
    default: {
      return null;
    }
    }
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    return super.onCreateDialog(id);
  }

  private ProgressDialog getRefreshJoinedDialog() {
    return ProgressDialog.show(this, getString(R.string.experiment_refresh),
                               getString(R.string.updating_your_joined_experiments_from_the_server), true, true);
  }

  private AlertDialog getUnableToJoinDialog(String message) {
    AlertDialog.Builder unableToJoinBldr = new AlertDialog.Builder(this);
    unableToJoinBldr.setTitle(R.string.experiment_could_not_be_retrieved).setMessage(message)
                    .setPositiveButton(R.string.dialog_dismiss, new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int which) {
                        setResult(FindExperimentsActivity.JOINED_EXPERIMENT);
                        finish();
                      }
                    });
    return unableToJoinBldr.create();
  }

  private AlertDialog getNoNetworkDialog() {
    AlertDialog.Builder noNetworkBldr = new AlertDialog.Builder(this);
    noNetworkBldr.setTitle(R.string.network_required).setMessage(getString(R.string.need_network_connection))
                 .setPositiveButton(R.string.go_to_network_settings, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {
                     showNetworkConnectionActivity();
                   }
                 }).setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {
                     setResult(FindExperimentsActivity.JOINED_EXPERIMENT);
                     finish();
                   }
                 });
    return noNetworkBldr.create();
  }

  private void showNetworkConnectionActivity() {
    startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), DownloadHelper.ENABLED_NETWORK);
  }

  private class AvailableExperimentsListAdapter extends ArrayAdapter<Experiment> {

    private LayoutInflater mInflater;

    AvailableExperimentsListAdapter(Context context, int resourceId, List<Experiment> experiments) {
      super(context, resourceId, experiments);
      mInflater = LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      View view = convertView;
      if (view == null) {
        view = mInflater.inflate(R.layout.experiment_list_row, null);
      }

      Experiment experiment = getItem(position);

      TextView tv = (TextView) view.findViewById(R.id.experimentListRowTitle);
      tv.setText(experiment != null ? experiment.getExperimentDAO().getTitle() : "ERROR");
      tv.setOnClickListener(myButtonListener);

      tv.setTag(experiment.getExperimentDAO().getId());

      ImageButton editButton = (ImageButton) view.findViewById(R.id.editExperimentButton);
      editButton.setOnClickListener(myButtonListener);
      editButton.setTag(experiment.getExperimentDAO().getId());

      editButton.setEnabled(ExperimentHelper.hasUserEditableSchedule(experiment.getExperimentDAO()));

      ImageButton quitButton = (ImageButton) view.findViewById(R.id.quitExperimentButton);
      quitButton.setOnClickListener(myButtonListener);
      quitButton.setTag(experiment.getExperimentDAO().getId());

      ImageButton exploreButton = (ImageButton) view.findViewById(R.id.exploreDataExperimentButton);
      exploreButton.setOnClickListener(myButtonListener);
      exploreButton.setTag(experiment.getExperimentDAO().getId());
      // show icon
      // ImageView iv = (ImageView) view.findViewById(R.id.explore_data_icon);
      // iv.setImageResource();
      return view;
    }

    private OnClickListener myButtonListener = new OnClickListener() {
      @Override
      public void onClick(final View v) {
        final int position = list.getPositionForView(v);
        if (position == ListView.INVALID_POSITION) {
          return;
        } else {
          final Long experimentServerId = (Long) v.getTag();
          final Experiment experiment = experiments.get(position);
          final List<ExperimentGroup> groups = experiment.getExperimentDAO().getGroups();

          if (v.getId() == R.id.editExperimentButton) {
            editExperiment(experiment, groups);
          } else if (v.getId() == R.id.exploreDataExperimentButton) {
            showDataForExperiment(experiment, groups);
          } else if (v.getId() == R.id.quitExperimentButton) {
            new AlertDialog.Builder(MyExperimentsActivity.this).setCancelable(true)
                                                               .setTitle(R.string.stop_the_experiment_dialog_title)
                                                               .setMessage(R.string.stop_experiment_dialog_body)
                                                               .setPositiveButton(R.string.yes,
                                                                                  new Dialog.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(DialogInterface dialog,
                                                                                                        int which) {
                                                                                      deleteExperiment(experimentServerId);
                                                                                    }
                                                                                  })
                                                               .setNegativeButton(R.string.no,
                                                                                  new Dialog.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(DialogInterface dialog,
                                                                                                        int which) {
                                                                                      dialog.dismiss();
                                                                                    }
                                                                                  }).create().show();

          } else if (v.getId() == R.id.experimentListRowTitle) {
            Intent experimentIntent = null;
            if (groups.size() > 1) {
              experimentIntent = new Intent(MyExperimentsActivity.this, ExperimentGroupPicker.class);
              experimentIntent.putExtra(ExperimentGroupPicker.SHOULD_GO_TO_RENDER_NEXT,
                                        ExperimentGroupPicker.RENDER_NEXT);
            } else {
              Class clazz = null;
              final ExperimentGroup experimentGroup = groups.get(0);
              if (experimentGroup.getCustomRendering()) {
                clazz = ExperimentExecutorCustomRendering.class;
              } else {
                clazz = ExperimentExecutor.class;
              }
              experimentIntent = new Intent(MyExperimentsActivity.this, clazz);
              experimentIntent.putExtra(Experiment.EXPERIMENT_GROUP_NAME_EXTRA_KEY, experimentGroup.getName());

            }
            experimentIntent.putExtra(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, experimentServerId);
            startActivity(experimentIntent);
          }
        }
      }
    };
  }

  @SuppressLint("NewApi")
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuItem item = menu.add(0, FIND_EXPERIMENTS_ITEM, 1, "Find Experiments");
    item.setIcon(android.R.drawable.ic_menu_search);
    if (Integer.parseInt(Build.VERSION.SDK) >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }
    item = menu.add(0, RINGTONE_CHOOSER_ITEM, 2, R.string.choose_alert_menu_item);
    addToActionBar(item);
    item = menu.add(0, ACCOUNT_CHOOSER_ITEM, 3, R.string.choose_account_menu_item);
    addToActionBar(item);
    item = menu.add(0, ABOUT_PACO_ITEM, 4, R.string.about_paco_menu_item);
    addToActionBar(item);
    item = menu.add(0, EULA_ITEM, 5, R.string.eula_menu_item);
    addToActionBar(item);
    item = menu.add(0, SEND_LOG_ITEM, 6, R.string.send_log_menu_item);
    addToActionBar(item);
    item = menu.add(0, SERVER_ADDRESS_ITEM, 7, R.string.server_address_menu_item);
    addToActionBar(item);
    item = menu.add(0, DEBUG_ITEM, 8, R.string.debug_menu_item);
    addToActionBar(item);
    return true;
  }

  @SuppressLint("NewApi")
  public void addToActionBar(MenuItem item) {
    if (Integer.parseInt(Build.VERSION.SDK) >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }
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
    case FIND_EXPERIMENTS_ITEM:
      launchFindExperiments();
      return true;
    default:
      return false;
    }
  }

  private void launchFindExperiments() {
    startActivity(new Intent(this, FindMyOrAllExperimentsChooserActivity.class));

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
    } else {
      intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                      RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
    }

    startActivityForResult(intent, RINGTONE_REQUESTCODE);
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

  private void launchAccountChooser() {
    Intent intent = new Intent(this, com.google.android.apps.paco.AccountChooser.class);
    startActivity(intent);
  }

}

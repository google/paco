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
package com.google.android.apps.paco;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.pacoapp.paco.R;


/**
 *
 */
public class RunningExperimentsActivity extends Activity {

  public static final int REFRESHING_EXPERIMENTS_DIALOG_ID = 1001;

  private static final int DATA_EXPERIMENT_OPTION = 3;
  private static final int STOP_EXPERIMENT_OPTION = 2;
  private static final int EDIT_EXPERIMENT_OPTION = 1;
  static final int JOIN_REQUEST_CODE = 1;
  static final int JOINED_EXPERIMENT = 1;

  private ExperimentProviderUtil experimentProviderUtil;
  private ListView list;
  private ViewGroup mainLayout;
  public UserPreferences userPrefs;

  private List<Experiment> experiments = Lists.newArrayList();
  protected AvailableExperimentsListAdapter adapter;

  private static DownloadFullExperimentsTask experimentDownloadTask;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mainLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.find_experiments, null);
    setContentView(mainLayout);
    Intent intent = getIntent();
    if (intent.getData() == null) {
      intent.setData(ExperimentColumns.CONTENT_URI);
    }

    userPrefs = new UserPreferences(this);
    list = (ListView) findViewById(R.id.find_experiments_list);
    list.setBackgroundColor(333);
    createListHeader();
    createRefreshHeader();

    experimentProviderUtil = new ExperimentProviderUtil(this);

    Button refreshButton = (Button) findViewById(R.id.RefreshExperimentsButton2);
    refreshButton.setVisibility(View.VISIBLE);

    refreshButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if (!isConnected()) {
          showDialog(DownloadHelper.NO_NETWORK_CONNECTION, null);
        } else {
          refreshList();
        }
      }
    });

    reloadAdapter();
    registerForContextMenu(list);
  }

  public void reloadAdapter() {
//    if (experiments == null || experiments.isEmpty()) {
      experiments = experimentProviderUtil.getJoinedExperiments();
      Collections.sort(experiments, new Comparator<Experiment>() {

        @Override
        public int compare(Experiment lhs, Experiment rhs) {

          return lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
        }

      });
//    }

    adapter = new AvailableExperimentsListAdapter(this,
                                                  R.id.find_experiments_list,
                                                  experiments);
    list.setAdapter(adapter);
  }

  private boolean isConnected() {
    return NetworkUtil.isConnected(this);
  }

  private void refreshList() {
    DownloadFullExperimentsTaskListener listener = new DownloadFullExperimentsTaskListener() {

      @Override
      public void done(String resultCode) {
        dismissDialog(REFRESHING_EXPERIMENTS_DIALOG_ID);
        if (resultCode == DownloadHelper.SUCCESS) {
          saveDownloadedExperiments();
          saveRefreshTime();
        } else {
          showFailureDialog(resultCode);
        }
      }
    };

    List<Long> joinedExperimentServerIds = experimentProviderUtil.getJoinedExperimentServerIds();
    if (joinedExperimentServerIds != null && joinedExperimentServerIds.size() > 0) {
      showDialog(REFRESHING_EXPERIMENTS_DIALOG_ID);
      experimentDownloadTask = new DownloadFullExperimentsTask(this, listener, userPrefs, joinedExperimentServerIds);
      experimentDownloadTask.execute();
    }
  }

  private void saveDownloadedExperiments() {
    String contentAsString = experimentDownloadTask.getContentAsString();
    if (contentAsString != null) {
      saveDownloadedExperiments(contentAsString);
    }
  }

  // Visible for testing
  public void saveDownloadedExperiments(String contentAsString) {
    try {
      experimentProviderUtil.updateExistingExperiments(contentAsString);
    } catch (JsonParseException e) {
      showFailureDialog(DownloadHelper.CONTENT_ERROR);
    } catch (JsonMappingException e) {
      showFailureDialog(DownloadHelper.CONTENT_ERROR);
    } catch (UnsupportedCharsetException e) {
      showFailureDialog(DownloadHelper.CONTENT_ERROR);
    } catch (IOException e) {
      showFailureDialog(DownloadHelper.CONTENT_ERROR);
    }
  }

  private void saveRefreshTime() {
    userPrefs.setJoinedExperimentListRefreshTime(new Date().getTime());
    TextView listHeader = (TextView)findViewById(R.id.ExperimentRefreshTitle);
    DateTime lastRefresh = userPrefs.getJoinedExperimentListRefreshTime();
    String header = getString(R.string.last_refreshed) + ": " + TimeUtil.formatDateTime(lastRefresh);
    listHeader.setText(header);
  }

  private void showFailureDialog(String status) {
    if (status.equals(DownloadHelper.CONTENT_ERROR) ||
        status.equals(DownloadHelper.RETRIEVAL_ERROR)) {
      showDialog(DownloadHelper.INVALID_DATA_ERROR, null);
    } else {
      showDialog(DownloadHelper.SERVER_ERROR, null);
    }
  }


  @Override
  protected void onResume() {
    super.onResume();
    if (userPrefs.getSelectedAccount() == null) {
      Intent acctChooser = new Intent(this, AccountChooser.class);
      this.startActivity(acctChooser);
    }
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    switch (item.getItemId()) {
    case EDIT_EXPERIMENT_OPTION:
      editExperiment(info.id);
      return true;
    case STOP_EXPERIMENT_OPTION:
      deleteExperiment(info.id);
      return true;
    case DATA_EXPERIMENT_OPTION:
      showDataForExperiment(info.id);
      return true;

    default:
      return super.onContextItemSelected(item);
      }
  }

  private void showDataForExperiment(long id) {
    Intent experimentIntent = new Intent(RunningExperimentsActivity.this, FeedbackActivity.class);
    experimentIntent.setData(Uri.withAppendedPath(getIntent().getData(), Long.toString(id)));
    startActivity(experimentIntent);
  }

  // Visible for testing
  public void deleteExperiment(long id) {

    NotificationCreator nc = NotificationCreator.create(this);
    nc.timeoutNotificationsForExperiment(id);
    Uri experimentUri = Uri.withAppendedPath(getIntent().getData(), Long.toString(id));
    Experiment experiment = experimentProviderUtil.getExperiment(id);
    createStopEvent(experiment);

    experimentProviderUtil.deleteFullExperiment(experimentUri);
    if (experiment.shouldWatchProcesses()) {
      BroadcastTriggerReceiver.initPollingAndLoggingPreference(this);
    }

    new AlarmStore(this).deleteAllSignalsForSurvey(id);

    reloadAdapter();
    startService(new Intent(RunningExperimentsActivity.this, BeeperService.class));
  }


  /**
   * Creates a pacot for stopping an experiment
   * @param experiment
   */
  private void createStopEvent(Experiment experiment) {
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getServerId());
    event.setExperimentName(experiment.getTitle());
    event.setExperimentVersion(experiment.getVersion());
    event.setResponseTime(new DateTime());

    Output responseForInput = new Output();
    responseForInput.setAnswer("false");
    responseForInput.setName("joined");
    event.addResponse(responseForInput);

    experimentProviderUtil.insertEvent(event);
    startService(new Intent(this, SyncService.class));
  }


  private void editExperiment(long id) {
    Intent experimentIntent = new Intent(RunningExperimentsActivity.this, ExperimentScheduleActivity.class);
    experimentIntent.setData(Uri.withAppendedPath(getIntent().getData(), Long.toString(id)));
    startActivity(experimentIntent);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    if (v.equals(list)) {
      menu.add(0, EDIT_EXPERIMENT_OPTION, 0, R.string.edit_schedule_menu_item);
      menu.add(0, STOP_EXPERIMENT_OPTION, 0, R.string.stop_experiment_menu_item);
      menu.add(0, DATA_EXPERIMENT_OPTION, 0, R.string.explore_data_menu_item);
    }
  }



  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == JOIN_REQUEST_CODE) {
      if (resultCode == JOINED_EXPERIMENT) {
        finish();
      }
    }
  }


  private TextView createListHeader() {
    TextView listHeader = (TextView)findViewById(R.id.ExperimentListTitle);
    String header = getString(R.string.running_experiments_title);
    listHeader.setText(header);
    listHeader.setTextSize(25);
    return listHeader;
  }

  private TextView createRefreshHeader() {
    TextView listHeader = (TextView)findViewById(R.id.ExperimentRefreshTitle);
    DateTime lastRefresh = userPrefs.getJoinedExperimentListRefreshTime();
    if (lastRefresh == null) {
      listHeader.setVisibility(View.GONE);
    } else {
      String lastRefreshTime = TimeUtil.formatDateTime(lastRefresh);
      String header = getString(R.string.last_refreshed) + ": " + lastRefreshTime;
      listHeader.setText(header);
      listHeader.setTextSize(15);
    }
    return listHeader;
  }


  protected Dialog onCreateDialog(int id, Bundle args) {
    switch (id) {
      case REFRESHING_EXPERIMENTS_DIALOG_ID: {
          return getRefreshJoinedDialog();
      } case DownloadHelper.INVALID_DATA_ERROR: {
          return getUnableToJoinDialog(getString(R.string.invalid_data));
      } case DownloadHelper.SERVER_ERROR: {
        return getUnableToJoinDialog(getString(R.string.dialog_dismiss));
      } case DownloadHelper.NO_NETWORK_CONNECTION: {
        return getNoNetworkDialog();
      } default: {
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
                               getString(R.string.updating_your_joined_experiments_from_the_server),
                               true, true);
  }

  private AlertDialog getUnableToJoinDialog(String message) {
    AlertDialog.Builder unableToJoinBldr = new AlertDialog.Builder(this);
    unableToJoinBldr.setTitle(R.string.experiment_could_not_be_retrieved)
                    .setMessage(message)
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
    noNetworkBldr.setTitle(R.string.network_required)
                 .setMessage(getString(R.string.need_network_connection))
                 .setPositiveButton(R.string.go_to_network_settings, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int which) {
                           showNetworkConnectionActivity();
                         }
                       })
                 .setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
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
      tv.setText(experiment != null ? experiment.getTitle() : "ERROR");
      tv.setOnClickListener(myButtonListener);

      tv.setTag(experiment.getId());

      ImageButton editButton = (ImageButton) view.findViewById(R.id.editExperimentButton);
      editButton.setOnClickListener(myButtonListener);
      editButton.setTag(experiment.getId());
      SignalingMechanism signalingMechanism = experiment.getSignalingMechanisms().get(0);
      editButton.setEnabled(signalingMechanism.getType().equals(SignalingMechanism.SIGNAL_SCHEDULE_TYPE)
                            && !((SignalSchedule) signalingMechanism).getScheduleType()
                                                                     .equals(SignalSchedule.SELF_REPORT));

      ImageButton quitButton = (ImageButton) view.findViewById(R.id.quitExperimentButton);
      quitButton.setOnClickListener(myButtonListener);
      quitButton.setTag(experiment.getId());

      ImageButton exploreButton = (ImageButton) view.findViewById(R.id.exploreDataExperimentButton);
      exploreButton.setOnClickListener(myButtonListener);
      exploreButton.setTag(experiment.getId());
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
        } else if (v.getId() == R.id.editExperimentButton) {
          editExperiment((Long) v.getTag());
        } else if (v.getId() == R.id.exploreDataExperimentButton) {
          showDataForExperiment((Long) v.getTag());
        } else if (v.getId() == R.id.quitExperimentButton) {
          new AlertDialog.Builder(RunningExperimentsActivity.this).setCancelable(true)
            .setTitle(R.string.stop_the_experiment_dialog_title)
            .setMessage(R.string.stop_experiment_dialog_body)
            .setPositiveButton(R.string.yes,
                               new Dialog.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog,
                                                     int which) {
                                   deleteExperiment((Long) v.getTag());
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
          Intent experimentIntent = new Intent(RunningExperimentsActivity.this, ExperimentExecutor.class);
          Uri uri = ContentUris.withAppendedId(getIntent().getData(), (Long) v.getTag());
          experimentIntent.setData(uri);
          startActivity(experimentIntent);
          finish();
        }
      }
    };
  }

}

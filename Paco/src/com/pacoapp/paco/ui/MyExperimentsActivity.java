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
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.paco.shared.model2.ExperimentGroup;
import com.google.paco.shared.util.ExperimentHelper;
import com.google.paco.shared.util.TimeUtil;
import com.pacoapp.paco.R;
import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.net.ExperimentUrlBuilder;
import com.pacoapp.paco.net.MyExperimentsFetchService;
import com.pacoapp.paco.net.MyExperimentsFetchService.ExperimentFetchListener;
import com.pacoapp.paco.net.MyExperimentsFetchService.LocalBinder;
import com.pacoapp.paco.net.NetworkClient;
import com.pacoapp.paco.net.NetworkUtil;
import com.pacoapp.paco.net.PacoBackgroundService;
import com.pacoapp.paco.net.SyncService;
import com.pacoapp.paco.os.RingtoneUtil;
import com.pacoapp.paco.sensors.android.BroadcastTriggerReceiver;
import com.pacoapp.paco.triggering.AndroidEsmSignalStore;
import com.pacoapp.paco.triggering.BeeperService;
import com.pacoapp.paco.triggering.NotificationCreator;

/**
 *
 */
public class MyExperimentsActivity extends ActionBarActivity implements
    NavigationDrawerFragment.NavigationDrawerCallbacks, NetworkClient {

  private static final int RINGTONE_REQUESTCODE = 945;
  public static final int REFRESHING_EXPERIMENTS_DIALOG_ID = 1001;

  private ExperimentProviderUtil experimentProviderUtil;
  private ListView list;
  private ViewGroup mainLayout;
  public UserPreferences userPrefs;

  private List<Experiment> experiments = Lists.newArrayList();
  protected AvailableExperimentsListAdapter adapter;
  private LinearLayout invitationLayout;
  private TextView invitationExperimentName;
  private TextView invitationContactTextView;
  private ImageButton invitationCloseButton;
  protected boolean bound;
  protected MyExperimentsFetchService mService;
  private List<Experiment> invitations;


  private ServiceConnection mConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
      LocalBinder binder = (LocalBinder) service;
      mService = binder.getService();
      bound = true;
      getAnyNewInvitations();
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      bound = false;
    }
  };

  private NavigationDrawerFragment mNavigationDrawerFragment;
  private ProgressBar progressBar;
  private ListView navDrawerList;



  @SuppressLint("NewApi")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mainLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_current_experiments, null);
    setContentView(mainLayout);


    ActionBar actionBar = getSupportActionBar();
    actionBar.setLogo(R.drawable.ic_launcher);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));


    // Set up the drawer.
    mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
    mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

    navDrawerList = (ListView)mNavigationDrawerFragment.getView().findViewById(R.id.navDrawerList);
    progressBar = (ProgressBar)findViewById(R.id.findExperimentsProgressBar);


    // TODO would this work if it is in the Systemchangereceiver ?
    new RingtoneUtil(this).installPacoBarkRingtone();

    userPrefs = new UserPreferences(this);

    list = (ListView) findViewById(R.id.find_experiments_list);
    list.setBackgroundColor(333);
    createListHeader();

    invitationLayout = (LinearLayout)findViewById(R.id.announcementLayout);
    invitationExperimentName = (TextView)findViewById(R.id.invitationExperimentNameTextView);
    invitationContactTextView = (TextView)findViewById(R.id.invitationContactTextView);
    invitationCloseButton = (ImageButton)findViewById(R.id.invitationAnnouncementCloseButton);

    experimentProviderUtil = new ExperimentProviderUtil(this);
    registerForContextMenu(list);
  }

  @Override
  public void onNavigationDrawerItemSelected(int position) {
    //navDrawerList.setItemChecked(position, true);
    switch (position) {
    case 0:
      // we are here launchMyCurrentExperiments();
      break;
    case 1:
      launchFindMyExperiments();
      break;
    case 2:
      launchFindPublicExperiments();
      break;
    case 3:
      launchCompletedExperiments();
      break;
    default:
      break;
    }
  }

  public void restoreActionBar() {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setTitle("Paco");
}


  private void getAnyNewInvitations() {
    mService.getExperiments(new ExperimentFetchListener() {


      @Override
      public void done(List<Experiment> invitations) {
        List<Experiment> unseenInvitations = removeSeenInvitations(invitations);
        unseenInvitations = removeJoinedExperiments(unseenInvitations);
        MyExperimentsActivity.this.invitations = unseenInvitations;
        runOnUiThread(new Runnable() {

          @Override
          public void run() {
            showInvitations(MyExperimentsActivity.this.invitations);

          }
        });

      }
    });
  }

  protected List<Experiment> removeJoinedExperiments(List<Experiment> invitations) {
    List<Long> joinedExperimentIds = Lists.newArrayList();
    for (Experiment experiment : experiments) {
      joinedExperimentIds.add(experiment.getExperimentDAO().getId());
    }
    List<Experiment> unseen = Lists.newArrayList();
    for (Experiment invitation : invitations) {
      final Long invitationId = invitation.getExperimentDAO().getId();
      if (!joinedExperimentIds.contains(invitationId)) {
        unseen.add(invitation);
      }
    }

    return unseen;
  }


  private List<Experiment> removeSeenInvitations(List<Experiment> invitations) {
    List<Long> seen = getSeenInvitations();
    List<Experiment> unseen = Lists.newArrayList();
    for (Experiment invitation : invitations) {
      final Long invitationId = invitation.getExperimentDAO().getId();
      if (!seen.contains(invitationId)) {
        unseen.add(invitation);
      }
    }

    return unseen;
  }

  public List<Long> getSeenInvitations() {
    return userPrefs.getSeenExperimentInvitationIds();
  }

  private void saveSeenInvitations(List<Long> seen) {
    userPrefs.saveSeenExperimentInvitations(seen);
  }

  private void markInvitationSeen(Experiment invitation) {
    Long serverId = invitation.getExperimentDAO().getId();
    List<Long> seen = getSeenInvitations();
    if (!seen.contains(serverId)) {
      seen.add(serverId);
      saveSeenInvitations(seen);
    }
  }

  protected void showInvitations(final List<Experiment> invitations) {
    if (invitations.size() == 0) {
      invitationLayout.setVisibility(View.GONE);
      return;
    }

    final Experiment invitation = invitations.get(0);

    invitationExperimentName.setText(invitation.getExperimentDAO().getTitle());
    String organization = invitation.getExperimentDAO().getOrganization();
    if (Strings.isNullOrEmpty(organization)) {
      organization = invitation.getExperimentDAO().getContactEmail();
    }
    invitationContactTextView.setText(organization);
    invitationLayout.setVisibility(View.VISIBLE);
    invitationLayout.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MyExperimentsActivity.this, ExperimentDetailActivity.class);
        intent.putExtra(ExperimentDetailActivity.ID_FROM_MY_EXPERIMENTS_FILE, true);
        intent.putExtra(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, invitation.getExperimentDAO().getId());
        startActivity(intent);
      }
    });
    invitationCloseButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        invitationLayout.setVisibility(View.GONE);
        markInvitationSeen(invitation);
        invitations.remove(invitation);
        showInvitations(invitations);
      }

    });
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
    if (userPrefs.getAccessToken() == null) {
      Intent splash = new Intent(this, SplashActivity.class);
      this.startActivity(splash);
//      Intent acctChooser = new Intent(this, AccountChooser.class);
//      this.startActivity(acctChooser);
    } else {
      reloadAdapter();
      if (invitationLayout.getVisibility() == View.VISIBLE) {
        List<Experiment> unseen = removeJoinedExperiments(invitations);
        unseen = removeSeenInvitations(unseen);
        invitations = unseen;
        showInvitations(unseen);
      }
    }
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
    case NetworkUtil.INVALID_DATA_ERROR: {
      return getUnableToJoinDialog(getString(R.string.invalid_data));
    }
    case NetworkUtil.SERVER_ERROR: {
      return getUnableToJoinDialog(getString(R.string.dialog_dismiss));
    }
    case NetworkUtil.NO_NETWORK_CONNECTION: {
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
    startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), NetworkUtil.ENABLED_NETWORK);
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
      tv.setText(experiment != null ? experiment.getExperimentDAO().getTitle() : "");
      tv.setOnClickListener(myButtonListener);
      tv.setTag(experiment.getExperimentDAO().getId());

      TextView organizationView = (TextView) view.findViewById(R.id.experimentListRowSubtitle);
      organizationView.setText(experiment != null ? "by " + experiment.getExperimentDAO().getOrganization() : "");
      organizationView.setTag(experiment.getExperimentDAO().getId());
      organizationView.setOnClickListener(myButtonListener);

      TextView joinDateView = (TextView) view.findViewById(R.id.experimentListRowJoinDate);
      joinDateView.setText(experiment != null ? "Joined on " + formatJoinDate(experiment) : "");
      joinDateView.setTag(experiment.getExperimentDAO().getId());
      joinDateView.setOnClickListener(myButtonListener);



//      ImageButton editButton = (ImageButton) view.findViewById(R.id.editExperimentButton);
//      editButton.setOnClickListener(myButtonListener);
//      editButton.setTag(experiment.getExperimentDAO().getId());
//
//      editButton.setEnabled(ExperimentHelper.hasUserEditableSchedule(experiment.getExperimentDAO()));
//
//      ImageButton quitButton = (ImageButton) view.findViewById(R.id.quitExperimentButton);
//      quitButton.setOnClickListener(myButtonListener);
//      quitButton.setTag(experiment.getExperimentDAO().getId());
//
//      ImageButton exploreButton = (ImageButton) view.findViewById(R.id.exploreDataExperimentButton);
//      exploreButton.setOnClickListener(myButtonListener);
//      exploreButton.setTag(experiment.getExperimentDAO().getId());
      // show icon
      // ImageView iv = (ImageView) view.findViewById(R.id.explore_data_icon);
      // iv.setImageResource();
      return view;
    }

    public String formatJoinDate(Experiment experiment) {
      final String joinDate = experiment.getJoinDate();
      DateTime dt = TimeUtil.parseDateWithZone(joinDate);
      return TimeUtil.formatDateLong(dt);
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

          /*if (v.getId() == R.id.editExperimentButton) {
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

          } else if (v.getId() == R.id.experimentListRowTitle) { */
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
          /*}*/
        }
      }
    };
  }

  @SuppressLint("NewApi")
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    int pos = 1;
    if (!mNavigationDrawerFragment.isDrawerOpen()) {
      getMenuInflater().inflate(R.menu.main, menu);
      restoreActionBar();
      return true;
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_find_experiments) {
      launchFindExperiments();
      return true;
    } else if (id == R.id.action_refreshMenuItem) {
      refreshExperiments();
      return true;
    } else if (id == R.id.action_settings) {
      launchSettings();
      return true;
    } else if (id == R.id.action_about) {
       launchAbout();
      return true;
    } else if (id == R.id.action_user_guide) {
      launchHelp();
      return true;
    } else if (id == R.id.action_user_agreement) {
      launchEula();
      return true;
    } else if (id == R.id.action_open_source_libs) {
      launchOpenSourceLibs();
      return true;
    } else if (id == R.id.action_email_paco_team) {
      launchEmailPacoTeam();
      return true;
    }
    return false;
  }

  private void refreshExperiments() {

    if (!NetworkUtil.isConnected(this)) {
      showDialog(NetworkUtil.NO_NETWORK_CONNECTION, null);
    } else {
      refreshList();
    }

  }

  private void refreshList() {
    List<Long> joinedExperimentServerIds = experimentProviderUtil.getJoinedExperimentServerIds();
    if (joinedExperimentServerIds != null && joinedExperimentServerIds.size() > 0) {
      progressBar.setVisibility(View.VISIBLE);
      final Long[] arrayOfIds = joinedExperimentServerIds.toArray(new Long[joinedExperimentServerIds.size()]);
      new PacoBackgroundService(this, ExperimentUrlBuilder.buildUrlForFullExperiment(userPrefs, arrayOfIds)).execute();
    }
  }

  private void saveDownloadedExperiments(ExperimentProviderUtil experimentProviderUtil,
                                         String contentAsString) {
    try {
      experimentProviderUtil.updateExistingExperiments(contentAsString);
    } catch (JsonParseException e) {
      // Nothing to be done here.
    } catch (JsonMappingException e) {
      // Nothing to be done here.
    } catch (UnsupportedCharsetException e) {
      // Nothing to be done here.
    } catch (IOException e) {
      // Nothing to be done here.
    }
  }

  private void launchFindExperiments() {
    startActivity(new Intent(this, FindMyOrAllExperimentsChooserActivity.class));
  }

  private void launchFindMyExperiments() {
    startActivity(new Intent(this, FindMyExperimentsActivity.class));
  }

  private void launchFindPublicExperiments() {
    startActivity(new Intent(this, FindExperimentsActivity.class));
  }

  private void launchCompletedExperiments() {
    //startActivity(new Intent(this, CompletedExperimentsActivity.class));
  }

  private void launchOpenSourceLibs() {
    startActivity(new Intent(this, OpenSourceLicenseListActivity.class));
  }

  private void launchSettings() {
    //startActivity(new Intent(this, FindMyOrAllExperimentsChooserActivity.class));
  }

  private void launchEula() {
    Intent eulaIntent = new Intent(this, EulaDisplayActivity.class);
    startActivity(eulaIntent);
  }

  private void launchHelp() {
    startActivity(new Intent(this, HelpActivity.class));
  }

  private void launchAbout() {
    Intent startIntent = new Intent(this, WelcomeActivity.class);
    startActivity(startIntent);
  }


  private void launchEmailPacoTeam() {
    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    String aEmailList[] = { getString(R.string.contact_email) };
    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Paco Feedback");
    emailIntent.setType("plain/text");
    startActivity(emailIntent);
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
    Intent startIntent = new Intent(this, ServerConfigurationActivity.class);
    startActivity(startIntent);
  }

  private void launchDebug() {
    Intent startIntent = new Intent(this, ESMSignalViewer.class);
    startActivity(startIntent);
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
  protected void onStop() {
    super.onStop();
    if (bound) {
      unbindService(mConnection);
      bound = false;
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    // Bind to LocalService
    if (userPrefs.getAccessToken() != null) {
      Intent intent = new Intent(this, MyExperimentsFetchService.class);
      bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
  }

  @Override
  public Context getContext() {
    return this;
  }

  @Override
  public void show(final String msg) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(MyExperimentsActivity.this, msg, Toast.LENGTH_LONG).show();
      }
    });
  }

  @Override
  public void showAndFinish(final String msg) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        progressBar.setVisibility(View.GONE);
        if (msg != null) {
          Toast.makeText(MyExperimentsActivity.this, "Download complete", Toast.LENGTH_LONG).show();
          saveDownloadedExperiments(experimentProviderUtil, msg);
          userPrefs.setJoinedExperimentListRefreshTime(new Date().getTime());
          reloadAdapter();
        } else {
          Toast.makeText(MyExperimentsActivity.this, "No experiment data retrieved. Try again.", Toast.LENGTH_LONG).show();
        }
      }
    });
  }

  @Override
  public void handleException(final Exception exception) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(MyExperimentsActivity.this, "Exception: " + exception.getMessage(), Toast.LENGTH_LONG).show();
      }
    });
  }


}

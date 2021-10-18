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

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
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
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.GroupTypeEnum;
import com.pacoapp.paco.shared.model2.Schedule;
import com.pacoapp.paco.shared.model2.ScheduleTrigger;
import com.pacoapp.paco.shared.util.ExperimentHelper;
import com.pacoapp.paco.shared.util.TimeUtil;
import com.pacoapp.paco.triggering.AndroidEsmSignalStore;
import com.pacoapp.paco.triggering.BeeperService;
import com.pacoapp.paco.triggering.ExperimentExpirationManagerService;
import com.pacoapp.paco.triggering.NotificationCreator;

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
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import androidx.fragment.app.FragmentManager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.PopupMenu;
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

/**
 *
 */
public class MyExperimentsActivity extends AppCompatActivity implements
    NavigationDrawerFragment.NavigationDrawerCallbacks, NetworkClient {

  private static final int RINGTONE_REQUESTCODE = 945;
  public static final int REFRESHING_EXPERIMENTS_DIALOG_ID = 1001;

  private Logger Log = LoggerFactory.getLogger(this.getClass());

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
    Log.info("MyExperimentsActivity onCreate");
    super.onCreate(savedInstanceState);
    mainLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_current_experiments, null);
    setContentView(mainLayout);



    // TODO would this work if it is in the Systemchangereceiver ?
    new RingtoneUtil(this).installPacoBarkRingtone();

    userPrefs = new UserPreferences(this);
    progressBar = (ProgressBar)findViewById(R.id.findExperimentsProgressBar);

    FragmentManager supportFragmentManager = getSupportFragmentManager();
    mNavigationDrawerFragment = (NavigationDrawerFragment) supportFragmentManager.findFragmentById(R.id.navigation_drawer);


    list = (ListView) findViewById(R.id.find_experiments_list);
    list.setBackgroundColor(333);
    experimentProviderUtil = new ExperimentProviderUtil(this);

    // Set up the drawer.



    invitationLayout = (LinearLayout)findViewById(R.id.announcementLayout);
    invitationExperimentName = (TextView)findViewById(R.id.invitationExperimentNameTextView);
    invitationContactTextView = (TextView)findViewById(R.id.invitationContactTextView);
    invitationCloseButton = (ImageButton)findViewById(R.id.invitationAnnouncementCloseButton);
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
    if (Strings.isNullOrEmpty(organization) || organization.equals("null")) {
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
    Log.info("MyExperimentsActivity onResume");
    super.onResume();
    if (userPrefs.getAccessToken() == null) {
      Intent splash = new Intent(this, SplashActivity.class);
      this.startActivity(splash);
//      Intent acctChooser = new Intent(this, AccountChooser.class);
//      this.startActivity(acctChooser);
    } else {
      ActionBar actionBar = getSupportActionBar();
      actionBar.setLogo(R.drawable.ic_launcher);
      actionBar.setDisplayUseLogoEnabled(true);
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));


      mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
      navDrawerList = (ListView)mNavigationDrawerFragment.getView().findViewById(R.id.navDrawerList);

      reloadAdapter();
      setListHeader();
      if (invitationLayout.getVisibility() == View.VISIBLE) {
        List<Experiment> unseen = removeJoinedExperiments(invitations);
        unseen = removeSeenInvitations(unseen);
        invitations = unseen;
        showInvitations(unseen);
      }
      registerForContextMenu(list);
    }
  }


  @Override
  protected void onPause() {
    Log.info("MyExperimentsActivity onPause");
    super.onPause();
    unregisterForContextMenu(list);
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
  public void deleteExperiment(Experiment experiment2) {
    NotificationCreator nc = NotificationCreator.create(this);
    nc.timeoutNotificationsForExperiment(experiment2.getExperimentDAO().getId());

    createStopEvent(experiment2);

    experimentProviderUtil.deleteExperiment(experiment2.getId());
    if (ExperimentHelper.shouldWatchProcesses(experiment2.getExperimentDAO())) {
      BroadcastTriggerReceiver.initPollingAndLoggingPreference(this);
    }

    new AndroidEsmSignalStore(this).deleteAllSignalsForSurvey(experiment2.getExperimentDAO().getId());

    reloadAdapter();
    startService(new Intent(this, BeeperService.class));
    startService(new Intent(this, ExperimentExpirationManagerService.class));
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

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (RingtoneUtil.isOkRingtoneResult(requestCode, resultCode)) {
      RingtoneUtil.updateRingtone(data, this);
    }
  }

  private TextView setListHeader() {
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
      return getUnableToJoinDialog(getString(R.string.ok));
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
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
      String organization = experiment.getExperimentDAO().getOrganization();
      if (Strings.isNullOrEmpty(organization) || organization.equals("null")) {
        organization = experiment.getExperimentDAO().getContactEmail();
      }
      organizationView.setText(experiment != null ? getString(R.string.by) + " " + organization : "");
      organizationView.setTag(experiment.getExperimentDAO().getId());
      organizationView.setOnClickListener(myButtonListener);

      TextView joinDateView = (TextView) view.findViewById(R.id.experimentListRowJoinDate);
      joinDateView.setText(experiment != null ? getString(R.string.joined_on) + " " + formatJoinDate(experiment) : "");
      joinDateView.setTag(experiment.getExperimentDAO().getId());
      joinDateView.setOnClickListener(myButtonListener);



      ImageButton menuButton = (ImageButton) view.findViewById(R.id.menuButton);
      menuButton.setOnClickListener(myButtonListener);
      menuButton.setTag(experiment.getExperimentDAO().getId());

      menuButton.setEnabled(true);
      menuButton.setOnClickListener(myButtonListener);
      return view;
    }

    public String formatJoinDate(Experiment experiment) {
      String joinDate = experiment.getJoinDate();
      if (joinDate == null) {
        joinDate = experiment.getExperimentDAO().getJoinDate();
      }
      if (joinDate == null) {
        return "";
      }
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
          List<ExperimentGroup> surveyGrps = ExperimentGroupPicker.getOnlySurveyGroups(groups);
          if (v.getId() == R.id.menuButton) {
            showPopup(experiment, v);
          } else {
            Intent experimentIntent = null;
            // TODO 
            if (surveyGrps.size() > 1) {
              experimentIntent = new Intent(MyExperimentsActivity.this, ExperimentGroupPicker.class);
              experimentIntent.putExtra(ExperimentGroupPicker.SHOULD_GO_TO_RENDER_NEXT,
                                        ExperimentGroupPicker.RENDER_NEXT);
            } else if (surveyGrps.size() == 1){
              Class clazz = null;
              final ExperimentGroup experimentGroup = surveyGrps.get(0);
              if (experimentGroup.getCustomRendering()) {
                clazz = ExperimentExecutorCustomRendering.class;
              } else {
                clazz = ExperimentExecutor.class;
              }
              experimentIntent = new Intent(MyExperimentsActivity.this, clazz);
              experimentIntent.putExtra(Experiment.EXPERIMENT_GROUP_NAME_EXTRA_KEY, experimentGroup.getName());

            } else {
              // TODO show them the experiment has no operable elements
            }
            experimentIntent.putExtra(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, experimentServerId);
            startActivity(experimentIntent);
          }
        }
      }
    };

    protected void showPopup(final Experiment experiment, View v) {
      PopupMenu popup = new PopupMenu(MyExperimentsActivity.this, v);
      final Menu menu = popup.getMenu();
      popup.getMenuInflater().inflate(R.menu.experiment_popup,
              menu);

      if (!userCanEditAtLeastOneSchedule(experiment)) {
        menu.removeItem(R.id.editSchedule);
      }
      popup.show();
      popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem item) {

              switch (item.getItemId()) {
              case R.id.editSchedule:
                  launchScheduleDetailScreen(experiment);
                  break;
              case R.id.emailResearcher:
                  emailResearcher(experiment);
                  break;
              case R.id.stopExperiment:
                deleteExperiment(experiment);
                break;
              default:
                  break;
              }

              return true;
          }


      });

    }


  }


  private boolean userCanEditAtLeastOneSchedule(Experiment experiment) {
      List<ExperimentGroup> groups = experiment.getExperimentDAO().getGroups();
      for (ExperimentGroup experimentGroup : groups) {
        List<ActionTrigger> actionTriggers = experimentGroup.getActionTriggers();
        for (ActionTrigger actionTrigger : actionTriggers) {
          if (actionTrigger instanceof ScheduleTrigger) {
            ScheduleTrigger scheduleTrigger = (ScheduleTrigger)actionTrigger;
            List<Schedule> schedules = scheduleTrigger.getSchedules();
            for (Schedule schedule : schedules) {
              if (schedule.getUserEditable()) {
                boolean userCanOnlyEditOnJoin = schedule.getOnlyEditableOnJoin();
                if (!userCanOnlyEditOnJoin) {
                  return true;
                }
              }
            }
          }

        }
      }
      return false;
    }
  protected void emailResearcher(Experiment experiment) {
    String contactEmail = experiment.getExperimentDAO().getContactEmail();
    if (Strings.isNullOrEmpty(contactEmail)) {
      contactEmail = experiment.getExperimentDAO().getCreator();
    }

    launchEmailTo(contactEmail);
  }

  private void launchScheduleDetailScreen(Experiment experiment) {
    Intent debugIntent = new Intent(this, ScheduleListActivity.class);
    debugIntent.putExtra(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, experiment.getExperimentDAO().getId());
    startActivity(debugIntent);
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
    } else if (id == R.id.action_preferences) {
      launchPreferences();
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
    } else if (id == R.id.action_troubleshooting) {
      launchTroubleshooting();
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
    Log.debug("MyExperimentsActivity refreshList");
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
    startActivity(new Intent(this, SettingsActivity.class));
  }

  private void launchPreferences() {
    startActivity(new Intent(this, PreferencesActivity.class));
  }

  private void launchTroubleshooting() {
    startActivity(new Intent(this, TroubleshootingActivity.class));
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
    launchEmailTo(getString(R.string.contact_email));
  }

  public void launchEmailTo(final String emailAddress) {
    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    String aEmailList[] = { emailAddress };
    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.email_subject_paco_feedback));
    emailIntent.setType("plain/text");
    startActivity(emailIntent);
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
    Log.info("MyExperimentsActivity onStop");
    super.onStop();
    if (bound) {
      unbindService(mConnection);
      bound = false;
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.debug("MyExperimentsActivity onStart");
    // Bind to LocalService
    if (userPrefs.getAccessToken() != null) {
      Log.debug("MyExperimentsActivity fetching new experiments");
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
          Toast.makeText(MyExperimentsActivity.this, getString(R.string.experiment_list_download_complete), Toast.LENGTH_LONG).show();
          saveDownloadedExperiments(experimentProviderUtil, msg);
          userPrefs.setJoinedExperimentListRefreshTime(new Date().getTime());
          reloadAdapter();
        } else {
          Toast.makeText(MyExperimentsActivity.this, getString(R.string.could_not_retrieve_experiments_try_again_), Toast.LENGTH_LONG).show();
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

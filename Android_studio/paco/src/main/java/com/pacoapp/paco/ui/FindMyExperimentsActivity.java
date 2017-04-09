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
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pacoapp.paco.R;
import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentColumns;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.net.ExperimentUrlBuilder;
import com.pacoapp.paco.net.NetworkClient;
import com.pacoapp.paco.net.NetworkUtil;
import com.pacoapp.paco.net.PacoForegroundService;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 */
public class FindMyExperimentsActivity extends ActionBarActivity implements NetworkActivityLauncher, NetworkClient,
                                                                NavigationDrawerFragment.NavigationDrawerCallbacks {

  static final int REFRESHING_EXPERIMENTS_DIALOG_ID = 1001;
  static final int JOIN_REQUEST_CODE = 1;
  static final int JOINED_EXPERIMENT = 1;

  private static Logger Log = LoggerFactory.getLogger(FindMyExperimentsActivity.class);

  private ExperimentProviderUtil experimentProviderUtil;
  private ListView list;
  private ProgressDialog p;
  private ViewGroup mainLayout;
  public UserPreferences userPrefs;
  protected AvailableExperimentsListAdapter adapter;
  private List<Experiment> experiments = Lists.newArrayList();
  private ProgressDialogFragment newFragment;
  private ProgressBar progressBar;
  private String experimentCursor;
  private boolean dialogable;
  private NavigationDrawerFragment mNavigationDrawerFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.debug("FindMyExperimentsActivity onCreate");
    mainLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.find_experiments, null);
    setContentView(mainLayout);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setLogo(R.drawable.ic_launcher);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));
    actionBar.setDisplayHomeAsUpEnabled(true);

    Intent intent = getIntent();
    if (intent.getData() == null) {
      intent.setData(ExperimentColumns.CONTENT_URI);
    }

    // Set up the drawer.
    // mNavigationDrawerFragment = (NavigationDrawerFragment)
    // getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
    // mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout)
    // findViewById(R.id.drawer_layout));

    userPrefs = new UserPreferences(this);
    list = (ListView) findViewById(R.id.find_experiments_list);
    // createListHeader();
    // createRefreshHeader();

    experimentProviderUtil = new ExperimentProviderUtil(this);

    progressBar = (ProgressBar) findViewById(R.id.findExperimentsProgressBar);

    reloadAdapter();
    list.setItemsCanFocus(false);
    list.setOnItemClickListener(new OnItemClickListener() {

      public void onItemClick(AdapterView<?> listview, View textview, int position, long id) {
        Experiment experiment = experiments.get(position);
        getIntent().putExtra(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, experiment.getServerId());

        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
          // The caller is waiting for us to return an experiment selected by
          // the user. The have clicked on one, so return it now.
          Intent resultIntent = new Intent();
          resultIntent.putExtra(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, experiment.getServerId());
          setResult(RESULT_OK, resultIntent);
        } else {
          Intent experimentIntent = new Intent(FindMyExperimentsActivity.this, ExperimentDetailActivity.class);
          experimentIntent.putExtra(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, experiment.getServerId());
          experimentIntent.putExtra(ExperimentDetailActivity.ID_FROM_MY_EXPERIMENTS_FILE, true);
          startActivityForResult(experimentIntent, JOIN_REQUEST_CODE);
        }
      }
    });
    // registerForContextMenu(list);
  }

  @Override
  public void onNavigationDrawerItemSelected(int position) {
    // navDrawerList.setItemChecked(position, true);
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
    actionBar.setTitle(R.string.app_name);
  }

  @SuppressLint("NewApi")
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    int pos = 1;
    if (true || !mNavigationDrawerFragment.isDrawerOpen()) {
      getMenuInflater().inflate(R.menu.main, menu);
      // restoreActionBar();
      // TODO hide find experiments (this is that item)
      // TODO make refresh be an always action
      MenuItem findExperiments = menu.getItem(0);
      findExperiments.setVisible(false);
      MenuItem refreshExperiments = menu.getItem(1);
      refreshExperiments.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
      return true;
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_refreshMenuItem) {
      if (!NetworkUtil.isConnected(this)) {
        showDialogById(NetworkUtil.NO_NETWORK_CONNECTION);
      } else {
        refreshList();
      }
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
    } else if (id == R.id.action_email_paco_team) {
      launchEmailPacoTeam();
      return true;
    }  else if (id == R.id.action_troubleshooting) {
      launchTroubleshooting();
      return true;
    } else if (id == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void launchTroubleshooting() {
    startActivity(new Intent(this, TroubleshootingActivity.class));
  }



  private void launchFindMyExperiments() {
    startActivity(new Intent(this, FindMyExperimentsActivity.class));
  }

  private void launchFindPublicExperiments() {
    startActivity(new Intent(this, FindExperimentsActivity.class));
  }

  private void launchCompletedExperiments() {
    // startActivity(new Intent(this, CompletedExperimentsActivity.class));
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
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.email_subject_paco_feedback));
    emailIntent.setType("plain/text");
    startActivity(emailIntent);
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.debug("FindMyExperimentsActivity onResume");
    dialogable = true;
    if (userPrefs.getAccessToken() == null) {
      Intent acctChooser = new Intent(this, SplashActivity.class);
      this.startActivity(acctChooser);
    } else {
      if (userPrefs.isMyExperimentsListStale()) {
        refreshList();
      }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == JOIN_REQUEST_CODE) {
      if (resultCode == JOINED_EXPERIMENT) {
        setResult(resultCode);
        finish();
      }
    }
  }

  private TextView createListHeader() {
    TextView listHeader = (TextView) findViewById(R.id.ExperimentListTitle);
    String header = null;
    header = getString(R.string.find_my_experiments);
    listHeader.setText(header);
    listHeader.setTextSize(25);
    return listHeader;
  }

  // private TextView createRefreshHeader() {
  // TextView listHeader = (TextView)findViewById(R.id.ExperimentRefreshTitle);
  // DateTime lastRefresh = userPrefs.getAvailableExperimentListRefreshTime();
  // if (lastRefresh == null) {
  // listHeader.setVisibility(View.GONE);
  // } else {
  // String lastRefreshTime =
  // TimeUtil.dateTimeNoZoneFormatter.print(lastRefresh);
  // String header = getString(R.string.last_refreshed) + ": " +
  // lastRefreshTime;
  // listHeader.setText(header);
  // listHeader.setTextSize(15);
  // }
  // return listHeader;
  // }

  private void saveRefreshTime() {
    userPrefs.setMyExperimentListRefreshTime(new Date().getTime());
    // TextView listHeader =
    // (TextView)findViewById(R.id.ExperimentRefreshTitle);
    // DateTime lastRefresh = userPrefs.getMyExperimentListRefreshTime();
    // String header = getString(R.string.last_refreshed) + ": " +
    // TimeUtil.dateTimeNoZoneFormatter.print(lastRefresh);
    // listHeader.setText(header);
  }

  public void showNetworkConnectionActivity() {
    try {
      startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), NetworkUtil.ENABLED_NETWORK);
    } catch (Exception e) {

    }
  }

  protected void refreshList() {
    progressBar.setVisibility(View.VISIBLE);

    // TODO replace this by binding to MyExperimentsFetchService and having it
    // callback when it is done
    final String myExperimentsUrl = ExperimentUrlBuilder.buildUrlForMyExperiments(userPrefs,
                                                                                  experimentCursor,
                                                                                  FindExperimentsActivity.DOWNLOAD_LIMIT);
    new PacoForegroundService(this, myExperimentsUrl).execute();
  }

  private void dismissAnyDialog() {
    if (newFragment != null) {
      newFragment = null;
      FragmentManager ft = getSupportFragmentManager();
      DialogFragment prev = (DialogFragment) getSupportFragmentManager().findFragmentByTag("dialog");
      if (prev != null && prev.isResumed()) {
        prev.dismissAllowingStateLoss();
      }
    }
  }

  // Visible for testing
  public void updateDownloadedExperiments(String contentAsString) {
    String oldCursor = experimentCursor;

    try {
      Map<String, Object> results = ExperimentProviderUtil.fromDownloadedEntitiesJson(contentAsString);
      String newExperimentCursor = (String) results.get("cursor");
      List<Experiment> newExperiments = (List<Experiment>) results.get("results");

      if (experimentCursor == null) { // we have either not loaded before or are
                                      // starting over
        experiments = newExperiments;
        Collections.sort(experiments, new Comparator<Experiment>() {

          @Override
          public int compare(Experiment lhs, Experiment rhs) {
            return lhs.getExperimentDAO().getTitle().toLowerCase()
                      .compareTo(rhs.getExperimentDAO().getTitle().toLowerCase());
          }

        });
        experimentCursor = newExperimentCursor;
        saveExperimentsToDisk();
      } else {
        for (Experiment experiment : newExperiments) {
          if (!experiments.contains(experiment)) {
            experiments.add(experiment); // we are mid-pagination so just add
            // the new batch to the existing.
          }
        }

        Collections.sort(experiments, new Comparator<Experiment>() {

          @Override
          public int compare(Experiment lhs, Experiment rhs) {
            return lhs.getExperimentDAO().getTitle().toLowerCase()
                      .compareTo(rhs.getExperimentDAO().getTitle().toLowerCase());
          }

        });
        experimentCursor = newExperimentCursor;
        saveExperimentsToDisk();
      }
      if (newExperiments.size() < FindExperimentsActivity.DOWNLOAD_LIMIT || newExperimentCursor == null
          || oldCursor == newExperimentCursor) {
        experimentCursor = null; // we have hit the end. The next refresh starts
                                 // over
        Toast.makeText(this, R.string.no_more_experiments_on_server, Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(this, R.string.load_more_experiments_from_server, Toast.LENGTH_LONG).show();
      }

      reloadAdapter();
    } catch (JsonParseException e) {
      showFailureDialog(NetworkUtil.CONTENT_ERROR);
    } catch (JsonMappingException e) {
      showFailureDialog(NetworkUtil.CONTENT_ERROR);
    } catch (UnsupportedCharsetException e) {
      showFailureDialog(NetworkUtil.CONTENT_ERROR);
    } catch (IOException e) {
      showFailureDialog(NetworkUtil.CONTENT_ERROR);
    }

  }

  private void saveExperimentsToDisk() {
    Log.debug("FindMyExperimentsActivity saveExperimentsToDisk");
    try {
      String contentAsString = ExperimentProviderUtil.getJson(experiments);
      experimentProviderUtil.saveMyExperimentsToDisk(contentAsString);
    } catch (JsonParseException e) {
      showFailureDialog(NetworkUtil.CONTENT_ERROR);
    } catch (JsonMappingException e) {
      showFailureDialog(NetworkUtil.CONTENT_ERROR);
    } catch (UnsupportedCharsetException e) {
      showFailureDialog(NetworkUtil.CONTENT_ERROR);
    } catch (IOException e) {
      showFailureDialog(NetworkUtil.CONTENT_ERROR);
    }
  }

  // Visible for testing
  public void reloadAdapter() {
    Log.debug("FindMyExperimentsActivity reloadAdapter");
    if (experiments == null || experiments.isEmpty()) {
      experiments = experimentProviderUtil.loadMyExperimentsFromDisk();
    }
    adapter = new AvailableExperimentsListAdapter(FindMyExperimentsActivity.this, R.id.find_experiments_list,
                                                  experiments);
    list.setAdapter(adapter);
  }

  protected void showDialogById(int id) {
    // DialogFragment.show() will take care of adding the fragment
    // in a transaction. We also want to remove any currently showing
    // dialog, so make our own transaction and take care of that here.
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
    if (prev != null) {
      ft.remove(prev);
    }
    ft.addToBackStack(null);

    // Create and show the dialog.
    newFragment = ProgressDialogFragment.newInstance(id);
    newFragment.show(ft, "dialog");
    // ft.commit();
  }

  private void showFailureDialog(String status) {
    if (dialogable) {
      if (status.equals(NetworkUtil.CONTENT_ERROR) || status.equals(NetworkUtil.RETRIEVAL_ERROR)) {
        showDialogById(NetworkUtil.INVALID_DATA_ERROR);
      } else if (status.equals(Integer.toString(NetworkUtil.UNKNOWN_HOST_ERROR))) {
        showDialogById(NetworkUtil.UNKNOWN_HOST_ERROR);
      }  else {
        showDialogById(NetworkUtil.SERVER_ERROR);
      }
    }
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
        view = mInflater.inflate(R.layout.experiments_available_list_row, null);
      }

      Experiment experiment = getItem(position);

      if (experiment != null) {
        TextView title = (TextView) view.findViewById(R.id.experimentListRowTitle);
        TextView creator = (TextView) view.findViewById(R.id.experimentListRowCreator);

        if (title != null) {
          title.setText(experiment.getExperimentDAO().getTitle());
          title.setOnClickListener(myButtonListener);
        }

        if (creator != null) {
          StringBuilder buf = new StringBuilder();
          String organizationEmail = experiment.getExperimentDAO().getOrganization();
          if (!Strings.isNullOrEmpty(organizationEmail) && !"null".equals(organizationEmail)) {
            buf.append(organizationEmail);
            buf.append(", ");
          }
          String contactEmail = experiment.getExperimentDAO().getContactEmail();
          if (Strings.isNullOrEmpty(contactEmail) || "null".equals(contactEmail)) {
            contactEmail = experiment.getExperimentDAO().getCreator();
          }
          buf.append(contactEmail);

          creator.setText(buf.toString());
          creator.setOnClickListener(myButtonListener);
        }
        // ImageView iv = (ImageView)
        // view.findViewById(R.id.experimentIconView);
        // iv.setImageBitmap(Bitmap.create(cursor.getString(iconColumn)));
      }
      view.setOnClickListener(myButtonListener);
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

          getIntent().putExtra(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, experiment.getServerId());

          String action = getIntent().getAction();
          Intent experimentIntent = new Intent(FindMyExperimentsActivity.this, ExperimentDetailActivity.class);
          experimentIntent.putExtra(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, experiment.getServerId());
          experimentIntent.putExtra(ExperimentDetailActivity.ID_FROM_MY_EXPERIMENTS_FILE, true);
          startActivityForResult(experimentIntent, JOIN_REQUEST_CODE);
        }
      }
    };

  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    // if (newFragment != null) {
    // outState.putInt("dialog_id", newFragment.getDialogTypeId());
    // }
    dismissAnyDialog();
    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
  }

  @Override
  protected void onPause() {
    Log.debug("FindMyExperimentsActivity onPause");
    dialogable = false;
    super.onPause();
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
        //Toast.makeText(FindMyExperimentsActivity.this, msg, Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.GONE);
        //showFailureDialog(getString(R.string.could_not_retrieve_experiments_try_again_));
        showFailureDialog(msg);
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
          Toast.makeText(FindMyExperimentsActivity.this, getString(R.string.experiment_list_download_complete), Toast.LENGTH_LONG).show();;
          updateDownloadedExperiments(msg);
          saveRefreshTime();
        } else {
          showFailureDialog(getString(R.string.could_not_retrieve_experiments_try_again_));
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
        Toast.makeText(FindMyExperimentsActivity.this, "Exception: " + exception.getMessage(), Toast.LENGTH_LONG);
      }
    });
  }

}

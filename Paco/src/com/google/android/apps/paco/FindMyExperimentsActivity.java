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
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.pacoapp.paco.R;
import com.pacoapp.paco.net.ExperimentUrlBuilder;
import com.pacoapp.paco.net.NetworkClient;
import com.pacoapp.paco.net.PacoForegroundService;
import com.pacoapp.paco.ui.SplashActivity;


/**
 *
 */
public class FindMyExperimentsActivity extends ActionBarActivity implements NetworkActivityLauncher, NetworkClient {

  static final int REFRESHING_EXPERIMENTS_DIALOG_ID = 1001;
  static final int JOIN_REQUEST_CODE = 1;
  static final int JOINED_EXPERIMENT = 1;

  private ExperimentProviderUtil experimentProviderUtil;
  private ListView list;
  private ProgressDialog  p;
  private ViewGroup mainLayout;
  public UserPreferences userPrefs;
  protected AvailableExperimentsListAdapter adapter;
  private List<Experiment> experiments = Lists.newArrayList();
  private ProgressDialogFragment newFragment;
  private ProgressBar progressBar;
  private String experimentCursor;
  private boolean loadedAllExperiments;
  private Button refreshButton;
  private boolean dialogable;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mainLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.find_experiments, null);
    setContentView(mainLayout);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    ActionBar actionBar = getSupportActionBar();
    actionBar.setLogo(R.drawable.ic_launcher);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));

    Intent intent = getIntent();
    if (intent.getData() == null) {
      intent.setData(ExperimentColumns.CONTENT_URI);
    }

    userPrefs = new UserPreferences(this);
    list = (ListView) findViewById(R.id.find_experiments_list);
//    createListHeader();
//    createRefreshHeader();

    experimentProviderUtil = new ExperimentProviderUtil(this);

    refreshButton = (Button) findViewById(R.id.RefreshExperimentsButton2);
    refreshButton.setVisibility(View.VISIBLE);

    refreshButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if (!isConnected()) {
          showDialogById(DownloadExperimentsHelper.NO_NETWORK_CONNECTION);
        } else {
          refreshList();
        }
      }
    });

    progressBar = (ProgressBar)findViewById(R.id.findExperimentsProgressBar);


    reloadAdapter();
    list.setItemsCanFocus(true);
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
    registerForContextMenu(list);
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

  private boolean isConnected() {
    return NetworkUtil.isConnected(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
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
	TextView listHeader = (TextView)findViewById(R.id.ExperimentListTitle);
    String header = null;
    header = getString(R.string.find_my_experiments_list_title);
    listHeader.setText(header);
    listHeader.setTextSize(25);
    return listHeader;
  }

//  private TextView createRefreshHeader() {
//    TextView listHeader = (TextView)findViewById(R.id.ExperimentRefreshTitle);
//    DateTime lastRefresh = userPrefs.getAvailableExperimentListRefreshTime();
//    if (lastRefresh == null) {
//      listHeader.setVisibility(View.GONE);
//    } else {
//      String lastRefreshTime = TimeUtil.dateTimeNoZoneFormatter.print(lastRefresh);
//      String header = getString(R.string.last_refreshed) + ": " + lastRefreshTime;
//      listHeader.setText(header);
//      listHeader.setTextSize(15);
//    }
//    return listHeader;
//  }

  private void saveRefreshTime() {
    userPrefs.setMyExperimentListRefreshTime(new Date().getTime());
//    TextView listHeader = (TextView)findViewById(R.id.ExperimentRefreshTitle);
//    DateTime lastRefresh = userPrefs.getMyExperimentListRefreshTime();
//    String header = getString(R.string.last_refreshed) + ": " + TimeUtil.dateTimeNoZoneFormatter.print(lastRefresh);
//    listHeader.setText(header);
  }

  public void showNetworkConnectionActivity() {
    try {
      startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), DownloadExperimentsHelper.ENABLED_NETWORK);
    } catch (Exception e) {

    }
  }


  protected void refreshList() {
    progressBar.setVisibility(View.VISIBLE);
    final String myExperimentsUrl = ExperimentUrlBuilder.buildUrlForMyExperiments(userPrefs, experimentCursor,
                                                                                  FindExperimentsActivity.DOWNLOAD_LIMIT);
    new PacoForegroundService(this, myExperimentsUrl).execute();
  }

  private void dismissAnyDialog() {
    if (newFragment != null) {
      newFragment = null;
      FragmentManager ft = getSupportFragmentManager();
      DialogFragment prev = (DialogFragment)getSupportFragmentManager().findFragmentByTag("dialog");
      if (prev != null && prev.isResumed()) {
        prev.dismissAllowingStateLoss();
      }
    }
  }





  // Visible for testing
  public void updateDownloadedExperiments(String contentAsString) {
    try {
      Map<String, Object> results = ExperimentProviderUtil.fromDownloadedEntitiesJson(contentAsString);
      String newExperimentCursor = (String) results.get("cursor");
      List<Experiment> newExperiments = (List<Experiment>) results.get("results");

      if (experimentCursor == null) { // we have either not loaded before or are starting over
        experiments = newExperiments;
        Collections.sort(experiments, new Comparator<Experiment>() {

          @Override
          public int compare(Experiment lhs, Experiment rhs) {
            return lhs.getExperimentDAO().getTitle().toLowerCase().compareTo(rhs.getExperimentDAO().getTitle().toLowerCase());
          }

        });
        experimentCursor = newExperimentCursor;
        saveExperimentsToDisk();
      } else {
        experiments.addAll(newExperiments); // we are mid-pagination so just add the new batch to the existing.
        Collections.sort(experiments, new Comparator<Experiment>() {

          @Override
          public int compare(Experiment lhs, Experiment rhs) {
            return lhs.getExperimentDAO().getTitle().toLowerCase().compareTo(rhs.getExperimentDAO().getTitle().toLowerCase());
          }

        });
        experimentCursor = newExperimentCursor;
        saveExperimentsToDisk();
      }
      if (newExperiments.size() == 0 || newExperimentCursor == null) {
        experimentCursor = null; // we have hit the end. The next refresh starts over
        refreshButton.setText(getString(R.string.refresh_experiments_list_from_server));
        Toast.makeText(this, R.string.no_more_experiments_on_server, Toast.LENGTH_LONG).show();
      } else {
        refreshButton.setText(getString(R.string.load_more_experiments_from_server));
      }

      reloadAdapter();
    } catch (JsonParseException e) {
      showFailureDialog(DownloadExperimentsHelper.CONTENT_ERROR);
    } catch (JsonMappingException e) {
      showFailureDialog(DownloadExperimentsHelper.CONTENT_ERROR);
    } catch (UnsupportedCharsetException e) {
      showFailureDialog(DownloadExperimentsHelper.CONTENT_ERROR);
    } catch (IOException e) {
      showFailureDialog(DownloadExperimentsHelper.CONTENT_ERROR);
    }

  }

  private void saveExperimentsToDisk() {
    try {
      String contentAsString = ExperimentProviderUtil.getJson(experiments);
      experimentProviderUtil.saveMyExperimentsToDisk(contentAsString);
    } catch (JsonParseException e) {
      showFailureDialog(DownloadExperimentsHelper.CONTENT_ERROR);
    } catch (JsonMappingException e) {
      showFailureDialog(DownloadExperimentsHelper.CONTENT_ERROR);
    } catch (UnsupportedCharsetException e) {
      showFailureDialog(DownloadExperimentsHelper.CONTENT_ERROR);
    } catch (IOException e) {
      showFailureDialog(DownloadExperimentsHelper.CONTENT_ERROR);
    }
  }

  // Visible for testing
  public void reloadAdapter() {
    if (experiments == null || experiments.isEmpty()) {
      experiments = experimentProviderUtil.loadMyExperimentsFromDisk();
    }
    adapter = new AvailableExperimentsListAdapter(FindMyExperimentsActivity.this,
                                                  R.id.find_experiments_list,
                                                  experiments);
    list.setAdapter(adapter);
  }

  protected void showDialogById(int id) {
    // DialogFragment.show() will take care of adding the fragment
    // in a transaction.  We also want to remove any currently showing
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
//    ft.commit();
  }



  private void showFailureDialog(String status) {
    if (dialogable) {
      if (status.equals(DownloadExperimentsHelper.CONTENT_ERROR) ||
          status.equals(DownloadExperimentsHelper.RETRIEVAL_ERROR)) {
        showDialogById(DownloadExperimentsHelper.INVALID_DATA_ERROR);
      } else {
        showDialogById(DownloadExperimentsHelper.SERVER_ERROR);
      }
    }
  }


  private class AvailableExperimentsListAdapter extends ArrayAdapter<Experiment> {

    private LayoutInflater mInflater;

    AvailableExperimentsListAdapter(Context context, int resourceId,
                                   List<Experiment> experiments) {
      super(context, resourceId, experiments);
      mInflater = LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup parent){
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
        }

        if (creator != null){
            creator.setText(experiment.getExperimentDAO().getCreator());
        } else {
            creator.setText(getContext().getString(R.string.unknown_author_text));
        }
//        ImageView iv = (ImageView) view.findViewById(R.id.experimentIconView);
//        iv.setImageBitmap(Bitmap.create(cursor.getString(iconColumn)));
      }
      return view;
    }

  }


  @Override
  protected void onSaveInstanceState(Bundle outState) {
//    if (newFragment != null) {
//      outState.putInt("dialog_id", newFragment.getDialogTypeId());
//    }
    dismissAnyDialog();
    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
  }

  @Override
  protected void onPause() {
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
        Toast.makeText(FindMyExperimentsActivity.this, msg, Toast.LENGTH_LONG);
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
          Toast.makeText(FindMyExperimentsActivity.this, "Download complete", Toast.LENGTH_LONG);
          updateDownloadedExperiments(msg);
          saveRefreshTime();
        } else {
          showFailureDialog("No experiment data retrieved. Try again.");
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

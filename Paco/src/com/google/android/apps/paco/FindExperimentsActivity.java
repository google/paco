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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import com.pacoapp.paco.R;

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
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


/**
 *
 */
public class FindExperimentsActivity extends Activity {

  private static final int REFRESHING_EXPERIMENTS_DIALOG_ID = 1001;
  static final int JOIN_REQUEST_CODE = 1;
  static final int JOINED_EXPERIMENT = 1;
  
  private ExperimentProviderUtil experimentProviderUtil;
  private ListView list;
  private ProgressDialog  p;
  private ViewGroup mainLayout;
  public UserPreferences userPrefs;
  protected AvailableExperimentsListAdapter adapter;
  private List<Experiment> experiments;

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
    list.setItemsCanFocus(true);
    list.setOnItemClickListener(new OnItemClickListener() {

      public void onItemClick(AdapterView<?> listview, View textview, int position, long id) {
        Experiment experiment = experiments.get(position);
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), experiment.getServerId());

        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
          // The caller is waiting for us to return an experiment selected by
          // the user. The have clicked on one, so return it now.
          setResult(RESULT_OK, new Intent().setData(uri));
        } else {
          Intent experimentIntent = new Intent(FindExperimentsActivity.this, ExperimentDetailActivity.class);
          experimentIntent.setData(uri);
          startActivityForResult(experimentIntent, JOIN_REQUEST_CODE);
        }
      }
    });
    registerForContextMenu(list);
  }
  
  private boolean isConnected() {
    return NetworkUtil.isConnected(this);
  }
  
  @Override
  protected void onResume() {
    super.onResume();
    if (userPrefs.getSelectedAccount() == null) {
      Intent acctChooser = new Intent(this, AccountChooser.class);
      this.startActivity(acctChooser);
    } else {
      if (userPrefs.isAvailableExperimentsListStale()) {
        refreshList();
      }
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
    String header = null;
    header = getString(R.string.available_experiments_title);
    listHeader.setText(header);
    listHeader.setTextSize(25);
    return listHeader;
  }
  
  private TextView createRefreshHeader() {
    TextView listHeader = (TextView)findViewById(R.id.ExperimentRefreshTitle);
    DateTime lastRefresh = userPrefs.getExperimentListRefreshTime(UserPreferences.FIND_EXPERIMENTS);
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
  
  private void refreshRefreshHeader() {
    TextView listHeader = (TextView)findViewById(R.id.ExperimentRefreshTitle);
    DateTime lastRefresh = userPrefs.getExperimentListRefreshTime(UserPreferences.FIND_EXPERIMENTS);
    String header = getString(R.string.last_refreshed) + ": " + TimeUtil.formatDateTime(lastRefresh);
    listHeader.setText(header); 
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
                               getString(R.string.checking_server_for_new_and_updated_experiment_definitions), 
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
  

//  @Override
//  protected Dialog onCreateDialog(int id) {
//    return super.onCreateDialog(id);
//  }
  
  
  protected void refreshList() {    
    DownloadShortExperimentsTaskListener listener = new DownloadShortExperimentsTaskListener() {
      
      @Override
      public void done(String resultCode) {
        reloadAdapter();
        refreshRefreshHeader();
        dismissDialog(REFRESHING_EXPERIMENTS_DIALOG_ID);
        if (resultCode != DownloadHelper.SUCCESS) {
          showFailureDialog(resultCode);
        }
      }
    };
    showDialog(REFRESHING_EXPERIMENTS_DIALOG_ID);
    new DownloadShortExperimentsTask(this, listener, userPrefs, experimentProviderUtil).execute();
  }

  private void reloadAdapter() {
    experiments = experimentProviderUtil.loadExperimentsFromDisk();
    adapter = new AvailableExperimentsListAdapter(FindExperimentsActivity.this, 
                                                  R.id.find_experiments_list, 
                                                  experiments);
    list.setAdapter(adapter);
  }
  
  private void showFailureDialog(String status) {
    if (status.equals(DownloadHelper.CONTENT_ERROR) ||
        status.equals(DownloadHelper.RETRIEVAL_ERROR)) {
      showDialog(DownloadHelper.INVALID_DATA_ERROR, null);
    } else {
      showDialog(DownloadHelper.SERVER_ERROR, null);
    }      
  }
  
  private class AvailableExperimentsListAdapter extends ArrayAdapter<Experiment> {

    private LayoutInflater mInflater;

    AvailableExperimentsListAdapter(Context context, int resourceId, 
                                   List<Experiment> experiments) {
        super(context, resourceId, experiments);
        mInflater = LayoutInflater.from(context);
      }

//    @Override
//    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//      View v = mInflater.inflate(R.layout.experiments_available_list_row, parent, false);
//      return v;
//    }

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
            title.setText(experiment.getTitle());
        }

        if (creator != null){
            creator.setText(experiment.getCreator());
        } else {
            creator.setText(getContext().getString(R.string.unknown_author_text));
        }
//        ImageView iv = (ImageView) view.findViewById(R.id.experimentIconView);
//        iv.setImageBitmap(Bitmap.create(cursor.getString(iconColumn)));
      }
      return view;
    }

  }

}

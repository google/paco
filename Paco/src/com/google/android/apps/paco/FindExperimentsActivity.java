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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


/**
 *
 */
public class FindExperimentsActivity extends Activity {

  private static final int DATA_EXPERIMENT_OPTION = 3;
  private static final int STOP_EXPERIMENT_OPTION = 2;
  private static final int EDIT_EXPERIMENT_OPTION = 1;
  static final int JOIN_REQUEST_CODE = 1;
  static final int JOINED_EXPERIMENT = 1;
  
  private boolean showingJoinedExperiments;
  private Cursor cursor;
  private ExperimentProviderUtil experimentProviderUtil;
  private ListView list;
  private ProgressDialog  p;
  private ViewGroup mainLayout;
  public UserPreferences userPrefs;
  private SimpleCursorAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mainLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.find_experiments, null);
    setContentView(mainLayout);
    Intent intent = getIntent();
    if (intent.getData() == null) {
      intent.setData(ExperimentColumns.CONTENT_URI);
    }
    showingJoinedExperiments = intent.getData().equals(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI);

    userPrefs = new UserPreferences(this);

    list = (ListView) findViewById(R.id.find_experiments_list);
    createListHeader();

    Button listFooter = (Button) findViewById(R.id.RefreshExperimentsButton2);
    listFooter.setVisibility(View.VISIBLE);
    if (!showingJoinedExperiments) {
      listFooter.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          refreshList();
        }
      });
    } else {
      listFooter.setVisibility(View.GONE);
    }

    experimentProviderUtil = new ExperimentProviderUtil(this);

    String selectionArgs = null;
    if (!showingJoinedExperiments) {
      selectionArgs = ExperimentColumns.JOIN_DATE + " IS NULL";
    }
    cursor = managedQuery(getIntent().getData(), new String[] { ExperimentColumns._ID, ExperimentColumns.TITLE },
        selectionArgs, null, null);

    adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor,
        new String[] { ExperimentColumns.TITLE }, new int[] { android.R.id.text1 }) {};

    list.setAdapter(adapter);
    // list.setItemsCanFocus(true);
    list.setOnItemClickListener(new OnItemClickListener() {

      public void onItemClick(AdapterView<?> listview, View textview, int position, long id) {
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
          // The caller is waiting for us to return an experiment selected by
          // the user. The have clicked on one, so return it now.
          setResult(RESULT_OK, new Intent().setData(uri));
        } else {
          // Launch activity to view/edit or run the currently selected
          // experiment
          if (showingJoinedExperiments) {
            // if (position == 0) {
            Intent experimentIntent = new Intent(FindExperimentsActivity.this, ExperimentExecutor.class);
            experimentIntent.setData(uri);
            startActivity(experimentIntent);
            finish();
          } else {
            Intent experimentIntent = new Intent(FindExperimentsActivity.this, ExperimentDetailActivity.class);
            experimentIntent.setData(uri);
            startActivityForResult(experimentIntent, JOIN_REQUEST_CODE);
          }
        }
      }
    });
    registerForContextMenu(list);

    if (!showingJoinedExperiments && listIsStale()) {
      refreshList();
    }

  }
  
  private boolean listIsStale() {
    return userPrefs.isExperimentListStale();
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
    Intent experimentIntent = new Intent(FindExperimentsActivity.this, FeedbackActivity.class);
    experimentIntent.setData(Uri.withAppendedPath(getIntent().getData(), Long.toString(id)));
    startActivity(experimentIntent);
  }

  private void deleteExperiment(long id) {
    NotificationCreator nc = NotificationCreator.create(this);
    nc.timeoutNotificationsForExperiment(id);
    experimentProviderUtil.deleteFullExperiment(Uri.withAppendedPath(getIntent().getData(), Long.toString(id)));
    new AlarmStore(this).deleteAllSignalsForSurvey(id);
    cursor.requery();
    startService(new Intent(FindExperimentsActivity.this, BeeperService.class));  
  }
 
  private void editExperiment(long id) {
    Intent experimentIntent = new Intent(FindExperimentsActivity.this, ExperimentScheduleActivity.class);
    experimentIntent.setData(Uri.withAppendedPath(getIntent().getData(), Long.toString(id)));
    startActivity(experimentIntent);
  }
  
  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    if (v.equals(list) && showingJoinedExperiments) {
//      AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
//      int position = info.position;
//      SignalSchedule schedule = ((Experiment)(adapter.getItem(position))).getSchedule();
//      if (schedule.getUserEditable() != null &&
//          schedule.getUserEditable()) {
        menu.add(0, EDIT_EXPERIMENT_OPTION, 0, "Edit Schedule");
//      }
      menu.add(0, STOP_EXPERIMENT_OPTION, 0, "Stop Experiment");
      menu.add(0, DATA_EXPERIMENT_OPTION, 0, "Explore Data");
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
    if (showingJoinedExperiments) {
      header = "Running Experiments";
    } else {
      header = "Available Experiments";
    }
    listHeader.setText(header);
    listHeader.setTextSize(25);
    return listHeader;
  }

  protected void refreshList() {    
    new DownloadExperimentsTask(this, cursor, userPrefs, experimentProviderUtil, null).execute();
  }

}

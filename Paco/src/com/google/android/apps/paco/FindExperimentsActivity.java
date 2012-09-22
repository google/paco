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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
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
  private BaseAdapter adapter;

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
    ensureGoogleAccountChosen();
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
    cursor = managedQuery(getIntent().getData(), new String[] { ExperimentColumns._ID, ExperimentColumns.TITLE, ExperimentColumns.CREATOR, ExperimentColumns.ICON },
        selectionArgs, null, ExperimentColumns.TITLE + " COLLATE NOCASE ASC");
    if (showingJoinedExperiments) {
      adapter = new RunningExperimentListAdapter(this, cursor);
    } else {
      adapter = new AvailableExperimentListAdapter(this, cursor); 
          //new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor,
//                                        new String[] { ExperimentColumns.TITLE },
//                                        new int[] { android.R.id.text1 }) {};
    }
    list.setAdapter(adapter);    
    if (!showingJoinedExperiments) {
      list.setItemsCanFocus(true);
      list.setOnItemClickListener(new OnItemClickListener() {

        public void onItemClick(AdapterView<?> listview, View textview, int position, long id) {
          Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

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
    }
    registerForContextMenu(list);

  }
  
  
  
  @Override
  protected void onResume() {
    super.onResume();
    if (!showingJoinedExperiments && listIsStale()) {
      refreshList();
    }

  }



  private void ensureGoogleAccountChosen() {
    if (userPrefs.getSelectedAccount(this) == null) {
      Intent acctChooser = new Intent(this, AccountChooser.class);
      this.startActivity(acctChooser);
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
    DownloadExperimentsTaskListener listener = new DownloadExperimentsTaskListener() {
      
      @Override
      public void done() {
        cursor.requery();
      }
    };
    new DownloadExperimentsTask(this, listener, userPrefs, experimentProviderUtil, null).execute();
  }

  private class RunningExperimentListAdapter extends CursorAdapter {

    private LayoutInflater mInflater;
    private int titleColumn;
    private int idColumn;

    RunningExperimentListAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mInflater = LayoutInflater.from(context);
        titleColumn = cursor.getColumnIndex( ExperimentColumns.TITLE);
        idColumn = cursor.getColumnIndex(ExperimentColumns._ID);
      }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      View v = mInflater.inflate(R.layout.experiment_list_row, parent, false);
      return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
      String id = cursor.getString(idColumn);
      
      TextView tv = (TextView) view.findViewById(R.id.experimentListRowTitle);
      tv.setText(cursor.getString(titleColumn));
      tv.setOnClickListener(myButtonListener);      
      
      tv.setTag(id);
      
      ImageButton editButton = (ImageButton)view.findViewById(R.id.editExperimentButton);
      editButton.setOnClickListener(myButtonListener);
      editButton.setTag(id); 

      ImageButton quitButton = (ImageButton)view.findViewById(R.id.quitExperimentButton);
      quitButton.setOnClickListener(myButtonListener);
      quitButton.setTag(id);
      
      ImageButton exploreButton = (ImageButton)view.findViewById(R.id.exploreDataExperimentButton);
      exploreButton.setOnClickListener(myButtonListener);
      exploreButton.setTag(id);
      // show icon
      // ImageView iv = (ImageView) view.findViewById(R.id.explore_data_icon);
      // iv.setImageResource();

    }
    
    private OnClickListener myButtonListener = new OnClickListener() {
      @Override
      public void onClick(final View v) {
        final int position = list.getPositionForView(v);
        if (position == ListView.INVALID_POSITION) {
          return;          
        } else if (v.getId() == R.id.editExperimentButton) {
          editExperiment(Long.parseLong((String) v.getTag()));
        } else if (v.getId() == R.id.exploreDataExperimentButton) {
          showDataForExperiment(Long.parseLong((String) v.getTag()));
        } else if (v.getId() == R.id.quitExperimentButton) {
          new AlertDialog.Builder(FindExperimentsActivity.this)
          .setCancelable(true)
          .setTitle("Stop the Experiment?")
          .setMessage("Are you sure you want to stop the experiment?")
          .setPositiveButton("Yes", new Dialog.OnClickListener() {           
            @Override
            public void onClick(DialogInterface dialog, int which) {
              deleteExperiment(Long.parseLong((String) v.getTag()));                  
            }
          })
          .setNegativeButton("No", new Dialog.OnClickListener() {           
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }
          }).create().show();
          
        } else if (v.getId() == R.id.experimentListRowTitle) {
          Intent experimentIntent = new Intent(FindExperimentsActivity.this, ExperimentExecutor.class);
          Uri uri = ContentUris.withAppendedId(getIntent().getData(), Long.parseLong((String)v.getTag()));
          experimentIntent.setData(uri);
          startActivity(experimentIntent);
          finish();
        }
      }
    };

  }
    
  private class AvailableExperimentListAdapter extends CursorAdapter {

    private LayoutInflater mInflater;
    private int titleColumn;
    private int idColumn;
    private int creatorColumn;
    private int iconColumn;

    AvailableExperimentListAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mInflater = LayoutInflater.from(context);
        titleColumn = cursor.getColumnIndex( ExperimentColumns.TITLE);
        creatorColumn = cursor.getColumnIndex(ExperimentColumns.CREATOR);
        idColumn = cursor.getColumnIndex(ExperimentColumns._ID);
        //iconColumn = cursor.getColumnIndex(ExperimentColumns.ICON);
      }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      View v = mInflater.inflate(R.layout.experiments_available_list_row, parent, false);
      return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
      String id = cursor.getString(idColumn);
      
      TextView tv = (TextView) view.findViewById(R.id.experimentListRowTitle);
      tv.setText(cursor.getString(titleColumn));
      
      String creatorText = null;
      if (creatorColumn != -1) {
        creatorText = cursor.getString(creatorColumn);
      } else {
        creatorText = "unknown author";
      }
      TextView tv2 = (TextView) view.findViewById(R.id.experimentListRowCreator);
      tv2.setText(creatorText);
      
//       ImageView iv = (ImageView) view.findViewById(R.id.experimentIconView);
//       iv.setImageBitmap(Bitmap.create(cursor.getString(iconColumn)));

    }    
  }

}

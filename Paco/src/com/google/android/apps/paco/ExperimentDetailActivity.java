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

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.corp.productivity.specialprojects.android.comm.Response;
import com.google.corp.productivity.specialprojects.android.comm.UrlContentManager;

public class ExperimentDetailActivity extends Activity {

  static final String EXPERIMENT_NAME = "com.google.android.apps.paco.Experiment";
  static DateTimeFormatter df = DateTimeFormat.shortDate();
  private Uri uri;
  private Button joinButton;
  private Experiment experiment;
  private ExperimentProviderUtil experimentProviderUtil;
  private UserPreferences userPrefs;
  private ProgressDialog p;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.experiment_detail);
    final Intent intent = getIntent();
    uri = intent.getData();
    userPrefs = new UserPreferences(this);
    
    experimentProviderUtil = new ExperimentProviderUtil(this);
    if (uri.getLastPathSegment().startsWith("0000")) {
      String realServerId = uri.getLastPathSegment().substring(4);
      List<Experiment> experiments = experimentProviderUtil.getExperimentsByServerId(new Long(realServerId));
      if (experiments != null && experiments.size() > 0) {
        experiment = experiments.get(0);
        uri= Uri.withAppendedPath(ExperimentColumns.CONTENT_URI, Long.toString(experiment.getId()));
      }
    } else {
      experiment = experimentProviderUtil.getExperiment(uri);  
    }
    
    if (experiment == null) {
      new AlertDialog.Builder(this).setMessage(R.string.selected_experiment_not_on_phone_refresh).setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
        
        public void onClick(DialogInterface dialog, int which) {
//          ExperimentDetailActivity.this.finish();
          refreshList();
          
        }
      }).show();
      
    } else {
      showExperiment();
    }
  }


  private void showExperiment() {
    joinButton = (Button)findViewById(R.id.JoinExperimentButton);
    if (isJoinedExperiment()) {
      joinButton.setVisibility(View.GONE);
    }

    ((TextView)findViewById(R.id.experiment_name)).setText(experiment.getTitle());
    ((TextView)findViewById(R.id.description)).setText(experiment.getDescription());
    ((TextView)findViewById(R.id.creator)).setText(experiment.getCreator());
    //    Schedule scheduleDetails = experiment.getScheduleDetails();
    ((TextView)findViewById(R.id.schedule)).setText(SignalSchedule.SCHEDULE_TYPES_NAMES[experiment.getSchedule().getScheduleType()]);
    String startDate = getString(R.string.ongoing_duration);
    String endDate = getString(R.string.ongoing_duration);
    if (experiment.isFixedDuration()) {
      startDate = df.print(experiment.getStartDate());
      endDate = df.print(experiment.getEndDate());
      ((TextView)findViewById(R.id.startDate)).setText(startDate);
      ((TextView)findViewById(R.id.endDate)).setText(endDate);
    } else {
      findViewById(R.id.startDatePanel).setVisibility(View.GONE);
      findViewById(R.id.endDatePanel).setVisibility(View.GONE);
    }
    ((TextView)findViewById(R.id.startDate)).setText(startDate);
    ((TextView)findViewById(R.id.endDate)).setText(endDate);

    String esm_frequency = experiment.getSchedule().getEsmFrequency() != null 
      ? experiment.getSchedule().getEsmFrequency().toString() 
      : null;
    if (experiment.getSchedule().getScheduleType() == SignalSchedule.ESM && esm_frequency != null && esm_frequency.length() > 0) {
      findViewById(R.id.esmPanel).setVisibility(View.VISIBLE); 
      ((TextView)findViewById(R.id.esm_frequency)).setText(esm_frequency+ "/" + SignalSchedule.ESM_PERIODS_NAMES[experiment.getSchedule().getEsmPeriodInDays()]);
    }
    // TODO (bobevans): Update to show all the new shceduling types in a succinct readonly way
    if (isJoinedExperiment()) {
      findViewById(R.id.timePanel).setVisibility(View.VISIBLE); 
      ((TextView)findViewById(R.id.time)).setText(toCommaSeparatedString(experiment.getSchedule().getTimes()));
    }
    if (!isJoinedExperiment()) {
      joinButton.setOnClickListener(new OnClickListener() {    	 
        public void onClick(View v) {
          Intent intent = new Intent(ExperimentDetailActivity.this, InformedConsentActivity.class);
          intent.setAction(Intent.ACTION_EDIT);
          intent.setData(uri);
          startActivityForResult(intent, FindExperimentsActivity.JOIN_REQUEST_CODE);
        }
      });
    }
  }

  
  private String toCommaSeparatedString(List<Long> times) {
    DateTimeFormatter df2 = org.joda.time.format.DateTimeFormat.shortTime();
    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for (Long long1 : times) {
      if (!first) {
        buf.append(",");
      }
      buf.append(new DateTime(long1).toString(df2));
    }
    String bufAsString = buf.toString();
    return bufAsString.substring(0, Math.min(bufAsString.length(), 40));
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == FindExperimentsActivity.JOIN_REQUEST_CODE) {
      if (resultCode == FindExperimentsActivity.JOINED_EXPERIMENT) {
        setResult(resultCode);
        finish();
      }
    }
  }


  private boolean isJoinedExperiment() {
    return experiment.getJoinDate() != null;
  }

  
  protected void refreshList() {
    p = ProgressDialog.show(this, getString(R.string.experiment_refresh), getString(R.string.checking_server_for_new_and_updated_experiment_definitions), true, true);
    new DownloadExperimentsTask().execute();
  }
  
  private class DownloadExperimentsTask extends AsyncTask<Void, Void, List<Experiment>> {
    @SuppressWarnings("unchecked")
    protected List<Experiment> doInBackground(Void... params) {
//      times.add(0, System.currentTimeMillis());
      UrlContentManager manager = null;
      try {
        manager = new UrlContentManager(ExperimentDetailActivity.this);
        
        String serverAddress = userPrefs.getServerAddress();
        Response response = manager.createRequest().setUrl(
            "https://"+serverAddress+"/experiments").execute();
        String contentAsString = response.getContentAsString();
        Log.i("ExperimentDetailActivity", "data: " + contentAsString);
        if (contentAsString != null) {
          ObjectMapper mapper = new ObjectMapper();
          mapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
          try {
            List<Experiment> readValue = mapper.readValue(contentAsString,
                new TypeReference<List<Experiment>>() {
                });
            return readValue;
          } catch (JsonParseException e) {
            Log.e(PacoConstants.TAG, "Could not parse text: " + contentAsString);
            e.printStackTrace();
          } catch (JsonMappingException e) {
            Log.e(PacoConstants.TAG, "Could not map json: " + contentAsString);
            e.printStackTrace();
          } catch (UnsupportedCharsetException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        return new ArrayList<Experiment>();
      } finally {
        if (manager != null) {
          manager.cleanUp();
        }
      }
    }

    protected void onProgressUpdate() {
           
    }

    protected void onPostExecute(List<Experiment> result) {
//      System.out.println("Elapsed Time for download of experiments: " + (System.currentTimeMillis() - times.get(0).longValue()));
      experimentProviderUtil.deleteAllUnJoinedExperiments();
      experimentProviderUtil.insertOrUpdateExperiments(result);
      userPrefs.setExperimentListRefreshTime(new Date().getTime());
      p.dismiss();
      List<Experiment> experiments = experimentProviderUtil.getExperimentsByServerId(new Long(uri.getLastPathSegment().substring(4)));
      if (experiments != null && experiments.size() > 0) {
        experiment = experiments.get(0);
        uri= Uri.withAppendedPath(ExperimentColumns.CONTENT_URI, Long.toString(experiment.getId()));
        showExperiment();
      } else {
        ExperimentDetailActivity.this.finish();
      }
    }
  }

  
}


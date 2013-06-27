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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.google.corp.productivity.specialprojects.android.comm.UrlContentManager;

class DownloadFullExperimentsTask extends AsyncTask<Void, Void, String> {
  private final Activity enclosingActivity;
  private UserPreferences userPrefs;
  private ExperimentProviderUtil experimentProviderUtil;
  private DownloadFullExperimentsTaskListener listener;
  private List<Experiment> experiments;
  private Boolean isAllRunningUpdate;

  @SuppressWarnings("unchecked")
  public DownloadFullExperimentsTask(Activity activity, 
                                     DownloadFullExperimentsTaskListener listener, 
                                     UserPreferences userPrefs, 
                                     ExperimentProviderUtil experimentProviderUtil, 
                                     List<Experiment> experiments,
                                     Boolean isAllRunningUpdate) {
    enclosingActivity = activity;      
    this.listener = listener;
    this.userPrefs = userPrefs;
    this.experimentProviderUtil = experimentProviderUtil;
    this.experiments = experiments;
    this.isAllRunningUpdate = isAllRunningUpdate;

  }
  
  public DownloadFullExperimentsTask(Activity activity, 
                                     DownloadFullExperimentsTaskListener listener, 
                                     UserPreferences userPrefs, 
                                     ExperimentProviderUtil experimentProviderUtil, 
                                     Experiment experiment) {
    enclosingActivity = activity;      
    this.listener = listener;
    this.userPrefs = userPrefs;
    this.experimentProviderUtil = experimentProviderUtil;
    
    this.experiments = new ArrayList<Experiment>();
    experiments.add(experiment);
    
    this.isAllRunningUpdate = false;

  }

  protected String doInBackground(Void... params) {
    DownloadHelper downloadHelper = new DownloadHelper(enclosingActivity, experimentProviderUtil, userPrefs);
    String errorCode = downloadHelper.updateRunningExperiments(experiments, isAllRunningUpdate);
    experiments = downloadHelper.getExperiments();
    return errorCode;
  }
   
  protected void onProgressUpdate() {

  }

  protected void onPostExecute(String resultCode) {
    if (listener != null) {
      listener.done(resultCode);
    }
  }

  public List<Experiment> getExperiments() {
    return experiments; 
  }
}
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
import android.os.AsyncTask;

import com.google.corp.productivity.specialprojects.android.comm.UrlContentManager;

class DownloadShortExperimentsTask extends AsyncTask<Void, Void, String> {
  private final Activity enclosingActivity;
  private UserPreferences userPrefs;
  private DownloadShortExperimentsTaskListener listener;
  private String contentAsString;

  @SuppressWarnings("unchecked")
  public DownloadShortExperimentsTask(Activity activity, 
                                 DownloadShortExperimentsTaskListener listener, 
                                 UserPreferences userPrefs) {
    enclosingActivity = activity;      
    this.listener = listener;
    this.userPrefs = userPrefs;
  }

  protected String doInBackground(Void... params) {
    DownloadHelper downloadHelper = new DownloadHelper(enclosingActivity, userPrefs);
    String errorCode = downloadHelper.downloadAvailableExperiments();
    contentAsString = downloadHelper.getContentAsString();
    return errorCode;
  }
  
  protected void onProgressUpdate() {

  }

  protected void onPostExecute(String errorCode) {
    if (listener != null) {
      listener.done(errorCode);
    }
  }
  
  public String getContentAsString() {
    return contentAsString;
  }
}
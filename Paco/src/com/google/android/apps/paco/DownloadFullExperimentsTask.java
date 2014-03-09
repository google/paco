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

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

class DownloadFullExperimentsTask extends AsyncTask<Void, Void, String> {
  private final Context enclosingContext;
  private UserPreferences userPrefs;
  private DownloadFullExperimentsTaskListener listener;
  private List<Long> experimentIds;
  private String contentAsString;

  @SuppressWarnings("unchecked")
  public DownloadFullExperimentsTask(Context context,
                                     DownloadFullExperimentsTaskListener listener,
                                     UserPreferences userPrefs,
                                     List<Long> experimentIds) {
    this.enclosingContext = context;
    this.listener = listener;
    this.userPrefs = userPrefs;
    this.experimentIds = experimentIds;
  }

  protected String doInBackground(Void... params) {
    DownloadHelper downloadHelper = new DownloadHelper(enclosingContext, userPrefs, null, null);
    String errorCode = downloadHelper.downloadRunningExperiments(experimentIds);
    contentAsString = downloadHelper.getContentAsString();
    return errorCode;
  }

  protected void onProgressUpdate() {

  }

  protected void onPostExecute(String resultCode) {
    if (listener != null) {
      listener.done(resultCode);
    }
  }

  public String getContentAsString() {
    return contentAsString;
  }
}
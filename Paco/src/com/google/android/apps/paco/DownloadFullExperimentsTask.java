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

// PRIYA - what is this class? lol.

package com.google.android.apps.paco;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.corp.productivity.specialprojects.android.comm.Response;
import com.google.corp.productivity.specialprojects.android.comm.UrlContentManager;

class DownloadFullExperimentsTask extends AsyncTask<Void, Void, String> {
    private final Activity enclosingActivity;
//    private ProgressDialog p;
    private UserPreferences userPrefs;
    private ExperimentProviderUtil experimentProviderUtil;
    private Runnable runnable;
    private DownloadExperimentsTaskListener listener;
    private Experiment experiment;

    @SuppressWarnings("unchecked")
    public DownloadFullExperimentsTask(Activity activity, 
        DownloadExperimentsTaskListener listener, 
        UserPreferences userPrefs, 
        ExperimentProviderUtil experimentProviderUtil, Runnable runnable,
        Experiment experiment) {
      enclosingActivity = activity;      
      this.listener = listener;
      this.userPrefs = userPrefs;
      this.experimentProviderUtil = experimentProviderUtil;
      this.runnable = runnable;
      this.experiment = experiment;
      
    }
    
  protected String doInBackground(Void... params) {
    UrlContentManager manager = null;
    try {
      manager = new UrlContentManager(enclosingActivity);
      String serverAddress = userPrefs.getServerAddress();
      String path = "/experiments?id=" + experiment.getServerId().toString();
      Response response = manager.createRequest().setUrl(ServerAddressBuilder.createServerUrl(serverAddress, path))
                                 .addHeader("http.useragent", "Android")
                                 .addHeader("paco.version", AndroidUtils.getAppVersion(enclosingActivity)).execute();
      String contentAsString = response.getContentAsString();
      System.out.println("content as string is: " + contentAsString);
      if (contentAsString != null) {
        try {
          experiment = ExperimentProviderUtil.getSingleExperimentFromJson(contentAsString);   // PRIYA
          //experimentProviderUtil.deleteAllUnJoinedExperiments();
          //experimentProviderUtil.updateExistingExperiments(experiments);
          //experimentProviderUtil.saveExperimentsToDisk(contentAsString);
          userPrefs.setExperimentListRefreshTime(new Date().getTime());       // PRIYA - make new prefs for this side
        } catch (JsonParseException e) {
          Log.e(PacoConstants.TAG, "Could not parse text: " + contentAsString + ", " + e.getMessage());
          return null;
        } catch (JsonMappingException e) {
          Log.e(PacoConstants.TAG, "Could not map json: " + contentAsString + ", " + e.getMessage());
          return null;
        } catch (UnsupportedCharsetException e) {
          Log.e(PacoConstants.TAG, "UnsupportedCharset. json: " + contentAsString + ", " + e.getMessage());
          return null;
        } catch (IOException e) {
          Log.e(PacoConstants.TAG, "IOException. json: " + contentAsString + ", " + e.getMessage());
          return null;
        }

      }
      return null;
    } finally {
      if (manager != null) {
        manager.cleanUp();
      }
    }
  }

    protected void onProgressUpdate() {
           
    }

    protected void onPostExecute(String unusedResult) {
//      p.dismiss();
      if (listener != null) {
        listener.done();
      }
      if (runnable != null) {
        runnable.run();
      }
      
    }
    
    // PRIYA
    public Experiment getExperiment() {
      return experiment; 
    }
}
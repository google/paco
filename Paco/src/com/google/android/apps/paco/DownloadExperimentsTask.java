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
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.corp.productivity.specialprojects.android.comm.Response;
import com.google.corp.productivity.specialprojects.android.comm.UrlContentManager;

class DownloadExperimentsTask extends AsyncTask<Void, Void, String> {
    /**
     * 
     */
    private final Activity enclosingActivity;
    private ProgressDialog p;
    private UserPreferences userPrefs;
    private ExperimentProviderUtil experimentProviderUtil;
    private Runnable runnable;
    private DownloadExperimentsTaskListener listener;

    @SuppressWarnings("unchecked")
    public DownloadExperimentsTask(Activity findExperimentsActivity, 
        DownloadExperimentsTaskListener listener, 
        UserPreferences userPrefs, 
        ExperimentProviderUtil experimentProviderUtil, Runnable runnable) {
      enclosingActivity = findExperimentsActivity;      
      this.listener = listener;
      this.userPrefs = userPrefs;
      this.experimentProviderUtil = experimentProviderUtil;
      this.runnable = runnable;
      
      p = ProgressDialog.show(enclosingActivity, "Experiment Refresh", "Checking Server for New and Updated Experiment Definitions", true, true);
    }
    
    protected String doInBackground(Void... params) {
//      times.add(0, System.currentTimeMillis());
      UrlContentManager manager = null;
      try {
        manager = new UrlContentManager(enclosingActivity);
        String serverAddress = userPrefs.getServerAddress();
        Response response = manager.createRequest().setUrl("https://"+serverAddress+"/experiments").execute();
        String contentAsString = response.getContentAsString();
//        Log.i("FindExperimentsActivity", "data: " + contentAsString);
        if (contentAsString != null) {
          ObjectMapper mapper = new ObjectMapper();
          mapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
          try {
            List<Experiment> readValue = mapper.readValue(contentAsString,
                new TypeReference<List<Experiment>>() {
                });
            experimentProviderUtil.deleteAllUnJoinedExperiments();
            experimentProviderUtil.insertOrUpdateExperiments(readValue);
            // Note, this is happening after the json processing and the (currently still used) insertOrUpdateExperiments, as I experiment with
            // no longer storing the list of downloaded experiments in the database, but rather on disk as json.            
            experimentProviderUtil.saveExperimentsToDisk(contentAsString);
            Log.i(PacoConstants.TAG, "SPEED: saving new experiments to disk t6.5 = " + System.currentTimeMillis());
            List<Experiment> experimentsReloaded = experimentProviderUtil.loadExperimentsFromDisk();
            userPrefs.setExperimentListRefreshTime(new Date().getTime());
            Log.i(PacoConstants.TAG, "SPEED: reloading new experiments to disk t6.75 = " + System.currentTimeMillis());
            experimentProviderUtil.getExperimentsByServerId(4693018);
            Log.i(PacoConstants.TAG, "SPEED: retrieving experiment by server id t6.8 = " + System.currentTimeMillis());
            return null;
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
      p.dismiss();
      if (listener != null) {
        listener.done();
      }
      if (runnable != null) {
        runnable.run();
      }
      
    }
}
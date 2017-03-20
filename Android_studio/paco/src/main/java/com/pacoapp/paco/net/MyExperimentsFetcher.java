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
package com.pacoapp.paco.net;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.net.MyExperimentsFetchService.ExperimentFetchListener;

import android.content.Context;

public class MyExperimentsFetcher {
  private static Logger Log = LoggerFactory.getLogger(MyExperimentsFetcher.class);

  private static MyExperimentsFetcher instance;

  public static synchronized MyExperimentsFetcher getInstance(Context context) {
    if (instance == null) {
      UserPreferences userPrefs = new UserPreferences(context);
      instance = new MyExperimentsFetcher(context, userPrefs);
    }
    return instance;
  }

  private Context context;
  private UserPreferences userPrefs;
  private String experimentCursor;
  private List<Experiment> experiments;
  private List<ExperimentFetchListener> listeners = Lists.newArrayList();
  private boolean isUpdating;

  // Visible for testing
  public MyExperimentsFetcher(Context context, UserPreferences userPrefs) {
    this.context = context;
    this.userPrefs = userPrefs;
  }

  public synchronized void update(ExperimentFetchListener listener) {
    listeners.add(listener);
    if (isUpdating) {
      return;
    }
    isUpdating = true;
    if (userPrefs.isMyExperimentsListStale()) {
      updateMyExperiments();
    } else {
      if (experiments != null) {
        listener.done(experiments);
      }
      isUpdating = false;
    }
  }
  private void updateMyExperiments() {

    final ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(context);
    NetworkClient networkClient = new NetworkClient.BackgroundNetworkClient(context) {
        @Override
        public void showAndFinish(String msg) {
          if (msg != null) {
            updateDownloadedExperiments(experimentProviderUtil, msg);
            userPrefs.setMyExperimentListRefreshTime(new Date().getTime());
            if (experiments != null) {
              for (ExperimentFetchListener listener : listeners) {
                listener.done(experiments);
              }
            }
            isUpdating = false;
            // notify any bound listeners that they need to check out the experiments list or that there were no new experiments?
            // Or, maybe we send them the list and the update time directly?
            // should we compute any new, unseen-before experiments here? (against a list of already-seen experiments)
          }
        }
      };
      // TODO cursor and limit - no one will have more than the default limit of personal experiments. Will they?
      // If so, then fetch until all are retrieved.

      new PacoBackgroundService(networkClient, ExperimentUrlBuilder.buildUrlForMyExperiments(userPrefs, null, null)).execute();
  }

  public void updateDownloadedExperiments(ExperimentProviderUtil experimentProviderUtil, String contentAsString) {
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
        saveExperimentsToDisk(experimentProviderUtil);
      } else {
        experiments.addAll(newExperiments); // we are mid-pagination so just add the new batch to the existing.
        Collections.sort(experiments, new Comparator<Experiment>() {

          @Override
          public int compare(Experiment lhs, Experiment rhs) {
            return lhs.getExperimentDAO().getTitle().toLowerCase().compareTo(rhs.getExperimentDAO().getTitle().toLowerCase());
          }

        });
        experimentCursor = newExperimentCursor;
        saveExperimentsToDisk(experimentProviderUtil);
      }
      if (newExperiments.size() == 0 || newExperimentCursor == null) {
        experimentCursor = null; // we have hit the end. The next refresh starts over
      }

    } catch (JsonParseException e) {
      onError(e);
    } catch (JsonMappingException e) {
      onError(e);
    } catch (UnsupportedCharsetException e) {
      onError(e);
    } catch (IOException e) {
      onError(e);
    }
  }
  private void onError(Exception e) {
    // TODO EsmSignalColumns any bound service that we were unable to refresh myexperiments
    Log.error("Could not fetch MyExperiments: " + e.getMessage(), e);
  }

  private void saveExperimentsToDisk(ExperimentProviderUtil experimentProviderUtil) {
    try {
      String contentAsString = ExperimentProviderUtil.getJson(experiments);
      experimentProviderUtil.saveMyExperimentsToDisk(contentAsString);
    } catch (JsonParseException e) {
      onError(e);
    } catch (JsonMappingException e) {
      onError(e);
    } catch (UnsupportedCharsetException e) {
      onError(e);
    } catch (IOException e) {
      onError(e);
    }
  }

}

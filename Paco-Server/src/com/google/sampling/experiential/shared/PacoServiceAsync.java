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
package com.google.sampling.experiential.shared;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.ExperimentQueryResult;

/*
 * * The async counterpart of <code>MeetingService</code>.
 */
public interface PacoServiceAsync {
  void eventsForUser(AsyncCallback<List<EventDAO>> callback);

  void eventSearch(String tags, AsyncCallback<List<EventDAO>> callback);

  void saveEvent(String who,
      String scheduledTime,
      String responseTime,
      String experimentId,
      Map<String, String> kvPairs,
      Integer experimentVersion,
      boolean shared, AsyncCallback<Void> asyncCallback);

  /**
   * @param timeZone
   * @param title
   * @param description
   * @param kvPairs
   * @param questionsCanChange
   * @param feedbackText
   * @param feedbackType
   * @param asyncCallback
   */
  void saveExperiment(ExperimentDAO experiment, String timeZone, AsyncCallback<Void> asyncCallback);

  void getAllJoinableExperiments(String tz, Integer limit, String cursor, AsyncCallback<ExperimentQueryResult> callback);

  void getMyJoinableExperiments(String tz, Integer limit, String cursor, AsyncCallback<ExperimentQueryResult> callback);

  void getUsersJoinedExperiments(Integer limit, String cursor, AsyncCallback<ExperimentQueryResult> callback);

  void getUsersAdministeredExperiments(Integer limit, String cursor, AsyncCallback<ExperimentQueryResult> callback);

  void joinExperiment(Long id, AsyncCallback<Boolean> asyncCallback);

  /**
   * @param experiment
   */
  void deleteExperiment(ExperimentDAO experiment, AsyncCallback<Boolean> asyncCallback);

  /**
   * @param id
   * @param callback
   */
  void statsForExperiment(Long id, boolean justUser, AsyncCallback<ExperimentStatsDAO> callback);

  void saveEvent(EventDAO event, AsyncCallback<Void> asyncCallback);

  void referencedExperiment(Long referencedExperimentId, AsyncCallback<ExperimentDAO> referencedCheckCallback);

  void setReferencedExperiment(Long referringExperimentId, Long referencedExperimentId, AsyncCallback<Void> callback);

  void getEndOfDayEvents(String queryText, AsyncCallback<Map<Date, EventDAO>> referringCallback);


}

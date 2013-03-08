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

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

/*
 * * The async counterpart of <code>MeetingService</code>.
 */
public interface MapServiceAsync {
  void map(AsyncCallback<List<EventDAO>> callback);

  void mapWithTags(String tags, AsyncCallback<List<EventDAO>> callback);

  void saveEvent(String who, 
      String scheduledTime, 
      String responseTime, 
      String experimentId,
      Map<String, String> kvPairs,
      Integer experimentVersion,
      boolean shared, AsyncCallback<Void> asyncCallback);

  /**
   * @param title
   * @param description
   * @param kvPairs
   * @param questionsCanChange
   * @param feedbackText 
   * @param feedbackType 
   * @param asyncCallback
   */
  void saveExperiment(ExperimentDAO experiment, AsyncCallback<Void> asyncCallback);

  void getExperimentsForUser(AsyncCallback<List<ExperimentDAO>> callback);

  /**
   * @param experiment
   */
  void deleteExperiment(ExperimentDAO experiment, AsyncCallback<Boolean> asyncCallback);

  /**
   * @param id
   * @param callback
   */
  void statsForExperiment(Long id, boolean justUser, AsyncCallback<ExperimentStatsDAO> callback);
  
  void getUsersJoinedExperiments(AsyncCallback<List<ExperimentDAO>> callback);

  void saveEvent(EventDAO event, AsyncCallback<Void> asyncCallback);

  void referencedExperiment(Long referencedExperimentId, AsyncCallback<ExperimentDAO> referencedCheckCallback);

  void setReferencedExperiment(Long referringExperimentId, Long referencedExperimentId, AsyncCallback<Void> callback);

}

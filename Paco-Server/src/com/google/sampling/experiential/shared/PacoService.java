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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.ExperimentQueryResult;

/*
 * * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("maps")
public interface PacoService extends RemoteService {
  List<EventDAO> eventsForUser();

  List<EventDAO> eventSearch(String tags);

  void saveEvent(String who, String scheduledTime, String responseTime, String experimentId,
                 Map<String, String> kvPairs, Integer experimentVersion, boolean shared);

  void saveExperiment(ExperimentDAO experiment, String timeZone);

  Boolean deleteExperiment(ExperimentDAO experiment);

  boolean joinExperiment(Long id);

  ExperimentQueryResult getAllJoinableExperiments(String tz, Integer limit, String cursor);

  ExperimentQueryResult getMyJoinableExperiments(String tz, Integer limit, String cursor);

  ExperimentQueryResult getUsersJoinedExperiments(Integer limit, String cursor);

  ExperimentQueryResult getUsersAdministeredExperiments(Integer limit, String cursor);

  ExperimentStatsDAO statsForExperiment(Long experimentId, boolean justUser);

  void saveEvent(EventDAO event);

  ExperimentDAO referencedExperiment(Long referencedExperimentId);

  void setReferencedExperiment(Long referringExperimentId, Long referencedExperimentId);

  Map<Date, EventDAO> getEndOfDayEvents(String queryText);
}

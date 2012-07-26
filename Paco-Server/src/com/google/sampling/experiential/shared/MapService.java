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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/*
 * * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("maps")
public interface MapService extends RemoteService {
  List<Response> map();

  List<Response> mapWithTags(String tags);

  void saveResponse(String who, 
      String scheduledTime, 
      String responseTime, 
      String experimentId,
      Map<String, String> kvPairs, 
      boolean shared);

  void saveExperiment(Experiment experiment);

  Boolean deleteExperiment(Experiment experiment);

  List<Experiment> getExperimentsForUser();

  ExperimentStats statsForExperiment(Long experimentId, boolean justUser);

  List<Experiment> getUsersJoinedExperiments();
}

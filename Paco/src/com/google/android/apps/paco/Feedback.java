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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class Feedback {

  @JsonIgnore
  private Long id;
  private Long experimentId;

  @JsonProperty("id")
  private Long serverId;
  private String text = "";

  @JsonIgnore
  public Long getId() {
    return id;
  }

  @JsonIgnore
  public void setId(Long id) {
    this.id = id;
  }

  public Long getExperimentId() {
    return experimentId;
  }

  public void setExperimentId(Long experimentId) {
    this.experimentId = experimentId;
  }

  @JsonProperty("id")
  public Long getServerId() {
    return serverId;
  }

  @JsonProperty("id")
  public void setServerId(Long serverId) {
    this.serverId = serverId;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }


  String getTextOfInputForOutput(Experiment experiment, Output output) {
    for (Input input : experiment.getInputs()) {
      if (input.getServerId().equals(output.getInputServerId())) {
        if (!input.isInvisible()) {
          return input.getText();
        } else {
          return input.getResponseType();
        }
      }
    }
    return output.getName();
  }



}

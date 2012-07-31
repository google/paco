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

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Class that holds a response to an experiment.
 *
 * @author Bob Evans
 *
 */
public class Response implements Serializable {
  private Long id;
  private String subject;
  private Long experimentId;
  private Long experimentVersion;
  private Date createTime;
  private Date signalTime;
  private Date responseTime;
  private Map<String, String> outputs = Maps.newHashMap();

  /**
   * Default constructor
   */
  public Response() {}

  /**
   * @param subject
   * @param experimentId
   * @param experimentVersion
   * @param signalTime
   * @param responseTime
   * @param what
   */
  public Response(String subject,
      Long experimentId,
      Long experimentVersion,
      Date createTime,
      Date signalTime,
      Date responseTime,
      Map<String, String> what) {
    super();
    this.subject = subject;
    this.experimentId = experimentId;
    this.experimentVersion = experimentVersion;
    this.createTime = createTime;
    this.signalTime = signalTime;
    this.responseTime = responseTime;
    this.outputs = what;
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * @param subject the subject to set
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * @return the experimentId
   */
  public Long getExperimentId() {
    return experimentId;
  }

  /**
   * @param experimentId the experimentId to set
   */
  public void setExperimentId(Long experimentId) {
    this.experimentId = experimentId;
  }

  /**
   * @return the experimentVersion
   */
  public Long getExperimentVersion() {
    return experimentVersion;
  }

  /**
   * @param experimentVersion the experimentVersion to set
   */
  public void setExperimentVersion(Long experimentVersion) {
    this.experimentVersion = experimentVersion;
  }

  /**
   * @return the createTime
   */
  public Date getCreateTime() {
    return createTime;
  }

  /**
   * @param createTime the createTime to set
   */
  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  /**
   * @return the signalTime
   */
  public Date getSignalTime() {
    return signalTime;
  }

  /**
   * @param signalTime the signalTime to set
   */
  public void setSignalTime(Date signalTime) {
    this.signalTime = signalTime;
  }

  /**
   * @return the responseTime
   */
  public Date getResponseTime() {
    return responseTime;
  }

  /**
   * @param responseTime the responseTime to set
   */
  public void setResponseTime(Date responseTime) {
    this.responseTime = responseTime;
  }

  /**
   * @param outputs the outputs to set
   */
  public void setOutputs(Map<String, String> outputs) {
    this.outputs = outputs;
  }

  /**
   * @param key
   * @return
   */
  public String getOutputByKey(String key) {
    return this.outputs.get(key);
  }

  /**
   * @return
   */
  public Map<String, String> getOutputs() {
    return outputs;
  }

  /**
   * @return
   */
  public boolean isMissedSignal() {
    return responseTime == null;
  }

  /**
   * @return
   */
  public long responseTime() {
    if (responseTime == null || signalTime == null) {
      return -1;
    }
    return responseTime.getTime() - signalTime.getTime();
  }

  /**
   * @return
   */
  public String getOutputsString() {
    if (outputs == null) {
      return "";
    }

    return outputs.toString();
  }
}

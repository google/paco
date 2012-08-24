/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.paco.shared.model;

import java.util.Date;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.google.common.collect.Maps;

/**
 * Class that holds a response to an experiment.
 *
 * @author Bob Evans
 *
 */
public class Event {
  @JsonIgnore
  private Long id;
  @JsonIgnore
  private String subject;
  @JsonIgnore
  private long experimentId;
  @JsonIgnore
  private Date createTime;

  private long experimentVersion;
  private Date signalTime;
  private Date responseTime;
  private Map<String, String> outputs;

  /**
   * Default constructor
   */
  public Event() {
    super();

    this.id = null;
    this.subject = null;
    this.experimentId = 0;
    this.experimentVersion = 0;
    this.createTime = new Date();
    this.signalTime = null;
    this.responseTime = null;
    this.outputs = Maps.newHashMap();
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
   * @return whether the event has an id
   */
  public boolean hasId() {
    return (id != null);
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
  public void setExperimentVersion(long experimentVersion) {
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
    if (outputs == null) {
      this.outputs = Maps.newHashMap();
    } else {
      this.outputs = outputs;
    }
  }

  /**
   * @param key the key
   * @param output the output
   * @return sets the output corresponding to the key
   */
  public String setOutputByKey(String key, String output) {
    return outputs.put(key, output);
  }

  /**
   * @param key the key
   * @return the output corresponding to the key
   */
  public String getOutputByKey(String key) {
    return outputs.get(key);
  }

  /**
   * @return the outputs
   */
  public Map<String, String> getOutputs() {
    return outputs;
  }

  /**
   * @return whether the event is a missed signal
   */
  @JsonIgnore
  public boolean isMissedSignal() {
    return responseTime == null;
  }

  /**
   * @return the response time or -1 if it is a misssed signal
   */
  public long responseTime() {
    if (responseTime == null || signalTime == null) {
      return -1;
    }

    return responseTime.getTime() - signalTime.getTime();
  }

  /**
   * @return a string representation of the outputs
   */
  @JsonIgnore
  public String getOutputsString() {
    if (outputs == null) {
      return "";
    }

    return outputs.toString();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (!(obj instanceof Event)) {
      return false;
    }

    Event other = (Event) obj;

    if (getId().equals(other.getId()) == false) {
      System.out.println("bad id");
      return false;
    }

    if (getSubject().equals(other.getSubject()) == false) {
      System.out.println("bad subject");
      return false;
    }

    if (getExperimentId().equals(other.getExperimentId()) == false) {
      System.out.println("bad experimentid");
      return false;
    }

    if (getCreateTime().equals(other.getCreateTime()) == false) {
      System.out.println("bad createtime");
      return false;
    }

    if (getSignalTime().equals(other.getSignalTime()) == false) {
      System.out.println("bad signaltime");
      return false;
    }

    if (getResponseTime().equals(other.getResponseTime()) == false) {
      System.out.println("bad responsetime");
      return false;
    }

    if (getOutputs().equals(other.getOutputs()) == false) {
      System.out.println("bad outputs");
      System.out.println(getOutputsString());
      System.out.println(other.getOutputsString());
      return false;
    }

    return true;
  }
}

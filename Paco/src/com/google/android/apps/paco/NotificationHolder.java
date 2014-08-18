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

import org.joda.time.DateTime;
import org.joda.time.Minutes;


public class NotificationHolder  {

  private static final String DEFAULT_SIGNAL_GROUP = "default";
  private Long id;
  private Long alarmTime;
  private Long experimentId;
  private Integer noticeCount;
  private Long timeoutMillis;
  /** The source of this experiment, e.g., one of the signal groups (currently only one) or a custom notification created by api **/
  private String notificationSource = DEFAULT_SIGNAL_GROUP;
  private String message;
  public static final String CUSTOM_GENERATED_NOTIFICATION = "customGenerated";


  public NotificationHolder(Long alarmTime, Long experimentId,
      Integer noticeCount, Long timeoutMillis, String notificationSource, String message) {
    super();
    this.alarmTime = alarmTime;
    this.experimentId = experimentId;
    this.noticeCount = noticeCount;
    this.timeoutMillis = timeoutMillis;
    this.notificationSource = notificationSource;
    this.message = message;
  }

  public NotificationHolder() {
  }

  public Long getAlarmTime() {
    return alarmTime;
  }

  public void setAlarmTime(Long alarmTime) {
    this.alarmTime = alarmTime;
  }

  public Integer getNoticeCount() {
    return noticeCount;
  }

  public void setNoticeCount(Integer noticeCount) {
    this.noticeCount = noticeCount;
  }

  public Long getTimeoutMillis() {
    return timeoutMillis;
  }

  public void setTimeoutMillis(Long timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getExperimentId() {
    return experimentId;
  }

  public void setExperimentId(Long experimentId) {
    this.experimentId = experimentId;
  }

  public boolean isActive(DateTime now) {
    return now.isBefore(new DateTime(alarmTime).plus(Minutes.minutes(timeoutMillis.intValue() / 60000)));
  }

  public String getNotificationSource() {
    return notificationSource;
  }

  public void setNotificationSource(String notificationSource) {
    this.notificationSource = notificationSource;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean isCustomNotification() {
    return this.notificationSource.equals(NotificationHolder.CUSTOM_GENERATED_NOTIFICATION);
  }

}

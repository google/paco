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
package com.pacoapp.paco.model;

import android.provider.BaseColumns;

public class EsmSignalColumns implements BaseColumns {

  public static final String DATE = "date";
  public static final String EXPERIMENT_ID = "experiment_id";
  public static final String TIME = "time";
  public static final String NOTIFICATION_CREATED = "fired";
  public static final String GROUP_NAME = "group_name";
  public static final String ACTION_TRIGGER_ID = "action_trigger_id";
  public static final String SCHEDULE_ID = "schedule_id";
  

  private EsmSignalColumns() {
  }

}

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
/**
 *
 */
package com.pacoapp.paco.model;

import android.net.Uri;
import android.provider.BaseColumns;

public class NotificationHolderColumns implements BaseColumns {
  public static final String ALARM_TIME = "alarm_time";
  public static final String EXPERIMENT_ID = "experiment_id";
  public static final String NOTICE_COUNT = "notice_count";
  public static final String TIMEOUT_MILLIS = "timeout_millis";
  public static final String NOTIFICATION_SOURCE = "notification_source";
  public static final String CUSTOM_MESSAGE = "custom_message";
  public static final String EXPERIMENT_GROUP_NAME = "experiment_group_name";
  public static final String ACTION_TRIGGER_ID = "action_trigger_id";
  public static final String ACTION_TRIGGER_SPEC_ID = "action_trigger_spec_id";
  public static final String ACTION_ID = "action_id";

//TODO Do we need this given that we are packing the path to the action again (exp, expGrp, actTrig, action)?
  public static final String SNOOZE_COUNT = "snooze_count";
  public static final String SNOOZE_TIME = "snooze_time";


  public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.paco.notification";
  public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.paco.notification";

  public static final Uri CONTENT_URI = Uri.parse("content://"+ExperimentProviderUtil.AUTHORITY+"/notifications");




}
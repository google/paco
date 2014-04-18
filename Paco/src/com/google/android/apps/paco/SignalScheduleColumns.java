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
package com.google.android.apps.paco;

import android.net.Uri;
import android.provider.BaseColumns;

public class SignalScheduleColumns implements BaseColumns {
  public static final String EXPERIMENT_ID = "experiment_id";
  public static final String SERVER_ID = "server_id";
  public static final String SCHEDULE_TYPE = "schedule_type";
  public static final String TIMES_CSV = "times";
  public static final String ESM_FREQUENCY = "esm_frequency";
  public static final String ESM_PERIOD = "esm_period";

  public static final String ESM_START_HOUR = "esm_start_hour";
  public static final String ESM_END_HOUR = "esm_end_hour";
  public static final String ESM_WEEKENDS = "esm_weekends";

  public static final String REPEAT_RATE =  "repeat_rate";
  public static final String WEEKDAYS_SCHEDULED  = "weekdays_scheduled";
  public static final String NTH_OF_MONTH  = "nth_of_month";
  public static final String BY_DAY_OF_MONTH = "by_day_of_month";
  public static final String DAY_OF_MONTH  = "day_of_month";
  public static final String BEGIN_DATE  = "begin_date";
  public static final String USER_EDITABLE = "user_editable";
  public static final String TIME_OUT = "timeout";
  public static final String MINIMUM_BUFFER = "minimum_buffer";
  public static final String SNOOZE_COUNT = "snooze_count";
  public static final String SNOOZE_TIME = "snooze_time";

  public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.paco.schedule";
  public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.paco.schedule";

  public static final Uri CONTENT_URI = Uri.parse("content://"+ExperimentProviderUtil.AUTHORITY+"/schedules");






}
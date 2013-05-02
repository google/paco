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

public class ExperimentColumns implements BaseColumns {
  
  public static final Uri CONTENT_URI = Uri.parse("content://"+ExperimentProviderUtil.AUTHORITY+"/experiments");
  public static final Uri JOINED_EXPERIMENTS_CONTENT_URI = Uri.parse("content://"+ExperimentProviderUtil.AUTHORITY+"/joinedexperiments");
  
  public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.paco.experiment";
  public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.paco.experiment";
  
  public static final String DEFAULT_SORT_ORDER = "_id";

  public static final String SERVER_ID = "server_id"; // id on the cloud
  public static final String TITLE = "title";
  public static final String DESCRIPTION = "description";
  public static final String CREATOR = "creator";
  public static final String HASH = "hash";
  public static final String INFORMED_CONSENT = "informed_consent";
  public static final String FIXED_DURATION ="fixed_duration"; // Essentially, do the start and end date fields matter?
  public static final String START_DATE ="start_date";
  public static final String END_DATE = "end_date";
//public static final String SCHEDULE_TYPE = "schedule_type"; // [DAILY, ONCE, FIXED_REPEATING, ESM_REPEATING]
  // FIXED_REPEATING is currently daily. expand to cron-like description in future. 

//  public static final String DEFAULT_TIME = "default_time"; // default time of day to be signaled for ONCe and FIXED_REPEATING
//  public static final String ESM_FREQUENCY = "frequency"; // How many times to signal user during the period of the schedule_type.
  													// does not apply to ONCE.
//  public static final String ESM_PERIOD = "period"; // How many times to signal user during the period of the schedule_type.
  // does not apply to ONCE.

  public static final String ICON = "icon";
  // variation for the joined experiment
  public static final String JOIN_DATE ="join_date";
  public static final String QUESTIONS_CHANGE = "questions_change"; // should we setup a service to check for updates to the questions? QOTD case.
  
  public static final String WEB_RECOMMENDED = "web_recommended";
  public static final String VERSION = "version";
  
  public static final String JSON = "json";
    
}
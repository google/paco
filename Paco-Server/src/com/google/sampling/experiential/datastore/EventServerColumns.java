package com.google.sampling.experiential.datastore;

import com.pacoapp.paco.shared.model2.EventBaseColumns;

public class EventServerColumns extends EventBaseColumns {
  public static final String WHO = "who";
  //since when is a keyword, we have to always use a back tick when we use it in any sql query.
  public static final String WHEN = "`when`";
  public static final String ARCHIVE_FLAG = "archive_flag";
  public static final String JOINED = "joined";
  public static final String SORT_DATE = "sort_date";
  public static final String CLIENT_TIME_ZONE = "client_timezone";
  public static final String PACO_VERSION = "paco_version";
  public static final String APP_ID = "app_id"; 
}

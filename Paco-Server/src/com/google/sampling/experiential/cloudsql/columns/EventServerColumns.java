package com.google.sampling.experiential.cloudsql.columns;

import com.pacoapp.paco.shared.model2.EventBaseColumns;

public class EventServerColumns extends EventBaseColumns {
  public static final String WHO = "who";
  //since when is a keyword, we have to always use a back tick when we use it in any sql query.
  public static final String WHEN = "`when`";
  public static final String WHEN_FRAC_SEC = "when_fractional_sec";
  public static final String ARCHIVE_FLAG = "archive_flag";
  public static final String JOINED = "joined";
  public static final String SORT_DATE = "sort_date";
  public static final String CLIENT_TIME_ZONE = "client_timezone";
  public static final String PACO_VERSION = "paco_version";
  public static final String APP_ID = "app_id";
  public static final String SORT_DATE_UTC = "sort_date_utc";
  public static final String RESPONSE_TIME_UTC = "response_time_utc";
  public static final String SCHEDULE_TIME_UTC = "schedule_time_utc";
  public static final String EXPERIMENT_VERSION_GROUP_MAPPING_ID = "experiment_version_group_mapping_id";
  
}

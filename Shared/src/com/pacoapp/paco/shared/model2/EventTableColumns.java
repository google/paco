package com.pacoapp.paco.shared.model2;

public enum EventTableColumns {
  EXPERIMENT_ID ("experiment_id", 1, false, false ),
  EXPERIMENT_SERVER_ID ( "experiment_server_id", 2, false, false),
  EXPERIMENT_NAME ( "experiment_name", 3, true, false),
  EXPERIMENT_VERSION ( "experiment_version", 4, false, false),
  SCHEDULE_TIME ( "schedule_time",5, true, true),
  RESPONSE_TIME ( "response_time",6, true, true),
  UPLOADED ( "uploaded", 7, false, false),
  GROUP_NAME ( "group_name",8, true, false),
  ACTION_TRIGGER_ID ( "action_trigger_id",9, false, false),
  ACTION_TRIGGER_SPEC_ID ( "action_trigger_spec_id", 10, false, false),
  ACTION_ID ( "action_id",11, false, false),
  LAT ( "lat", 12, true, false),
  LON ( "lon", 13, true, false),
  WHO ( "who", 14, true, false),
  PACO_VERSION("paco_version", 15, true, false),
  EVENTS_ID("events._id", 16, false, false),
  _ID("_id", 17, false, false);
  
  private String colNameInDB;
  int index;
  boolean quoted;
  boolean dateTime;
  
  EventTableColumns(String colName, int index, boolean quoted, boolean dateTime){
    this.colNameInDB = colName;
    this.index = index;
    this.quoted = quoted;
    this.dateTime = dateTime;
  }

  public String getColNameInDB() {
    return colNameInDB;
  }

  public void setColNameInDB(String colNameInDB) {
    this.colNameInDB = colNameInDB;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public boolean isQuoted() {
    return quoted;
  }

  public void setQuoted(boolean quoted) {
    this.quoted = quoted;
  }

  public boolean isDateTime() {
    return dateTime;
  }

  public void setDateTime(boolean dateTime) {
    this.dateTime = dateTime;
  }
  
}

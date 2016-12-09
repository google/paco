package com.google.sampling.experiential.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTimeZone;

import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.comm.SQLQuery1;

public interface StreamingStrategy  {
  long WAIT_TIME = 60000;
  boolean USE_LEGACY_SQL = true;
  List<EventDAO> processRequest(SQLQuery1 jsonRequest, String user, DateTimeZone tz);
 
//  AvailableDataStrategy
//  LiveDataStrategy  
  
//  boolean createTablesinBQ();
//  boolean populateTablesinBQ();
//  List<Event> processQueryinBQ(String jsonQuery);
//  
//  boolean isLiveDataNeeded=false;
//  boolean isCurrentDataLoaded=false;
//  
//  void setIsLiveDataNeeded(boolean flag);
//  void setIsCurrentDataLoaded(boolean flag);
//  
//  boolean getIsLiveDataNeeded();
//  boolean getIsCurrentDataLoaded();
  
  

}

//class 

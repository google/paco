package com.google.sampling.experiential.server;

import java.util.List;

import org.joda.time.DateTimeZone;

import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.comm.SQLQuery1;

public class DataContextFactory  {
  
  private DataStrategy ds;
  
  private void identifyStrategy(SQLQuery1 jsonRequest){
    String liveData = jsonRequest.getLiveData();
    if(liveData.equalsIgnoreCase("false")){
      ds =  new AvailableDataStrategy();
    } else if(liveData.equalsIgnoreCase("true")){
      ds = new LiveDataStrategy(jsonRequest.getStreaming());
    }
  }
  
  public List<EventDAO> processRequest(SQLQuery1 jsonRequest, String user, DateTimeZone tz){
    identifyStrategy(jsonRequest);
    return ds.processRequest(jsonRequest, user, tz);
  }
}

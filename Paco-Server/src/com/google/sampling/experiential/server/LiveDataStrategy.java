package com.google.sampling.experiential.server;

import java.util.List;

import org.joda.time.DateTimeZone;

import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.comm.SQLQuery1;

public class LiveDataStrategy implements DataStrategy {
  StreamingStrategy ss;
  
  public LiveDataStrategy(){
    //TODO is this needed --> default
    this.ss = new CompleteStreamingStrategy();
  }
  
  public LiveDataStrategy(String jsonRequest){
    setStreamingStrategy(jsonRequest);
  }


  private void setStreamingStrategy(String jsonRequest){
    if (jsonRequest.contains("incremental")){
      ss = new IncrementalStreamingStrategy();
    }else if (jsonRequest.contains("complete")){
      ss = new CompleteStreamingStrategy();
    }
  }  
   
  @Override
  public List<EventDAO> processRequest(SQLQuery1 jsonRequest, String user, DateTimeZone tz) {
    // TODO Auto-generated method stub
    return ss.processRequest(jsonRequest, user, tz);
  }

  

}

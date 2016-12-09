package com.google.sampling.experiential.server;
import java.util.List;

import org.joda.time.DateTimeZone;

import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.comm.SQLQuery1;

public class AvailableDataStrategy implements DataStrategy {
  @Override
  public List<EventDAO> processRequest(SQLQuery1 jsonRequest , String user, DateTimeZone tz) {
    PacoBQProcessor pbq = new PacoBQProcessor();
    return pbq.processRequest(jsonRequest, user, tz);
           
  }
}

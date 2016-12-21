package com.google.sampling.experiential.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTimeZone;

import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.comm.SQLQuery1;

public interface DataStrategy  {
  long WAIT_TIME = 60000;
  boolean USE_LEGACY_SQL = true;
  String TABLE_NAME = "hello12";
  
  List<EventDAO> processRequest(SQLQuery1 jsonRequest, String user, DateTimeZone tz);
 }

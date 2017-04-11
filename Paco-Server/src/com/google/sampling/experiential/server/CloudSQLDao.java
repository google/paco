package com.google.sampling.experiential.server;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTimeZone;

import com.google.sampling.experiential.model.ApplicationUsage;
import com.google.sampling.experiential.model.ApplicationUsageRaw;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.SPRequest;

public interface CloudSQLDao {
  boolean insertEvent(Event e) throws SQLException, ParseException;
  List<EventDAO> getEvents(String query, DateTimeZone tzForClient) throws SQLException, ParseException;
  Map<String,List<ApplicationUsageRaw>> getAppUsageTopN(SPRequest spRequest) throws SQLException;
  String createTables() throws SQLException;
}

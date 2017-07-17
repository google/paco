package com.google.sampling.experiential.server;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.WhatDAO;

public interface CloudSQLDao {
  boolean insertEventAndOutputs(Event e) throws SQLException, ParseException;
  boolean insertSingleEventOnly(Event e) throws SQLException, ParseException;
  boolean insertSingleOutput(Long eventId, String text, String answer) throws SQLException; 
  boolean insertFailedEvent(String failedJson, String reason, String comments);
  List<EventDAO> getEvents(String query, DateTimeZone tzForClient, Long eventId) throws SQLException, ParseException;
  List<EventDAO> getEvents(Long eventId) throws SQLException, ParseException;
  JSONArray getResultSetAsJson(String query, DateTimeZone tzForClient, Long eventId) throws SQLException, ParseException, JSONException;
  Map<Long, String> getFailedEvents() throws SQLException;
  boolean updateFailedEventsRetry(Long failedEventsId, String reprocessed) throws SQLException;
  List<WhatDAO> getOutputs(Long eventId) throws SQLException;
}

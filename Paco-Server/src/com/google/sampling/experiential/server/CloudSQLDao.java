package com.google.sampling.experiential.server;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTimeZone;

import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.shared.EventDAO;

public interface CloudSQLDao {
  boolean insertEvent(Event e) throws SQLException, ParseException;
  boolean insertFailedEvent(String failedJson, String reason, String comments);
  List<EventDAO> getEvents(String query, DateTimeZone tzForClient) throws SQLException, ParseException;
  List<EventDAO> getEvents(String eventId) throws SQLException, ParseException;
  Map<Long, String> getFailedEvents() throws SQLException;
  boolean updateFailedEventsRetry(Long id, String reprocessed) throws SQLException;
}

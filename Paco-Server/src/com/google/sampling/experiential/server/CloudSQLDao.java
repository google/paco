package com.google.sampling.experiential.server;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTimeZone;

import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.server.migration.MigrationOutput;
import com.google.sampling.experiential.shared.EventDAO;

public interface CloudSQLDao {
  boolean insertEvent(Event e) throws SQLException, ParseException;
  List<EventDAO> getEvents(String query, DateTimeZone tzForClient) throws SQLException, ParseException;
  String createTables() throws SQLException;
  boolean insertEventsInBatch(List<Event> events) ;
  boolean insertOutputsInBatch(Set<MigrationOutput> outputs);
}

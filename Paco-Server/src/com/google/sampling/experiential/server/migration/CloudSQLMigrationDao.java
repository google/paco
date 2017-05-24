package com.google.sampling.experiential.server.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.joda.time.DateTime;
import com.google.sampling.experiential.model.Event;

public interface CloudSQLMigrationDao {
  String createTables() throws SQLException;
  boolean insertEventsInBatch(List<Event> events) ;
  boolean insertOutputsInBatch(List<MigrationOutput> outputs);
  Long getEarliestWhen() throws SQLException, ParseException;
  boolean persistCursor(String cursor);
  boolean persistMissedEvent(Connection conn, Long eventId);
  boolean persistStreamingStart(DateTime startTime);
  Long getEarliestStreaming() throws SQLException, ParseException;
}

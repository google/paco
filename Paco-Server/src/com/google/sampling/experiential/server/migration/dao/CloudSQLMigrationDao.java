package com.google.sampling.experiential.server.migration.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.joda.time.DateTime;

import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.server.migration.MigrationOutput;

public interface CloudSQLMigrationDao {
  String createTables(String stepNo) throws SQLException;
  boolean insertEventsInBatch(List<Event> events) ;
  boolean insertOutputsInBatch(List<MigrationOutput> outputs);
  Long getEarliestWhen() throws SQLException, ParseException;
  boolean persistCursor(String cursor);
  boolean persistMissedEvent(Connection conn,  String migrationOutput);
  boolean persistStreamingStart(DateTime startTime);
  Long getEarliestStreaming() throws SQLException, ParseException;
  boolean insertCatchupFailure(String insertType, Long eventId, String text, String comments);
}

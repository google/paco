package com.google.sampling.experiential.server.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.joda.time.DateTime;

import com.google.sampling.experiential.model.Event;

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
  boolean eventV5AddNewColumns() throws SQLException;
  boolean eventV5RenameExistingColumns() throws SQLException;
  boolean eventV5UpdateNewColumnsWithValues() throws SQLException;
  boolean eventV5RemoveOldIndexes() throws SQLException;
  boolean eventV5AddNewIndexes() throws SQLException;
  boolean anonymizeParticipantsCreateTables() throws SQLException;
  boolean anonymizeParticipantsAddColumnToEventTable() throws SQLException;
  boolean anonymizeParticipantsMigrateToUserAndExptUser() throws SQLException;
  boolean anonymizeParticipantsMigrateToExperimentLookup()  throws SQLException;
  boolean anonymizeParticipantsMigrateToExperimentLookupTracking()  throws SQLException;
  boolean anonymizeParticipantsUpdateEventWhoAndLookupIdByTracking() throws SQLException;
  boolean anonymizeParticipantsUpdateEventWhoAndLookupIdSerially() throws SQLException;
  boolean anonymizeParticipantsTakeBackupEventIdWho() throws SQLException;
  boolean anonymizeParticipantsRenameOldEventColumns() throws SQLException;
  boolean anonymizeParticipantsModifyExperimentNameFromNullToBlank() throws SQLException;
}

package com.google.sampling.experiential.server.migration.dao;

import java.sql.SQLException;

public interface AnonymizeParticipantsMigrationDao {
  
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
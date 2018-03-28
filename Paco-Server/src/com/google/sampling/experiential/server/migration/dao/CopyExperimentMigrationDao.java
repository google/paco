package com.google.sampling.experiential.server.migration.dao;

import java.sql.Connection;
import java.sql.SQLException;

import com.google.sampling.experiential.shared.EventDAO;

public interface CopyExperimentMigrationDao {
  
  boolean copyExperimentCreateTables() throws SQLException; 
  boolean addModificationsToExistingTables() throws SQLException;
  boolean insertPredefinedRecords() throws SQLException, Exception; 
  boolean copyExperimentPopulateExperimentBundleTables()  throws SQLException; 
  boolean anonymizeParticipantsCreateTables() throws SQLException; 
  boolean populatePivotTableHelper() throws SQLException;
  boolean updateEventTableGroupNameNull() throws SQLException;
  boolean copyExperimentRenameOldEventColumns() throws SQLException;
  boolean copyExperimentSplitGroupsAndPersist() throws SQLException, Exception;
  boolean copyExperimentTakeBackupInCloudSql() throws SQLException;
  boolean insertIntoExperimentDefinition() throws SQLException, Exception;
  EventDAO getSingleUnprocessedEvent(Connection conn) throws SQLException;
  void processOlderVersionsAndAnonUsersInEventTable(Connection conn) throws Exception;
}

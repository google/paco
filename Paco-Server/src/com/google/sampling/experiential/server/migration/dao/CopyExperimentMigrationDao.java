package com.google.sampling.experiential.server.migration.dao;

import java.sql.SQLException;

public interface CopyExperimentMigrationDao {
  
  boolean copyExperimentCreateTables() throws SQLException; 
  boolean addModificationsToExistingTables() throws SQLException;
  boolean insertPredefinedRecords() throws SQLException; 
  boolean copyExperimentMigrateFromDataStoreToCloudSql()  throws SQLException; 
  boolean anonymizeParticipantsCreateTables() throws SQLException; 
  boolean populatePivotTableHelper() throws SQLException;
  boolean processPivotTableHelper()  throws SQLException;
  boolean updateEventTableGroupNameNull() throws SQLException;
  boolean processOlderVersionsAndAnonUsersInEventTable() throws SQLException;
  boolean copyExperimentRenameOldEventColumns() throws SQLException;
  boolean copyExperimentSplitGroupsAndPersist() throws SQLException;
}

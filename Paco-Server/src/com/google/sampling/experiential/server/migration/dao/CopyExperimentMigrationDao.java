package com.google.sampling.experiential.server.migration.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.google.sampling.experiential.shared.EventDAO;

public interface CopyExperimentMigrationDao {
  
  boolean copyExperimentCreateTables() throws SQLException; 
  boolean addModificationsToExistingTables() throws SQLException;
  boolean insertPredefinedRecords() throws SQLException, Exception; 
  boolean copyExperimentPopulateExperimentBundleTables()  throws SQLException; 
  boolean copyExperimentPopulateDistinctExperimentIdVersionAndGroupName() throws SQLException;
  boolean copyExperimentDeleteEventsAndOutputsForDeletedExperiments() throws SQLException;
  boolean copyExperimentCreateEVGMRecordsForAllExperiments() throws SQLException;
  boolean copyExperimentChangeGroupNameOfEventsWithPredefinedInputs() throws SQLException;
  boolean anonymizeParticipantsCreateTables() throws SQLException; 
  boolean populatePivotTableHelper() throws SQLException;
  boolean updateEventTableExperimentVersionAndGroupNameNull() throws SQLException;
  boolean copyExperimentRenameOldEventColumns() throws SQLException;
  boolean copyExperimentSplitGroupsAndPersist() throws SQLException, Exception;
  boolean copyExperimentTakeBackupInCloudSql() throws SQLException;
  boolean insertIntoExperimentDefinition() throws SQLException, Exception;
  void processOlderVersionsAndAnonUsersInEventTable(Connection conn, List<Long> erroredExperimentIds, List<EventDAO> allEvents) throws Exception;
  List<EventDAO> getSingleBatchUnprocessedEvent(Connection conn, List<Long> erroredExperimentIds, String unprocessedRecordQuery, Long experimentId,
                                                Integer experimentVersion, String groupName) throws SQLException;
  boolean updateEventTableGroupNameNullToUnknown() throws SQLException;
  boolean removeUnwantedPredefinedExperiments() throws Exception;
 
}

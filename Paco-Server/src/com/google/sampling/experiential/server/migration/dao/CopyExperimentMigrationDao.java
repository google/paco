package com.google.sampling.experiential.server.migration.dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.google.sampling.experiential.shared.EventDAO;

public interface CopyExperimentMigrationDao {
  
  Boolean dataCleanupMakeDBChanges() throws SQLException;
  boolean dataCleanupUpdateEventTableExperimentVersionAndGroupNameNull() throws SQLException;
  boolean dataCleanupRemoveUnwantedEventAndOutputsPredefinedExperiments() throws Exception;
  boolean dataCleanupChangeDupCounterAloneOnVariableNames(String query) throws Exception;
  boolean dataCleanupEnforceForeignKeyConstraintOnEVGM() throws Exception;
  boolean dataCleanupCreateTables() throws SQLException; 
  boolean dataCleanupAddModificationsToExistingTables() throws SQLException;
  boolean dataCleanupInsertPredefinedRecords() throws SQLException, Exception;
  boolean dataCleanupAnonymizeParticipantsCreateTables() throws SQLException; 
  
  boolean experimentSplitPopulateExperimentBundleTables()  throws SQLException;
  boolean experimentSplitGroupsAndPersist() throws SQLException, Exception;
  boolean experimentSplitTakeBackupInCloudSql() throws SQLException;
  boolean experimentSplitInsertIntoExperimentDefinition() throws SQLException, Exception;
  boolean experimentSplitRemoveUnwantedExperimentJsonFromExperimentDefinition() throws Exception;
  
  boolean copyExperimentPopulateDistinctExperimentIdVersionAndGroupName() throws SQLException;
  boolean copyExperimentDeleteEventsAndOutputsForDeletedExperiments() throws SQLException;
  boolean copyExperimentCreateEVGMRecordsForAllExperiments() throws SQLException;
  boolean copyExperimentChangeGroupNameOfEventsWithPredefinedInputs() throws SQLException;
  boolean copyExperimentRenameOldEventColumns() throws SQLException;
  boolean copyExperimentFilterExperimentsForPivotTableProcessing() throws Exception;
  boolean copyExperimentPopulatePivotTableForFilteredExperiments() throws SQLException;
  boolean copyExperimentUpdateEventAndOutputCatchAll(String query) throws SQLException, Exception;
  boolean copyExperimentChangeDupCounterOnVariableNames(String query) throws Exception;
  String copyExperimentStoreCreateSqlInCloudStorage(String fileName) throws SQLException, FileNotFoundException,
  IOException;
  
  boolean populatePivotTableHelper() throws SQLException;
  List<EventDAO> getSingleBatchUnprocessedEvent(Connection conn, List<Long> erroredExperimentIds,
                                                String unprocessedRecordQuery, Long experimentId,
                                                Integer experimentVersion, String groupName,
                                                Integer batchSize) throws SQLException;

  void processOlderVersionsAndAnonUsersInEventTable(Connection conn, List<Long> erroredExperimentIds, List<EventDAO> allEvents, Boolean aggregateInputNames) throws Exception;
  boolean copyExperimentCreateEVGMRecordsForExperimentsThatDoNotHaveEVGM() throws SQLException;
  boolean copyExperimentPopulatePivotTableForSelectiveRecords() throws SQLException;
  boolean copyExperimentPopulatePivotTableForMissingRecords() throws SQLException;
  void copyExperimentFixMissingInputIds() throws SQLException, Exception;
  
}

package com.google.sampling.experiential.server.migration.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.common.collect.Maps;
import com.google.sampling.experiential.dao.CSOutputDao;
import com.google.sampling.experiential.dao.CSTempExperimentDefinitionDao;
import com.google.sampling.experiential.dao.CSTempExperimentIdVersionGroupNameDao;
import com.google.sampling.experiential.dao.dataaccess.ExperimentLite;
import com.google.sampling.experiential.dao.impl.CSOutputDaoImpl;
import com.google.sampling.experiential.dao.impl.CSTempExperimentDefinitionDaoImpl;
import com.google.sampling.experiential.dao.impl.CSTempExperimentIdVersionGroupNameDaoImpl;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.QueryConstants;
import com.google.sampling.experiential.server.migration.MigrationJob;
import com.google.sampling.experiential.server.migration.dao.CopyExperimentMigrationDao;
import com.google.sampling.experiential.server.migration.dao.impl.CopyExperimentMigrationDaoImpl;
import com.google.sampling.experiential.shared.EventDAO;

public class CopyExperimentFromDataStoreToCloudSqlJob implements MigrationJob {

    public static final Logger log = Logger.getLogger(CopyExperimentFromDataStoreToCloudSqlJob.class.getName());

    @Override
    public boolean doMigration(String cursor, DateTime startTime, DateTime endTime) {
      Boolean success = false;
      try {
        String successMsg = copyExperimentFromDataStoreToCloudSql(cursor);
        if ("All Done".equals(successMsg)) { 
          success = true;
        } else {
          success = false;
        }
      } catch (Exception e) { 
        log.warning(ExceptionUtil.getStackTraceAsString(e));
        success = false;
      }
      return success;
    }
    
    private String copyExperimentFromDataStoreToCloudSql(String cursor) throws Exception {
      String returnString = "";
      Boolean doAll = false;
      CopyExperimentMigrationDao sqlMigDaoImpl = new CopyExperimentMigrationDaoImpl();
      String unprocessedEventRecordQuery = QueryConstants.UNPROCESSED_EVENT_QUERY.toString();
      if (cursor == null) {
        doAll = true;
      }
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step1"))) {
        try {
          log.info("------------------------------------------------Step 1 Begin------------------------------------------------");
          sqlMigDaoImpl.dataCleanupUpdateEventTableExperimentVersionAndGroupNameNull();
          sqlMigDaoImpl.copyExperimentPopulateDistinctExperimentIdVersionAndGroupName();
          sqlMigDaoImpl.copyExperimentDeleteEventsAndOutputsForDeletedExperiments();
          log.info("------------------------------------------------Step 1 End------------------------------------------------");
          returnString += "delete events of deleted experiments Done. Step1 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to delete events of deleted experiments. Restart job from step1";
          throw new SQLException(returnString, e);
        }
      }
      
      if ( doAll || (cursor != null && cursor.equalsIgnoreCase("step2"))) {
        try {
          log.info("------------------------------------------------Step 2 Begin------------------------------------------------");
          // picks up exp id version group name status 0
          sqlMigDaoImpl.copyExperimentCreateEVGMRecordsForAllExperiments();
          // on success, updates exp id version group name status 1
          log.info("------------------------------------------------Step 2 End------------------------------------------------");
          returnString += "create evgm record for earlier versions Done. Step2 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to create evgm records for earlier versions of experiments. Restart job from step2";
          throw new SQLException(returnString, e);
        }
      }
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step3"))) {
        try {
          log.info("------------------------------------------------Step 3 Begin------------------------------------------------");
          // picks up exp id version group name status 1
          sqlMigDaoImpl.copyExperimentChangeGroupNameOfEventsWithPredefinedInputs();
          // on success, updates exp id version group name status 2
          log.info("------------------------------------------------Step 3 End------------------------------------------------");
          returnString += "group name changed for events with predefined inputs Done. Step3 complete.";
          returnString = "All Done";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to change group name for events with predefined inputs. Restart job from step3";
          throw new SQLException(returnString, e);
        }
      }
      
      if ( doAll || (cursor != null && cursor.equalsIgnoreCase("step4"))) {
        try {
          log.info("------------------------------------------------Step 4 Begin------------------------------------------------");
          // picks up exp id version group name status 2
          returnString += updateEventAndOutputTable(2, 1000, unprocessedEventRecordQuery);
          // on success, updates exp id version group name status 3 or 20
          log.info("------------------------------------------------Step 4 End------------------------------------------------");
          doAll = true;
        } catch (Exception e) {
          returnString += "Failed to update event and output table . Restart job from step4";
          throw new SQLException(returnString, e);
        }
      }
      
      if (doAll ||  (cursor != null && cursor.equalsIgnoreCase("step5"))) {
        try {
          log.info("------------------------------------------------Step 5 Begin------------------------------------------------");
          // picks up exp id version group name status 20
          returnString += updateEventAndOutputTable(20, 500, unprocessedEventRecordQuery);
          // on success, updates exp id version group name status 3
          log.info("------------------------------------------------Step 5 End------------------------------------------------");
          doAll = true;
        } catch (Exception e) {
          returnString += "Failed to update event and output table . Restart job from step5";
          throw new SQLException(returnString, e);
        }
       returnString = "All Done";
      }
      
      if (doAll ||  (cursor != null && cursor.equalsIgnoreCase("step6"))) {
        String query = "select * from events where experiment_id is not null and experiment_version_group_mapping_id is null";
        try {
          log.info("------------------------------------------------Step 6 Begin------------------------------------------------");
          // picks up all new exp id version group name combination events posted from the time ExperimentMigrationDataPreCleanupJob->step 7 happens
          // until this jobs first step starts. Some of these events might not have been updated with evgm ids and inputs ids. 
          // Right when this job starts all events will be posted with new format.
          sqlMigDaoImpl.copyExperimentUpdateEventAndOutputCatchAll(query);
          // on success, creates/updates exp id version group name status 0
          // TODO check if evgm exists for it already  if yes just update status or else create evgm and then update
          sqlMigDaoImpl.copyExperimentCreateEVGMRecordsForExperimentsThatDoNotHaveEVGM();
          // on success, updates exp id version group name status 1
          sqlMigDaoImpl.copyExperimentChangeGroupNameOfEventsWithPredefinedInputs();
          // on success, , updates exp id version group name status 2
          updateEventAndOutputTable(2, 1000, unprocessedEventRecordQuery);
          updateEventAndOutputTable(20, 100, unprocessedEventRecordQuery);
          // on success, udpates exp id version group name status 3
          log.info("------------------------------------------------Step 6 End------------------------------------------------");
          returnString += " CatchAll complete"; 
          doAll = true;
        } catch (Exception e) {
          returnString += "Failed to update event and output table . Restart job from step6";
          throw new SQLException(returnString, e);
        }
      }
      
      if (doAll ||  (cursor != null && cursor.equalsIgnoreCase("step7"))) {
        try {
          log.info("------------------------------------------------Step 7 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentFilterExperimentsForPivotTableProcessing();
          // exp id version group name status 5
          log.info("------------------------------------------------Step 7 End------------------------------------------------");
          returnString += "mark pivot table helper Done. Step7 complete.";
          returnString = "All Done";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to populate pivot table helper. Restart job from step7";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll ||  (cursor != null && cursor.equalsIgnoreCase("step8"))) {
        try {
          log.info("------------------------------------------------Step 8 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentPopulatePivotTableForFilteredExperiments();
          // exp id version group name status 4
          log.info("------------------------------------------------Step 8 End------------------------------------------------");
          returnString += "populate pivot table helper Done. Step8 complete.";
        } catch (SQLException e) {
          returnString += "Failed to populate pivot table helper. Restart job from step8";
          throw new SQLException(returnString, e);
        }
      }
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step10"))) {
        try {
          log.info("------------------------------------------------Step 10 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentFixMissingInputIds();
          log.info("------------------------------------------------Step 10 End------------------------------------------------");
          returnString += "Populate pivot table for missing records";
        } catch (SQLException e) {
          returnString += "Failed to update input ids for unprocessed events. Restart job from step10";
          throw new SQLException(returnString, e);
        }
      }
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step11"))) {
        try {
          // picks pivot helper records with events poasted as 0 and then counts number of events and outputs and updates 
          // pivothelper table.
          log.info("------------------------------------------------Step 11 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentPopulatePivotTableForSelectiveRecords();
          log.info("------------------------------------------------Step 11 End------------------------------------------------");
          returnString += "Update update count for pivot helper records that are reset";
        } catch (SQLException e) {
          returnString += "Failed to Update update count for pivot helper records that are reset. Restart job from step11";
          throw new SQLException(returnString, e);
        }
      }

      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step12"))) {
        try {
          log.info("------------------------------------------------Step 12 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentRenameOldEventColumns();
          log.info("------------------------------------------------Step 12 End------------------------------------------------");
          returnString = "Rename old event columns";
        } catch (SQLException e) {
          returnString += "Failed to rename event columns to avoid ambiguity. Restart job from step12";
          throw new SQLException(returnString, e);
        }
      }
      
      if (cursor != null && cursor.equalsIgnoreCase("step13")) {
        try {
          log.info("------------------------------------------------Step 13 Begin------------------------------------------------");
          String blobKey = sqlMigDaoImpl.copyExperimentStoreCreateSqlInCloudStorage("createsql");
          log.info("blobkey return"+blobKey);
          log.info("------------------------------------------------Step 13 End------------------------------------------------");
          returnString = "All Done";
        } catch (SQLException e) {
          returnString += "Failed to create sql. Restart job from step13";
          throw new SQLException(returnString, e);
        }
      }
    
      return returnString;
    }
    
    private String updateEventAndOutputTable(Integer expLiteStatus, Integer batchSize, String unprocessedEventRecordQuery) throws Exception {
      CSTempExperimentDefinitionDao expDefDaoImpl = new CSTempExperimentDefinitionDaoImpl();
      CSTempExperimentIdVersionGroupNameDao expIdVersionDaoImpl = new CSTempExperimentIdVersionGroupNameDaoImpl();
      CSOutputDao outputDaoImpl = new CSOutputDaoImpl();
      CopyExperimentMigrationDao sqlMigDaoImpl = new CopyExperimentMigrationDaoImpl();
      Connection conn = null;
      String returnString = null;
      try {
        List<Long> erroredExperimentIds = expDefDaoImpl.getErroredExperimentDefinition();
        List<EventDAO> allEventsForThisExptVersion = null;
        Map<Long, Long> expIdDistinctVariableMap = Maps.newHashMap();
        int ct = 0;
        Long distinctVariableCount = 0L;
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        List<ExperimentLite> expLites = expIdVersionDaoImpl.getAllExperimentLiteOfStatus(expLiteStatus);
        for (ExperimentLite expLite : expLites) {
          if (expLiteStatus == 2) {
            distinctVariableCount = expIdDistinctVariableMap.get(expLite.getExperimentId());
            if ( distinctVariableCount == null) { 
              distinctVariableCount = outputDaoImpl.getDistinctOutputCount(expLite.getExperimentId());
              expIdDistinctVariableMap.put(expLite.getExperimentId(), distinctVariableCount);
            }
          }
          if (distinctVariableCount < batchSize ) { 
            log.info("distinct exp id version ct " + (ct++) + " exp id"+ expLite.getExperimentId() + "expVersion" + expLite.getExperimentVersion() + "expGroup" + expLite.getExperimentGroupName()  + "distinct variable name count" + distinctVariableCount);
              
            while (true) {
              allEventsForThisExptVersion = sqlMigDaoImpl.getSingleBatchUnprocessedEvent(conn, erroredExperimentIds, unprocessedEventRecordQuery, expLite.getExperimentId(), expLite.getExperimentVersion(), expLite.getExperimentGroupName(), batchSize) ;
              if (allEventsForThisExptVersion == null || allEventsForThisExptVersion.size() == 0 ) { 
                log.info("All Event records for this Expt version have EGVM Id.............................." + expLite.getExperimentId() + "--" + expLite.getExperimentVersion());
                break;
              } else {
                sqlMigDaoImpl.processOlderVersionsAndAnonUsersInEventTable(conn, erroredExperimentIds, allEventsForThisExptVersion, expLiteStatus == 20);
              } 
            }
            expIdVersionDaoImpl.updateExperimentIdVersionGroupNameStatus(expLite.getExperimentId(), expLite.getExperimentVersion(), expLite.getExperimentGroupName(), 3);
          } else {
            expIdVersionDaoImpl.updateExperimentIdVersionGroupNameStatus(expLite.getExperimentId(), expLite.getExperimentVersion(), expLite.getExperimentGroupName(), 20 );
          }
        }
        returnString += "OlderVersions And AnonUsers Updates in Events and Outputs table Done. Job complete.";
      } catch (SQLException e) {
        returnString += "Failed to update OlderVersions And AnonUsers data in events table. Restart job";
        throw new SQLException(returnString, e);
      } finally {
        try {
          if (conn != null) {
            conn.close();
          }
        } catch (SQLException e) {
          log.warning("Exception in finally block" + ExceptionUtil.getStackTraceAsString(e));
        }
      }
      return returnString; 
    }
}

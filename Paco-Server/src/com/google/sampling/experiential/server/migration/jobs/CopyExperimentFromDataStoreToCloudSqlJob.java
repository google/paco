package com.google.sampling.experiential.server.migration.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.dao.CSEventOutputDao;
import com.google.sampling.experiential.dao.CSOutputDao;
import com.google.sampling.experiential.dao.CSTempExperimentDefinitionDao;
import com.google.sampling.experiential.dao.CSTempExperimentIdVersionGroupNameDao;
import com.google.sampling.experiential.dao.dataaccess.ExperimentLite;
import com.google.sampling.experiential.dao.impl.CSEventOutputDaoImpl;
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
          sqlMigDaoImpl.copyExperimentDeleteEventsAndOutputsForDeletedExperiments();
          log.info("------------------------------------------------Step 1 End------------------------------------------------");
          returnString += "delete events of deleted experiments Done. Step1 complete.";
//          returnString = "All Done";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to delete events of deleted experiments. Restart job from step1";
          throw new SQLException(returnString, e);
        }
      }
      
      if ( (cursor != null && cursor.equalsIgnoreCase("step2"))) {
        try {
          log.info("------------------------------------------------Step 2 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentCreateEVGMRecordsForAllExperiments();
          // egn status 1
          log.info("------------------------------------------------Step 2 End------------------------------------------------");
          returnString += "create evgm record for earlier versions Done. Step2 complete.";
//          returnString = "All Done";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to create evgm records for earlier versions of experiments. Restart job from step2";
          throw new SQLException(returnString, e);
        }
      }
    
      if ( (cursor != null && cursor.equalsIgnoreCase("step3"))) {
        try {
          log.info("------------------------------------------------Step 3 Begin------------------------------------------------");
          returnString = updateEventAndOutputTable(2, 1000, unprocessedEventRecordQuery);
          log.info("------------------------------------------------Step 3 End------------------------------------------------");
          doAll = true;
        } catch (Exception e) {
          returnString += "Failed to update event and output table . Restart job from step3";
          throw new SQLException(returnString, e);
        }
      }
      if ( (cursor != null && cursor.equalsIgnoreCase("step4"))) {
        try {
          log.info("------------------------------------------------Step 4 Begin------------------------------------------------");
          returnString = updateEventAndOutputTable(20, 100, unprocessedEventRecordQuery);
          log.info("------------------------------------------------Step 4 End------------------------------------------------");
          doAll = true;
        } catch (Exception e) {
          returnString += "Failed to update event and output table . Restart job from step4";
          throw new SQLException(returnString, e);
        }
       returnString = "All Done";
      }
      
      if ( (cursor != null && cursor.equalsIgnoreCase("step5"))) {
        try {
          log.info("------------------------------------------------Step 5 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentMarkExperimentsForPivotTable();
          // egn status 2
          log.info("------------------------------------------------Step 5 End------------------------------------------------");
          returnString += "populate pivot table helper Done. Step5 complete.";
          returnString = "All Done";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to populate pivot table helper. Restart job from step5";
          throw new SQLException(returnString, e);
        }
      }
      if ( (cursor != null && cursor.equalsIgnoreCase("step6"))) {
        try {
          log.info("------------------------------------------------Step 6 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentPopulatePivotTableForAllExperiments();
          log.info("------------------------------------------------Step 6 End------------------------------------------------");
          returnString += "populate pivot table helper Done. Step6 complete.";
          returnString = "All Done";
    //      doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to populate pivot table helper. Restart job from step6";
          throw new SQLException(returnString, e);
        }
      }

      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step7"))) {
        try {
          log.info("------------------------------------------------Step 7 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentRenameOldEventColumns();
          log.info("------------------------------------------------Step 7 End------------------------------------------------");
          returnString = "All Done";
        } catch (SQLException e) {
          returnString += "Failed to rename event columns to avoid ambiguity. Restart job from step7";
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
      CSEventOutputDao eventOutputDaoImpl = new CSEventOutputDaoImpl();
      Connection conn = null;
      String returnString = null;
      try {
        List<Long> erroredExperimentIds = expDefDaoImpl.getErroredExperimentDefinition();
        List<EventDAO> allEventsForThisExptVersion = null;
        Map<Long, Long> expIdDistinctVariableMap = Maps.newHashMap();
        int ct = 0 ;
        Long distinctVariableCount = 0L;
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        List<ExperimentLite> expLites = expIdVersionDaoImpl.getAllExperimentLiteOfStatus(expLiteStatus);
        List<Long> experimentIdsWithResetDups = Lists.newArrayList();
        for (ExperimentLite expLite : expLites) {
          if (expLiteStatus == 2) {
            distinctVariableCount = expIdDistinctVariableMap.get(expLite.getExperimentId());
            if ( distinctVariableCount == null) { 
              distinctVariableCount = outputDaoImpl.getDistinctOutputCount(expLite.getExperimentId());
              expIdDistinctVariableMap.put(expLite.getExperimentId(), distinctVariableCount);
            }
          } else {
            // identify dup variables in expt of status 20
            if (!experimentIdsWithResetDups.contains(expLite.getExperimentId())) {
              eventOutputDaoImpl.resetDupCounterForVariableNames(expLite.getExperimentId());
              experimentIdsWithResetDups.add(expLite.getExperimentId());
            }
          }
          if (distinctVariableCount < batchSize ) { 
            log.info("distinct exp id version ct " + (ct++) + " exp id"+ expLite.getExperimentId() + "expVersion" + expLite.getExperimentVersion() + "expGroup" + expLite.getExperimentGroupName()  + "distinct variable name " + distinctVariableCount);
              
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
      }
      return returnString; 
    }
}

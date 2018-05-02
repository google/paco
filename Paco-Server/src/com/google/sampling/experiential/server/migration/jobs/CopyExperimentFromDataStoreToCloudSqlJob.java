package com.google.sampling.experiential.server.migration.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.common.collect.Maps;
import com.google.sampling.experiential.dao.CSExperimentDefinitionDao;
import com.google.sampling.experiential.dao.CSExperimentIdVersionDao;
import com.google.sampling.experiential.dao.CSOutputDao;
import com.google.sampling.experiential.dao.dataaccess.ExperimentLite;
import com.google.sampling.experiential.dao.impl.CSExperimentDefinitionDaoImpl;
import com.google.sampling.experiential.dao.impl.CSExperimentIdVersionDaoImpl;
import com.google.sampling.experiential.dao.impl.CSOutputDaoImpl;
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
      CSExperimentDefinitionDao expDefDaoImpl = new CSExperimentDefinitionDaoImpl();
      CSExperimentIdVersionDao expIdVersionDaoImpl = new CSExperimentIdVersionDaoImpl();
      CSOutputDao outputDaoImpl = new CSOutputDaoImpl();
      Connection conn = null;
      String unprocessedEventRecordQuery = QueryConstants.UNPROCESSED_EVENT_QUERY.toString();
//      String unprocessedOutputRecordQuery = QueryConstants.UNPROCESSED_OUTPUT_QUERY.toString();
      if (cursor == null) {
        doAll = true;
      }
      // step 1
      // create tables - input_types, expt, groups, inputs, input_collection, choice_collection, expt_version_group_inputcoll_mapping, extern_string
      // step 2
      // read all expt json in datastore.
      // for each json, check if version is 1 or if higher pull the most recent experiment version
      // if no history -> push expt data, then groups data, then create input collection while adding inputs data.
      // if inputs response type is list/likert, then create choice collection while adding choices
      // 
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step1"))) {
        try {
          log.info("------------------------------------------------Step 1 Begin------------------------------------------------");
          sqlMigDaoImpl.updateEventTableExperimentVersionAndGroupNameNull();
          log.info("------------------------------------------------Step 1 End------------------------------------------------");
          returnString += "Modify experiment version from null to 0 Done. Step1 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to modify experiment version from null to 0. Restart job from step1";
          throw new SQLException(returnString, e);
        }
      }
        
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step2"))) {
        try {
            log.info("------------------------------------------------Step 2 Begin------------------------------------------------");
            sqlMigDaoImpl.copyExperimentCreateTables();
            sqlMigDaoImpl.anonymizeParticipantsCreateTables();
            log.info("------------------------------------------------Step 2 End------------------------------------------------");
            returnString = "Created new tables. Step2 complete.";
            doAll = true;
          } catch (SQLException e) {
            returnString = "Create new tables failed. Restart job from step2";
            throw new SQLException(returnString, e);
          }
      }
      // create tables needs to happen first, as backup table has to be created first
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step3"))) {
        try {
          log.info("------------------------------------------------Step 3 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentTakeBackupInCloudSql();
          log.info("------------------------------------------------Step 3 End------------------------------------------------");
          returnString += "Backup of Datastore experiments Done. Step3 complete.";
//          returnString = "All Done";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Backup of datastore experiments. Restart job from step3";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step4"))) {
        try {
          log.info("------------------------------------------------Step 4 Begin------------------------------------------------");
          sqlMigDaoImpl.addModificationsToExistingTables();
          log.info("------------------------------------------------Step 4 End------------------------------------------------");
          returnString += "Modified existing tables. Step4 complete.";
          doAll =  true;
        } catch (SQLException e) {
          returnString += "Failed to modify existing tables. Restart job from step4";
          throw new SQLException(returnString, e);
        }
      }
//       insert in exp defintion, only the source first
//    create tables needs to happen first, bcoz backup table has to be created first
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step5"))) {
        try {
          log.info("------------------------------------------------Step 5 Begin------------------------------------------------");
          sqlMigDaoImpl.insertIntoExperimentDefinition();
          log.info("------------------------------------------------Step 5 End------------------------------------------------");
          returnString += "Insert to ExperimentDefintion from Backup Done. Step5 complete.";
//          returnString = "All Done";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Insert to ExperimentDefintion from Backup. Restart job from step5";
          throw new SQLException(returnString, e);
        }
      }  
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step5b"))) {
        try {
          log.info("------------------------------------------------Step 5b Begin------------------------------------------------");
          sqlMigDaoImpl.removeUnwantedPredefinedExperiments();
          log.info("------------------------------------------------Step 5b End------------------------------------------------");
          returnString += "Remove unwanted predefined Experiments before processing. Step 5b complete";
          doAll =  true;
        } catch (SQLException e) {
          returnString += "Failed to remove predefined Experiments. Restart job from step5b";
          throw new SQLException(returnString, e);
        }
      }
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step6"))) {
        try {
          log.info("------------------------------------------------Step 6 Begin------------------------------------------------");
          sqlMigDaoImpl.insertPredefinedRecords();
          log.info("------------------------------------------------Step 6 End------------------------------------------------");
          returnString += "Inserted records to data types. Step 6 complete";
          doAll =  true;
        } catch (SQLException e) {
          returnString += "Failed to insert data types. Restart job from step6";
          throw new SQLException(returnString, e);
        }
      }
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step7"))) {
        try {
          log.info("------------------------------------------------Step 7 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentSplitGroupsAndPersist();
          log.info("------------------------------------------------Step 7 End------------------------------------------------");
          returnString += "Split groups and persist Done. Step7 complete.";
//          returnString = "All Done";
          doAll = true;
        } catch (Exception e) {
          returnString += "Failed to split groups and persist. Restart job from step7";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step8"))) {
        try {
          log.info("------------------------------------------------Step 8 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentPopulateExperimentBundleTables();
          log.info("------------------------------------------------Step 8 End------------------------------------------------");
          returnString += "copy experiment data from ds to cs bundle Done. Step8 complete.";
//          returnString = "All Done";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to populate bundle tables. Restart job from step8";
          throw new SQLException(returnString, e);
        }
      }
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step9"))) {
        try {
          log.info("------------------------------------------------Step 9 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentPopulateDistinctExperimentIdVersionAndGroupName();
          log.info("------------------------------------------------Step 9 End------------------------------------------------");
          returnString += "copy distinct exp id and version from events Done. Step9 complete.";
//          returnString = "All Done";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to populate bundle tables. Restart job from step9";
          throw new SQLException(returnString, e);
        }
      }
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step10"))) {
        try {
          log.info("------------------------------------------------Step 10 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentDeleteEventsAndOutputsForDeletedExperiments();
          log.info("------------------------------------------------Step 10 End------------------------------------------------");
          returnString += "delete events of deleted experiments Done. Step10 complete.";
//          returnString = "All Done";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to delete events of deleted experiments. Restart job from step10";
          throw new SQLException(returnString, e);
        }
      }
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step11"))) {
        try {
          log.info("------------------------------------------------Step 11 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentCreateEVGMRecordsForAllExperiments();
          // egn status 1
          log.info("------------------------------------------------Step 11 End------------------------------------------------");
          returnString += "create evgm record for earlier versions Done. Step11 complete.";
//          returnString = "All Done";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to create evgm records for earlier versions of experiments. Restart job from step11";
          throw new SQLException(returnString, e);
        }
      }
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step12"))) {
        try {
          log.info("------------------------------------------------Step 12 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentChangeGroupNameOfEventsWithPredefinedInputs();
          // egn status 2
          log.info("------------------------------------------------Step 12 End------------------------------------------------");
          returnString += "group name changed for events with predefined inputs Done. Step12 complete.";
          returnString = "All Done";
//          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to change group name for events with predefined inputs. Restart job from step12";
          throw new SQLException(returnString, e);
        }
      }
    
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step14"))) {
        try {
          List<Long> erroredExperimentIds = expDefDaoImpl.getErroredExperimentDefinition();
          List<EventDAO> allEventsForThisExptVersion = null;
          Map<Long, Long> expIdDistinctVariableMap = Maps.newHashMap();
          int ct = 0 ;
          Long distinctVariableCount = 0L;
          log.info("------------------------------------------------Step 14 Begin------------------------------------------------");
          conn = CloudSQLConnectionManager.getInstance().getConnection();
          List<ExperimentLite> expLites = expIdVersionDaoImpl.getAllExperimentLiteOfStatus(2);
          for (ExperimentLite expLite : expLites) {
            distinctVariableCount = expIdDistinctVariableMap.get(expLite.getExperimentId());
            if ( distinctVariableCount == null) { 
              distinctVariableCount = outputDaoImpl.getDistinctOutputCount(expLite.getExperimentId());
              expIdDistinctVariableMap.put(expLite.getExperimentId(), distinctVariableCount);
            } 
            if (distinctVariableCount < 1000 ) { 
              log.info("distinct exp id version ct " + (ct++) + " exp id"+ expLite.getExperimentId() + "expVersion" + expLite.getExperimentVersion() + "expGroup" + expLite.getExperimentGroupName()  + "distinct variable name " + distinctVariableCount);
                
              while (true) {
                  allEventsForThisExptVersion = sqlMigDaoImpl.getSingleBatchUnprocessedEvent(conn, erroredExperimentIds, unprocessedEventRecordQuery, expLite.getExperimentId(), expLite.getExperimentVersion(), expLite.getExperimentGroupName()) ;
                  if (allEventsForThisExptVersion == null || allEventsForThisExptVersion.size() == 0 ) { 
                    log.info("All Event records for this Expt version have EGVM Id.............................." + expLite.getExperimentId() + "--" + expLite.getExperimentVersion());
                    break;
                  } else {
                    sqlMigDaoImpl.processOlderVersionsAndAnonUsersInEventTable(conn, erroredExperimentIds, allEventsForThisExptVersion);
                  } 
              }
              expIdVersionDaoImpl.updateExperimentIdGroupNameStatus(expLite.getExperimentId(), expLite.getExperimentVersion(), expLite.getExperimentGroupName(), 3);
            } else {
              expIdVersionDaoImpl.updateExperimentIdGroupNameStatus(expLite.getExperimentId(), expLite.getExperimentVersion(), expLite.getExperimentGroupName(), 20 );
//              log.info("b4 remove" + expLites.size());
//              expLites.remove(expLite);
//              log.info("after remove" + expLites.size());
              
            }
          }
          log.info("------------------------------------------------Step 14 End------------------------------------------------");
          returnString += "OlderVersions And AnonUsers Updates in Events and Outputs table Done. Step14 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to update OlderVersions And AnonUsers data in events table. Restart job from step14";
          throw new SQLException(returnString, e);
        } 
       returnString = "All Done";
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step15"))) {
        try {
          log.info("------------------------------------------------Step 15 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentRenameOldEventColumns();
          log.info("------------------------------------------------Step 15 End------------------------------------------------");
          returnString = "All Done";
        } catch (SQLException e) {
          returnString += "Failed to rename event columns to avoid ambiguity. Restart job from step15";
          throw new SQLException(returnString, e);
        }
      }
      return returnString;
    }
}

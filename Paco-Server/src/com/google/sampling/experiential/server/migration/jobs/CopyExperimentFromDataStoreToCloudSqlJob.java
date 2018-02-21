package com.google.sampling.experiential.server.migration.jobs;

import java.sql.SQLException;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.migration.MigrationJob;
import com.google.sampling.experiential.server.migration.dao.CopyExperimentMigrationDao;
import com.google.sampling.experiential.server.migration.dao.impl.CopyExperimentMigrationDaoImpl;

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
    
    private String copyExperimentFromDataStoreToCloudSql(String cursor) throws SQLException {
      String returnString = "";
      Boolean doAll = false;
      CopyExperimentMigrationDao sqlMigDaoImpl = new CopyExperimentMigrationDaoImpl();
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
            sqlMigDaoImpl.copyExperimentCreateTables();
            sqlMigDaoImpl.anonymizeParticipantsCreateTables();
            log.info("------------------------------------------------Step 1 End------------------------------------------------");
            returnString = "Created new tables. Step1 complete.";
            doAll = true;
          } catch (SQLException e) {
            returnString = "Create new tables failed. Restart job";
            throw new SQLException(returnString, e);
          }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step2"))) {
        try {
          log.info("------------------------------------------------Step 2 Begin------------------------------------------------");
          sqlMigDaoImpl.addModificationsToExistingTables();
          log.info("------------------------------------------------Step 2 End------------------------------------------------");
          returnString += "Modified existing tables. Step2 complete.";
          doAll =  true;
        } catch (SQLException e) {
          returnString += "Failed to modify existing tables. Restart job from step2";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step3"))) {
        try {
          log.info("------------------------------------------------Step 3 Begin------------------------------------------------");
          sqlMigDaoImpl.insertPredefinedRecords();
          log.info("------------------------------------------------Step 3 End------------------------------------------------");
          returnString += "Inserted records to data types. Step 3 complete";
          doAll =  true;
        } catch (SQLException e) {
          returnString += "Failed to insert data types. Restart job from step3";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step4"))) {
        try {
          log.info("------------------------------------------------Step 4 Begin------------------------------------------------");
          sqlMigDaoImpl.updateEventTableGroupNameNull();
          log.info("------------------------------------------------Step 4 End------------------------------------------------");
          returnString += "Modify experiment name from null to System Done. Step4 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to modify experiment name from null to System. Restart job from step4";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step5"))) {
        try {
          log.info("------------------------------------------------Step 5 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentSplitGroupsAndPersist();
          log.info("------------------------------------------------Step 5 End------------------------------------------------");
          returnString += "Split groups and persist Done. Step5 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to split groups and persist. Restart job from step5";
          throw new SQLException(returnString, e);
        }
      }
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step6"))) {
        try {
          log.info("------------------------------------------------Step 6 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentMigrateFromDataStoreToCloudSql();
          log.info("------------------------------------------------Step 6 End------------------------------------------------");
          returnString += "copy experiment data from ds to cs bundle Done. Step6 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to populate bundle tables. Restart job from step6";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step7"))) {
        try {
          log.info("------------------------------------------------Step 7 Begin------------------------------------------------");
          sqlMigDaoImpl.populatePivotTableHelper();
          log.info("------------------------------------------------Step 7 End------------------------------------------------");
          returnString += "populate pivot table helper Done. Step7 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to populate pivot tables. Restart job from step7";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step8"))) {
        try {
          log.info("------------------------------------------------Step 8 Begin------------------------------------------------");
          sqlMigDaoImpl.processPivotTableHelper();
          log.info("------------------------------------------------Step 8 End------------------------------------------------");
          returnString += "process pivot table helper Done. Step8 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to process pivot table. Restart job from step8";
          throw new SQLException(returnString, e);
        }
      } 
    
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step9"))) {
        try {
          log.info("------------------------------------------------Step 9 Begin------------------------------------------------");
          while (true) {
            boolean insertedToPivotHelperTable = sqlMigDaoImpl.processOlderVersionsAndAnonUsersInEventTable();
            if (insertedToPivotHelperTable) {
              sqlMigDaoImpl.processPivotTableHelper();
            } 
            else {
              break;
            }
          }
          log.info("------------------------------------------------Step 9 End------------------------------------------------");
          returnString += "OlderVersions And AnonUsers Updates in Events and Outputs table Done. Step8 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to update OlderVersions And AnonUsers data in events table. Restart job from step9";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step10"))) {
        try {
          log.info("------------------------------------------------Step 10 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentRenameOldEventColumns();
          log.info("------------------------------------------------Step 10 End------------------------------------------------");
          returnString = "All Done";
        } catch (SQLException e) {
          returnString += "Failed to rename event columns to avoid ambiguity. Restart job from step10";
          throw new SQLException(returnString, e);
        }
      }
      
      

      return returnString;
    }
}

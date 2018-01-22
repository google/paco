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
            sqlMigDaoImpl.copyExperimentCreateTables();
            returnString = "Created new tables. Step1 complete.";
            doAll = true;
          } catch (SQLException e) {
            returnString = "Create new tables failed. Restart job";
            throw new SQLException(returnString, e);
          }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step2"))) {
        try {
          sqlMigDaoImpl.addModificationsToExistingTables();
          returnString += "Modified existing tables. Step2 complete.";
          doAll =  true;
        } catch (SQLException e) {
          returnString += "Failed to modify existing tables. Restart job from step2";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step3"))) {
        try {
          sqlMigDaoImpl.addDataTypes();
//          returnString += "insert records to data types";
          returnString = "All Done";
          doAll =  true;
        } catch (SQLException e) {
          returnString += "Failed to insert data types. Restart job from step3";
          throw new SQLException(returnString, e);
        }
      }
//      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step4"))) {
//        try {
//          sqlMigDaoImpl.anonymizeParticipantsMigrateToUserAndExptUser();
//          returnString += "migrateToUserAndExptUser Done. Step4 complete.";
//          doAll = true;
//        } catch (SQLException e) {
//          returnString += "Failed to migrate to User and Expt tables. Restart job from step4";
//          throw new SQLException(returnString, e);
//        }
//      }
//      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step5"))) {
//        try {
//          sqlMigDaoImpl.anonymizeParticipantsModifyExperimentNameFromNullToBlank();
//          returnString += "Modify experiment name from null to blank Done. Step5 complete.";
//          doAll = true;
//        } catch (SQLException e) {
//          returnString += "Failed to modify experiment name from null to blank. Restart job from step5";
//          throw new SQLException(returnString, e);
//        }
//      }
//      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step6"))) {
//        try {
//          sqlMigDaoImpl.anonymizeParticipantsMigrateToExperimentLookupTracking();
//          returnString += "migrateToExperimentLookupTracking Done. Step6 complete.";
//          doAll = true;
//        } catch (SQLException e) {
//          returnString += "Failed to migrate to Expt Lookup Tracking tables. Restart job from step6";
//          throw new SQLException(returnString, e);
//        }
//      }
//      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step7"))) {
//        try {
//          sqlMigDaoImpl.anonymizeParticipantsMigrateToExperimentLookup();
//          returnString += "migrateToExperimentLookup Done. Step7 complete.";
//          doAll = true;
//        } catch (SQLException e) {
//          returnString += "Failed to migrate to Experimentlookup tables. Restart job from step7";
//          throw new SQLException(returnString, e);
//        }
//      }
//      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step8"))) {
//        try {
//          sqlMigDaoImpl.anonymizeParticipantsUpdateEventWhoAndLookupIdByTracking();
//          returnString += "update event who and lookup id Done. Step8 complete.";
//          doAll = true;
//        } catch (Exception ex)  {
//          log.warning(ExceptionUtil.getStackTraceAsString(ex));
//          returnString += "Ex:Failed to update event who and lookup id . Restart job from step8";
//          throw ex;
//        }
//      }
//      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step9"))) {
//        try {
//          sqlMigDaoImpl.anonymizeParticipantsUpdateEventWhoAndLookupIdSerially();
//          returnString += "update event on failed ones Done. Step9 complete.";
//          doAll = true;
//        } catch (SQLException e) {
//          returnString += "Failed to update event on failed ones. Restart job from step9";
//          log.warning(ExceptionUtil.getStackTraceAsString(e));
//          throw new SQLException(returnString, e);
//        }
//      }
//      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step10"))) {
//        try {
//          sqlMigDaoImpl.anonymizeParticipantsRenameOldEventColumns();
//          returnString = "All Done";
//        } catch (SQLException e) {
//          returnString += "Failed to rename event columns. Restart job from step10";
//          log.warning(ExceptionUtil.getStackTraceAsString(e));
//          throw new SQLException(returnString, e);
//        }
//      }
      return returnString;
    }
}

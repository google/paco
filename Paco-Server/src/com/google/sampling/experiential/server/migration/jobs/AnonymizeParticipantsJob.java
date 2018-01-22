package com.google.sampling.experiential.server.migration.jobs;

import java.sql.SQLException;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.migration.MigrationJob;
import com.google.sampling.experiential.server.migration.dao.AnonymizeParticipantsMigrationDao;
import com.google.sampling.experiential.server.migration.dao.impl.AnonymizeParticipantsMigrationDaoImpl;

public class AnonymizeParticipantsJob implements MigrationJob {

    public static final Logger log = Logger.getLogger(AnonymizeParticipantsJob.class.getName());

    @Override
    public boolean doMigration(String cursor, DateTime startTime, DateTime endTime) {
      Boolean success = false;
      try {
        String successMsg = anonymizeParticipants(cursor);
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
    
    private String anonymizeParticipants(String cursor) throws SQLException {
      String returnString = "";
      Boolean doAll = false;
      AnonymizeParticipantsMigrationDao sqlMigDaoImpl = new AnonymizeParticipantsMigrationDaoImpl();
      if (cursor == null) {
        doAll = true;
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step1"))) {
        try {
            sqlMigDaoImpl.anonymizeParticipantsCreateTables();
            returnString = "Created new tables. Step1 complete.";
            doAll = true;
          } catch (SQLException e) {
            returnString = "Create new tables failed. Restart job";
            throw new SQLException(returnString, e);
          }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step2"))) {
        try {
          sqlMigDaoImpl.anonymizeParticipantsAddColumnToEventTable();
          returnString += "Added lookup column to event table. Step2 complete.";
          doAll =  true;
        } catch (SQLException e) {
          returnString += "Failed to add lookup column. Restart job from step2";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step3"))) {
        try {
          sqlMigDaoImpl.anonymizeParticipantsTakeBackupEventIdWho();
          returnString += "Created backup of id, who in event table. Step3 complete.";
          doAll =  true;
        } catch (SQLException e) {
          returnString += "Failed to create backup of id, who in event table. Restart job from step3";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step4"))) {
        try {
          sqlMigDaoImpl.anonymizeParticipantsMigrateToUserAndExptUser();
          returnString += "migrateToUserAndExptUser Done. Step4 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to migrate to User and Expt tables. Restart job from step4";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step5"))) {
        try {
          sqlMigDaoImpl.anonymizeParticipantsModifyExperimentNameFromNullToBlank();
          returnString += "Modify experiment name from null to blank Done. Step5 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to modify experiment name from null to blank. Restart job from step5";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step6"))) {
        try {
          sqlMigDaoImpl.anonymizeParticipantsMigrateToExperimentLookupTracking();
          returnString += "migrateToExperimentLookupTracking Done. Step6 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to migrate to Expt Lookup Tracking tables. Restart job from step6";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step7"))) {
        try {
          sqlMigDaoImpl.anonymizeParticipantsMigrateToExperimentLookup();
          returnString += "migrateToExperimentLookup Done. Step7 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to migrate to Experimentlookup tables. Restart job from step7";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step8"))) {
        try {
          sqlMigDaoImpl.anonymizeParticipantsUpdateEventWhoAndLookupIdByTracking();
          returnString += "update event who and lookup id Done. Step8 complete.";
          doAll = true;
        } catch (Exception ex)  {
          log.warning(ExceptionUtil.getStackTraceAsString(ex));
          returnString += "Ex:Failed to update event who and lookup id . Restart job from step8";
          throw ex;
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step9"))) {
        try {
          sqlMigDaoImpl.anonymizeParticipantsUpdateEventWhoAndLookupIdSerially();
          returnString += "update event on failed ones Done. Step9 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to update event on failed ones. Restart job from step9";
          log.warning(ExceptionUtil.getStackTraceAsString(e));
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step10"))) {
        try {
          sqlMigDaoImpl.anonymizeParticipantsRenameOldEventColumns();
          returnString = "All Done";
        } catch (SQLException e) {
          returnString += "Failed to rename event columns. Restart job from step10";
          log.warning(ExceptionUtil.getStackTraceAsString(e));
          throw new SQLException(returnString, e);
        }
      }
      return returnString;
    }
}

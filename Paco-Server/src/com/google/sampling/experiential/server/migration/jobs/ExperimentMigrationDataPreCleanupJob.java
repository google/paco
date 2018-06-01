package com.google.sampling.experiential.server.migration.jobs;

import java.sql.SQLException;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.QueryConstants;
import com.google.sampling.experiential.server.migration.MigrationJob;
import com.google.sampling.experiential.server.migration.dao.CloudSQLMigrationDao;
import com.google.sampling.experiential.server.migration.dao.CopyExperimentMigrationDao;
import com.google.sampling.experiential.server.migration.dao.impl.CloudSQLMigrationDaoImpl;
import com.google.sampling.experiential.server.migration.dao.impl.CopyExperimentMigrationDaoImpl;

public class ExperimentMigrationDataPreCleanupJob implements MigrationJob {

    public static final Logger log = Logger.getLogger(ExperimentMigrationDataPreCleanupJob.class.getName());

    @Override
    public boolean doMigration(String cursor, DateTime startTime, DateTime endTime) {
      Boolean success = false;
      try {
        String successMsg = cleanupDataBeforeMigration(cursor);
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
    
    private String cleanupDataBeforeMigration(String cursor) throws Exception {
      String returnString = "";
      Boolean doAll = false;
      CloudSQLMigrationDao csMigDaoImpl = new CloudSQLMigrationDaoImpl();
      CopyExperimentMigrationDao sqlMigDaoImpl = new CopyExperimentMigrationDaoImpl();
      if (cursor == null) {
        doAll = true;
      }
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step1"))) {
        try {
          log.info("------------------------------------------------Step 1 Begin------------------------------------------------");
//          csMigDaoImpl.persistStreamingStart(new DateTime());
          sqlMigDaoImpl.dataCleanupUpdateEventTableExperimentVersionAndGroupNameNull();
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
          sqlMigDaoImpl.dataCleanupRemoveUnwantedEventAndOutputsPredefinedExperiments();
          log.info("------------------------------------------------Step 2 End------------------------------------------------");
          returnString += "Remove unwanted predefined Experiments before processing. Step 2 complete";
          doAll =  true;
        } catch (SQLException e) {
          returnString += "Failed to remove predefined Experiments. Restart job from step2";
          throw new SQLException(returnString, e);
        }
      }
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step3"))) {
        try {
          log.info("------------------------------------------------Step 3 Begin------------------------------------------------");
          sqlMigDaoImpl.dataCleanupMakeDBChanges();
          log.info("------------------------------------------------Step 3 End------------------------------------------------");
          returnString += "Make db changes Done. Step3 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to make DB changes. Restart job from step3";
          throw new SQLException(returnString, e);
        }
      }
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step4"))) {
        String query = QueryConstants.GET_EXPERIMENT_IDS_FROM_EVENTS_WITH_DUP_INPUTS.toString();
        try {
          log.info("------------------------------------------------Step 4 Begin------------------------------------------------");
          sqlMigDaoImpl.dataCleanupChangeDupCounterAloneOnVariableNames(query);
          log.info("------------------------------------------------Step 4 End------------------------------------------------");
          returnString += "Modify event with DUP input variable names Done. Step4 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to modify event with DUP variable names. Restart job from step4";
          throw new SQLException(returnString, e);
        }
      }
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step5"))) {
        try {
          log.info("------------------------------------------------Step 5 Begin------------------------------------------------");
          sqlMigDaoImpl.experimentSplitCreateTables();
          sqlMigDaoImpl.experimentSplitAnonymizeParticipantsCreateTables();
          sqlMigDaoImpl.experimentSplitAddModificationsToExistingTables();
          log.info("------------------------------------------------Step 5 End------------------------------------------------");
          returnString += "Created new tables and altered existing tables. Step5 complete.";
//          returnString = "All Done";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Create new tables and altering existing tables failed. Restart job from step5";
          throw new SQLException(returnString, e);
        }
      }
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step6"))) {
        try {
          log.info("------------------------------------------------Step 6 Begin------------------------------------------------");
          sqlMigDaoImpl.experimentSplitInsertPredefinedRecords();
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
          sqlMigDaoImpl.copyExperimentPopulateDistinctExperimentIdVersionAndGroupName();
          log.info("------------------------------------------------Step 7 End------------------------------------------------");
          returnString += "copy distinct exp id and version from events Done. Step7 complete.";
//          returnString = "All Done";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to populate bundle tables. Restart job from step7";
          throw new SQLException(returnString, e);
        }
      }
    
     
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step8"))) {
        try {
          log.info("------------------------------------------------Step 8 Begin------------------------------------------------");
          sqlMigDaoImpl.copyExperimentChangeGroupNameOfEventsWithPredefinedInputs();
          // egn status 2
          log.info("------------------------------------------------Step 8 End------------------------------------------------");
          returnString += "group name changed for events with predefined inputs Done. Step8 complete.";
          returnString = "All Done";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to change group name for events with predefined inputs. Restart job from step8";
          throw new SQLException(returnString, e);
        }
      }
      return returnString;
    }
}

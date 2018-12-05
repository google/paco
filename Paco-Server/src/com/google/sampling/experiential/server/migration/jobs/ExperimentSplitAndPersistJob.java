package com.google.sampling.experiential.server.migration.jobs;

import java.sql.SQLException;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.migration.MigrationJob;
import com.google.sampling.experiential.server.migration.dao.CopyExperimentMigrationDao;
import com.google.sampling.experiential.server.migration.dao.impl.CopyExperimentMigrationDaoImpl;

public class ExperimentSplitAndPersistJob implements MigrationJob {

    public static final Logger log = Logger.getLogger(ExperimentSplitAndPersistJob.class.getName());

    @Override
    public boolean doMigration(String cursor, DateTime startTime, DateTime endTime) {
      Boolean success = false;
      try {
        String successMsg = splitExperimentAndPersist(cursor);
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
    
    private String splitExperimentAndPersist(String cursor) throws Exception {
      String returnString = "";
      Boolean doAll = false;
      CopyExperimentMigrationDao sqlMigDaoImpl = new CopyExperimentMigrationDaoImpl();
      if (cursor == null) {
        doAll = true;
      }  
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step1"))) {
        try {
          log.info("------------------------------------------------Step 1 Begin------------------------------------------------");
          sqlMigDaoImpl.experimentSplitTakeBackupInCloudSql();
          log.info("------------------------------------------------Step 1 End------------------------------------------------");
          returnString = "Backup of Datastore experiments Done. Step1 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString = "Backup of datastore experiments. Restart job from step1";
          throw new SQLException(returnString, e);
        }
      }
       
      // create tables needs to happen first, bcoz backup table has to be created first
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step2"))) {
        try {
          log.info("------------------------------------------------Step 2 Begin------------------------------------------------");
          // exp def status will be 0
          sqlMigDaoImpl.experimentSplitInsertIntoExperimentDefinition();
          sqlMigDaoImpl.experimentSplitRemoveUnwantedExperimentJsonFromExperimentDefinition();
          log.info("------------------------------------------------Step 2 End------------------------------------------------");
          returnString += "Insert to ExperimentDefintion from Backup Done. Step2 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Insert to ExperimentDefintion from Backup. Restart job from step2";
          throw new SQLException(returnString, e);
        }
      }  
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step3"))) {
        try {
          log.info("------------------------------------------------Step 3 Begin------------------------------------------------");
          // picks exp def mig status 0
          sqlMigDaoImpl.experimentSplitGroupsAndPersist();
          // on success, updates exp def mig status 1
          log.info("------------------------------------------------Step 3 End------------------------------------------------");
          returnString += "Split groups and persist Done. Step3 complete.";
          doAll = true;
        } catch (Exception e) {
          returnString += "Failed to split groups and persist. Restart job from step3";
          throw new SQLException(returnString, e);
        }
      }    
      
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step4"))) {
        try {
          log.info("------------------------------------------------Step 4 Begin------------------------------------------------");
          // picks up exp def mig status 1
          sqlMigDaoImpl.experimentSplitPopulateExperimentBundleTables();
          // on success, updates exp def mig status 2
          log.info("------------------------------------------------Step 4 End------------------------------------------------");
          returnString += "All Done";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to populate bundle tables. Restart job from step4";
          throw new SQLException(returnString, e);
        }
      }
      return returnString;
    }
}

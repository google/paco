package com.google.sampling.experiential.server.migration.jobs;

import java.sql.SQLException;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.migration.MigrationJob;
import com.google.sampling.experiential.server.migration.dao.ExperimentMigrationVerificationDao;
import com.google.sampling.experiential.server.migration.dao.impl.ExperimentMigrationVerificationDaoImpl;

public class EVGMigrationVerificationJob implements MigrationJob {

  public static final Logger log = Logger.getLogger(EVGMigrationVerificationJob.class.getName());

  @Override
  public boolean doMigration(String cursor, DateTime startTime, DateTime endTime) {
    Boolean success = false;
    try {
      String successMsg = evgMigrationVerification(cursor);
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
  
  private String evgMigrationVerification(String cursor) throws Exception {
    String returnString = "";
    Boolean doAll = false;
    ExperimentMigrationVerificationDao sqlMigDaoImpl = new ExperimentMigrationVerificationDaoImpl();
    
    if (cursor == null) {
      doAll = true;
    }  
    if (doAll || (cursor != null && cursor.equalsIgnoreCase("step1"))) {
      try {
        log.info("------------------------------------------------Step 1 Begin------------------------------------------------");
        sqlMigDaoImpl.verifyExperimentId();
        log.info("------------------------------------------------Step 1 End------------------------------------------------");
        returnString += "Verification of experiment version  Done. Step1 complete.";
        doAll = true;
      } catch (SQLException e) {
        returnString += "Verify experiment version failed. Restart job from step1";
        throw new SQLException(returnString, e);
      }
    }
    
    if (doAll || (cursor != null && cursor.equalsIgnoreCase("step2"))) {
      try {
        log.info("------------------------------------------------Step 2 Begin------------------------------------------------");
        sqlMigDaoImpl.verifyExperimentName();
        log.info("------------------------------------------------Step 2 End------------------------------------------------");
        returnString += "Verification of experiment name  Done. Step2 complete.";
        doAll = true;
      } catch (SQLException e) {
        returnString += "Verify experiment name failed. Restart job from step2";
        throw new SQLException(returnString, e);
      }
    }
    if (doAll || (cursor != null && cursor.equalsIgnoreCase("step3"))) {
      try {
        log.info("------------------------------------------------Step 3 Begin------------------------------------------------");
        sqlMigDaoImpl.verifyExperimentVersion();
        log.info("------------------------------------------------Step 3 End------------------------------------------------");
        returnString += "Verification of experiment version  Done. Step3 complete.";
        doAll = true;
      } catch (SQLException e) {
        returnString += "Verify experiment version failed. Restart job from step3";
        throw new SQLException(returnString, e);
      }
    }
    
    if (doAll || (cursor != null && cursor.equalsIgnoreCase("step4"))) {
      try {
        log.info("------------------------------------------------Step 4 Begin------------------------------------------------");
        sqlMigDaoImpl.verifyGroupName();
        log.info("------------------------------------------------Step 4 End------------------------------------------------");
        returnString += "Verification of experiment group name  Done. Step4 complete.";
        doAll = true;
      } catch (SQLException e) {
        returnString += "Verify experiment group name failed. Restart job from step4";
        throw new SQLException(returnString, e);
      }
    }
    
    if (doAll || (cursor != null && cursor.equalsIgnoreCase("step5"))) {
      try {
        log.info("------------------------------------------------Step 5 Begin------------------------------------------------");
        sqlMigDaoImpl.verifyAnonWho();
        log.info("------------------------------------------------Step 5 End------------------------------------------------");
        returnString += "Verification of anon who  Done. Step5 complete.";
        doAll = true;
      } catch (SQLException e) {
        returnString += "Verify anon who failed. Restart job from step5";
        throw new SQLException(returnString, e);
      }
    }
    
    if (doAll || (cursor != null && cursor.equalsIgnoreCase("step6"))) {
      try {
        log.info("------------------------------------------------Step 6 Begin------------------------------------------------");
        sqlMigDaoImpl.verifyInputId();
        log.info("------------------------------------------------Step 6 End------------------------------------------------");
        returnString += "Verification of  input name  Done. Step6 complete.";
        doAll = true;
      } catch (SQLException e) {
        returnString += "Verify input name failed. Restart job from step6";
        throw new SQLException(returnString, e);
      }
    }
    return returnString;
  }
}

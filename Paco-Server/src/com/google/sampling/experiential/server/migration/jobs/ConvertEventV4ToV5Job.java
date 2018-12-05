package com.google.sampling.experiential.server.migration.jobs;

import java.sql.SQLException;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.migration.MigrationJob;
import com.google.sampling.experiential.server.migration.dao.EventV5MigrationDao;
import com.google.sampling.experiential.server.migration.dao.impl.EventV5MigrationDaoImpl;

public class ConvertEventV4ToV5Job implements MigrationJob {

    public static final Logger log = Logger.getLogger(ConvertEventV4ToV5Job.class.getName());

    @Override
    public boolean doMigration(String cursor, DateTime startTime, DateTime endTime) {
      Boolean success = false;
      try {
        String successMsg = convertEventV4ToV5(cursor);
        if (successMsg != null && successMsg.equals("All Done")) { 
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
    
    private String convertEventV4ToV5(String cursor) throws SQLException {
      String returnString = "";
      Boolean doAll = false;
      EventV5MigrationDao sqlMigDaoImpl = new EventV5MigrationDaoImpl();
      if (cursor == null) {
        doAll = true;
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step1"))) {
        try {
            sqlMigDaoImpl.eventV5RemoveOldIndexes();
            returnString = "Removed old Indexes. Step1 complete.";
            doAll = true;
          } catch (SQLException e) {
            returnString = "Failed to remove old Indexes. Restart job";
            throw new SQLException(returnString, e);
          }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step2"))) {
        try {
          sqlMigDaoImpl.eventV5RenameExistingColumns();
          returnString += "Renamed old columns. Step2 complete.";
          doAll =  true;
        } catch (SQLException e) {
          returnString += "Failed to rename old columns. Restart job from step2";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step3"))) {
        try {
          sqlMigDaoImpl.eventV5AddNewColumns();
          returnString += "Added new columns. Step3 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to add new columns. Restart job from step3";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step4"))) {
        try {
          sqlMigDaoImpl.eventV5UpdateNewColumnsWithValues();
          returnString += "Updating new new columns with values. Step4 complete.";
          doAll = true;
        } catch (SQLException e) {
          returnString += "Failed to update new columns with values. Restart job from step4";
          throw new SQLException(returnString, e);
        }
      }
      if (doAll || (cursor != null && cursor.equalsIgnoreCase("step5"))) {
        try {
          sqlMigDaoImpl.eventV5AddNewIndexes();
          returnString = "All Done";
        } catch (SQLException e) {
          returnString += "Failed to add New Indexes. Restart job from step5";
          throw new SQLException(returnString, e);
        }
      }
       
      return returnString;
    }
}

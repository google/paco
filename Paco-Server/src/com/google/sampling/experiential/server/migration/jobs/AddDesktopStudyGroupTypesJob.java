package com.google.sampling.experiential.server.migration.jobs;

import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.migration.MigrationJob;
import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.logging.Logger;

public class AddDesktopStudyGroupTypesJob implements MigrationJob {

  public static final Logger log = Logger.getLogger(AddDesktopStudyGroupTypesJob.class.getName());

  @Override
  public boolean doMigration(String cursor, DateTime startTime, DateTime endTime) {
    try {
      log.info("------------------------------------------------Begin------------------------------------------------");
      AddDesktopStudyGroupTypes migrator = new AddDesktopStudyGroupTypes();
      migrator.insertNewGroupTypes();
      log.info("------------------------------------------------End------------------------------------------------");
      return true;
    } catch (Exception e) {
      log.warning(ExceptionUtil.getStackTraceAsString(e));
    }
    return false;
  }
}

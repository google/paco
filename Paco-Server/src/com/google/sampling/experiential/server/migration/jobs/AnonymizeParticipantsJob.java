package com.google.sampling.experiential.server.migration.jobs;

import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.migration.MigrationDataRetriever;
import com.google.sampling.experiential.server.migration.MigrationJob;

public class AnonymizeParticipantsJob implements MigrationJob {

    public static final Logger log = Logger.getLogger(AnonymizeParticipantsJob.class.getName());

    @Override
    public boolean doMigration(String cursor, DateTime startTime, DateTime endTime) {
      Boolean success = false;
      try {
        String successMsg = MigrationDataRetriever.getInstance().anonymizeParticipants(cursor);
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
}

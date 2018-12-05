package com.google.sampling.experiential.server.migration.jobs;

import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.sampling.experiential.server.migration.MigrationDataRetriever;
import com.google.sampling.experiential.server.migration.MigrationJob;

public class CatchUpDateRangeJob implements MigrationJob {

    public static final Logger log = Logger.getLogger(CatchUpDateRangeJob.class.getName());

    @Override
    public boolean doMigration(String cursor, DateTime startTime, DateTime endTime) {
      boolean populateExperimentInfoInEventsTable = false;
      return MigrationDataRetriever.getInstance().catchUpEventsFromDSToCS(cursor, startTime, endTime, populateExperimentInfoInEventsTable);
    }
}

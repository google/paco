package com.google.sampling.experiential.server.migration.jobs;

import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.sampling.experiential.server.migration.MigrationDataRetriever;
import com.google.sampling.experiential.server.migration.MigrationJob;

public class CSInsertEventsJob implements MigrationJob {

    public static final Logger log = Logger.getLogger(CSInsertEventsJob.class.getName());

    @Override
    public boolean doMigration(String cursor, DateTime startTime, DateTime endTime) {
      return MigrationDataRetriever.getInstance().copyAllEventsFromLowLevelDSToCloudSql(cursor);
    }
}

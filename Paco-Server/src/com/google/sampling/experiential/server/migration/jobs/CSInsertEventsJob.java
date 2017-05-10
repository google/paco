package com.google.sampling.experiential.server.migration.jobs;

import java.util.logging.Logger;

import com.google.sampling.experiential.server.EventRetriever;
import com.google.sampling.experiential.server.migration.MigrationJob;

public class CSInsertEventsJob implements MigrationJob {

    public static final Logger log = Logger.getLogger(CSInsertEventsJob.class.getName());

    @Override
    public boolean doMigration() {
      return doMigration(null);
    }

    @Override
    public boolean doMigration(String cursor) {
      boolean isFinished = EventRetriever.getInstance().copyAllEventsFromLowLevelDSToCloudSql(cursor);
      return isFinished;
    }

}

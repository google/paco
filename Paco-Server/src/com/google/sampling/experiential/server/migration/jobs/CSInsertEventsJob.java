package com.google.sampling.experiential.server.migration.jobs;

import java.util.logging.Logger;

import com.google.sampling.experiential.server.migration.MigrationDataRetriever;
import com.google.sampling.experiential.server.migration.MigrationJob;

public class CSInsertEventsJob implements MigrationJob {

    public static final Logger log = Logger.getLogger(CSInsertEventsJob.class.getName());

    @Override
    public boolean doMigration(String cursor) {
      return MigrationDataRetriever.getInstance().copyAllEventsFromLowLevelDSToCloudSql(cursor);
    }
}

package com.google.sampling.experiential.server.migration.jobs;

import java.util.logging.Logger;

import com.google.sampling.experiential.server.EventRetriever;
import com.google.sampling.experiential.server.migration.MigrationJob;

public class CSInsertOutputsJob implements MigrationJob {

    public static final Logger log = Logger.getLogger(CSInsertOutputsJob.class.getName());

    @Override
    public boolean doMigration() {
      return doMigration(null);
    }

    @Override
    public boolean doMigration(String cursor) {
      EventRetriever.getInstance().readOutputsDataStoreAndInsertToCloudSql(cursor);
      return true;
    }

}

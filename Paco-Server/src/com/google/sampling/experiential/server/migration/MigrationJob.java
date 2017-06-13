package com.google.sampling.experiential.server.migration;

import org.joda.time.DateTime;

public interface MigrationJob {

  public abstract boolean doMigration(String cursor, DateTime startTime, DateTime endTime);

}
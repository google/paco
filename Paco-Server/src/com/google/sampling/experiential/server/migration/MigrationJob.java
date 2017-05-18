package com.google.sampling.experiential.server.migration;

public interface MigrationJob {

  public abstract boolean doMigration(String cursor);

}
package com.google.sampling.experiential.server.migration.dao;

import java.sql.SQLException;

public interface CopyExperimentMigrationDao {
  
  boolean copyExperimentCreateTables() throws SQLException;
  boolean addModificationsToExistingTables() throws SQLException;
  boolean addDataTypes() throws SQLException;
}
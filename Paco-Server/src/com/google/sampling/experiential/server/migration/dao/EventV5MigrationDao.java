package com.google.sampling.experiential.server.migration.dao;

import java.sql.SQLException;

public interface EventV5MigrationDao {
  boolean eventV5AddNewColumns() throws SQLException;
  boolean eventV5RenameExistingColumns() throws SQLException;
  boolean eventV5UpdateNewColumnsWithValues() throws SQLException;
  boolean eventV5RemoveOldIndexes() throws SQLException;
  boolean eventV5AddNewIndexes() throws SQLException;
 }
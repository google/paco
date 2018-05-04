package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;

public interface CSExperimentDefinitionDao {
  
  boolean insertExperimentDefinition(Long experimentId, Integer version, String jsonString) throws SQLException;

  boolean updateSplitJson(Long experimentId, Integer experimentVersion, String splitJson) throws SQLException;

  boolean updateMigrationStatus(Long experimentId, Integer experimentVersion, String errorMessage) throws SQLException;

  Integer getTotalRecordsInExperimentDefinitionBackupTable() throws SQLException;

  boolean insertExperimentDefinitionBackup(Long experimentId, Integer version, String jsonString) throws SQLException;
  
  List<Long> getErroredExperimentDefinition() throws SQLException;

  boolean deleteExperiment(List<Long> experimentIds) throws SQLException;

}
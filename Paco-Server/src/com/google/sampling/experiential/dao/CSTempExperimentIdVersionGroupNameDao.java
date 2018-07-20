package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;

import com.google.sampling.experiential.dao.dataaccess.ExperimentLite;

public interface CSTempExperimentIdVersionGroupNameDao {
  void insertExperimentIdVersionAndGroupName() throws SQLException;
  List<Long> getExperimentIdsToBeDeleted() throws SQLException;
  List<ExperimentLite> getAllExperimentLiteOfStatus(Integer status) throws SQLException;
  List<ExperimentLite> getDistinctExperimentIdAndVersion(Integer status) throws SQLException;
  boolean deleteExperiments(List<Long> toBeDeletedExperiments) throws SQLException;
  boolean deleteExperiment(Long toBeDeletedExperimentId, Integer experimentVersion) throws SQLException;
  boolean updateExperimentIdVersionGroupNameStatus(Long expId, Integer expVersion, String groupName, Integer status) throws SQLException;
  boolean upsertExperimentIdVersionGroupName(Long expId, Integer expVersion, String groupName,
                                             Integer status) throws SQLException;
}

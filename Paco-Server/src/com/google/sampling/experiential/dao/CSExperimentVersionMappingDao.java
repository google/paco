package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.google.sampling.experiential.dao.dataaccess.ExperimentVersionMapping;
import com.pacoapp.paco.shared.model2.ExperimentDAO;

public interface CSExperimentVersionMappingDao {
  void updateExperimentVersionMapping(ExperimentDAO experimentDao) throws SQLException;
  Map<String, ExperimentVersionMapping> getAllGroupsPreviousVersion(Long experimentId, Integer version) throws SQLException;
  boolean createExperimentVersionMapping(List<ExperimentVersionMapping> experimentVersionMappingLst);
}

package com.google.sampling.experiential.dao;

import java.sql.SQLException;

import com.google.sampling.experiential.dao.dataaccess.ExperimentDetail;

public interface CSExperimentDetailDao {
  void insertExperimentDetail(ExperimentDetail experiment) throws SQLException;
  Long getExperimentDetailId(Long expId, Integer expVersion) throws SQLException;
}

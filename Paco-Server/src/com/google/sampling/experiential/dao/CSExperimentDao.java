package com.google.sampling.experiential.dao;

import java.sql.SQLException;

import com.google.sampling.experiential.dao.dataaccess.Experiment;

public interface CSExperimentDao {
  void insertExperiment(Experiment experiment) throws SQLException;
}

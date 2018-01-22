package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.Map;

import com.google.sampling.experiential.dao.dataaccess.InputCollection;
import com.pacoapp.paco.shared.model2.ExperimentDAO;

public interface CSInputCollectionDao {
  
  void createInputCollectionId(ExperimentDAO exptDao,
                                                  Map<String, InputCollection> newVersion, Map<String, InputCollection> oldVersion) throws SQLException;
}

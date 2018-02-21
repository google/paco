package com.google.sampling.experiential.dao;

import java.sql.SQLException;

public interface CSExperimentDefinitionDao {

  
  boolean insertExperimentDefinition(Long experimentId, Integer version, String jsonString) throws SQLException;

}

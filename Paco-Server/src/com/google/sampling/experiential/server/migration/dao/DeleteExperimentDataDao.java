package com.google.sampling.experiential.server.migration.dao;

import java.sql.SQLException;

public interface DeleteExperimentDataDao {
  boolean deleteEventsAndOutputs(Long expId) throws SQLException; 
  boolean deleteExperimentGroupDetailAndInformedConsent(Long expId) throws SQLException;
  boolean deleteInputCollectionInputAndChoiceCollection(Long expId)  throws SQLException; 
  boolean deleteUser(Long expId) throws SQLException;
  boolean deleteExperimentUser(Long expId) throws SQLException; 
}

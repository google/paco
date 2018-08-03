package com.google.sampling.experiential.server.migration.dao;

import java.sql.SQLException;

public interface ExperimentMigrationVerificationDao {
  
  boolean verifyExperimentId() throws SQLException; 
  boolean verifyExperimentVersion() throws SQLException;
  boolean verifyGroupName() throws SQLException, Exception; 
  boolean verifyAnonWho()  throws SQLException; 
  boolean verifyInputId() throws SQLException;
  boolean verifyExperimentName() throws SQLException;
   
}

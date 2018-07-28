package com.google.sampling.experiential.server.migration.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.google.sampling.experiential.dao.CSEventOutputDao;
import com.google.sampling.experiential.dao.CSInputCollectionDao;
import com.google.sampling.experiential.dao.impl.CSEventOutputDaoImpl;
import com.google.sampling.experiential.dao.impl.CSInputCollectionDaoImpl;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.QueryConstants;
import com.google.sampling.experiential.server.migration.dao.DeleteExperimentDataDao;
import com.pacoapp.paco.shared.util.ErrorMessages;

public class DeleteExperimentDataDaoImpl implements DeleteExperimentDataDao {
  public static final Logger log = Logger.getLogger(ExperimentMigrationVerificationDaoImpl.class.getName());
  
  
  @Override
  public boolean deleteEventsAndOutputs(Long expId) throws SQLException {
    boolean successFlag = false;
    CSEventOutputDao daoImpl = new CSEventOutputDaoImpl();
    daoImpl.deleteAllEventsAndOutputsData(expId);
    return successFlag;
  }

  @Override
  public boolean deleteExperimentGroupDetailAndInformedConsent(Long expId) throws SQLException {
    Connection conn = null;
    PreparedStatement statementDeleteExpGroupDetailAndInfConsent = null;
    String deleteQuery2 = QueryConstants.DELETE_EVGM_EXP_GROUP_DETAILS_INF_CONSENT.toString();
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementDeleteExpGroupDetailAndInfConsent = conn.prepareStatement(deleteQuery2);
      statementDeleteExpGroupDetailAndInfConsent.setLong(1, expId);
      statementDeleteExpGroupDetailAndInfConsent.execute();
      log.info("Deleted " + statementDeleteExpGroupDetailAndInfConsent.getUpdateCount() +  " events, exp detail, group detail, informed consent records" );
      return true;
    } finally {
      try {
        if (statementDeleteExpGroupDetailAndInfConsent != null) {
          statementDeleteExpGroupDetailAndInfConsent.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    } 
    
   
  }

  @Override
  public boolean deleteInputCollectionInputAndChoiceCollection(Long expId) throws SQLException {
    CSInputCollectionDao icDaoImpl = new CSInputCollectionDaoImpl();
    icDaoImpl.deleteInputCollectionInputAndChoiceCollection(expId);
    return true;
  }

  @Override
  public boolean deleteUser(Long expId) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean deleteExperimentUser(Long expId) throws SQLException {
    Connection conn = null;
    PreparedStatement statementDeleteExperimentUser = null;
    String deleteQuery2 = QueryConstants.DELETE_EXPERIMENT_USER_FOR_EXPERIMENT.toString();
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementDeleteExperimentUser = conn.prepareStatement(deleteQuery2);
      statementDeleteExperimentUser.setLong(1, expId);
      statementDeleteExperimentUser.execute();
      log.info("Deleted " + statementDeleteExperimentUser.getUpdateCount() +  " experiment user records" );
      return true;
    } finally {
      try {
        if (statementDeleteExperimentUser != null) {
          statementDeleteExperimentUser.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    } 
    
  }
  
}

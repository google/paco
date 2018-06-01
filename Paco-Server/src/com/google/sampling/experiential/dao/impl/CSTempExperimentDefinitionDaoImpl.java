package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.cloudsql.columns.TempExperimentDefinitionColumns;
import com.google.sampling.experiential.dao.CSTempExperimentDefinitionDao;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.QueryConstants;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.util.ErrorMessages;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

public class CSTempExperimentDefinitionDaoImpl implements CSTempExperimentDefinitionDao {
  public static final Logger log = Logger.getLogger(CSTempExperimentDefinitionDaoImpl.class.getName());
  private static List<Column> experimentDefinitionColList = Lists.newArrayList();
  private static List<Column> experimentDefinitionBkColList = Lists.newArrayList();
  
  static {
    experimentDefinitionColList.add(new Column(TempExperimentDefinitionColumns.ID));
    experimentDefinitionColList.add(new Column(TempExperimentDefinitionColumns.VERSION));
    experimentDefinitionColList.add(new Column(TempExperimentDefinitionColumns.SOURCE_JSON));
    experimentDefinitionColList.add(new Column(TempExperimentDefinitionColumns.CONVERTED_JSON));
    experimentDefinitionColList.add(new Column(TempExperimentDefinitionColumns.MIGRATION_STATUS));
    experimentDefinitionColList.add(new Column(TempExperimentDefinitionColumns.ERROR_MESSAGE));
    experimentDefinitionBkColList.add(new Column(TempExperimentDefinitionColumns.ID));
    experimentDefinitionBkColList.add(new Column(TempExperimentDefinitionColumns.VERSION));
    experimentDefinitionBkColList.add(new Column(TempExperimentDefinitionColumns.SOURCE_JSON));
  }

  @Override
  public boolean insertExperimentDefinition(Long experimentId, Integer version, String jsonString) throws SQLException {

    PreparedStatement statementCreateExperimentDefinition = null;
    ExpressionList expDefExprList = new ExpressionList();
    List<Expression>  out = Lists.newArrayList();
    Insert expDefInsert = new Insert();
    Connection conn = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      expDefInsert.setTable(new Table(TempExperimentDefinitionColumns.TABLE_NAME));
      expDefInsert.setUseValues(true);
      expDefExprList.setExpressions(out);
      expDefInsert.setItemsList(expDefExprList);
      expDefInsert.setColumns(experimentDefinitionColList);
      // Adding ? for prepared stmt
      for (Column c : experimentDefinitionColList) {
        ((ExpressionList) expDefInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }
      statementCreateExperimentDefinition = conn.prepareStatement(expDefInsert.toString());
      statementCreateExperimentDefinition.setLong(1, experimentId);
      statementCreateExperimentDefinition.setInt(2, version);
      statementCreateExperimentDefinition.setString(3, jsonString);
      statementCreateExperimentDefinition.setString(4, null);
      statementCreateExperimentDefinition.setInt(5, 0);
      statementCreateExperimentDefinition.setString(6, null);
      log.info("create exp def for "+ experimentId + "--" + version);
      statementCreateExperimentDefinition.execute();
      conn.commit();
      return true;
    } finally {
      try {
        if (statementCreateExperimentDefinition != null) {
          statementCreateExperimentDefinition.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
  }
  
  @Override
  public boolean insertExperimentDefinitionBackup(Long experimentId, Integer version, String jsonString) throws SQLException {

    PreparedStatement statementCreateExperimentDefinition = null;
    ExpressionList expDefExprList = new ExpressionList();
    List<Expression>  out = Lists.newArrayList();
    Insert expDefInsert = new Insert();
    Connection conn = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      expDefInsert.setTable(new Table(TempExperimentDefinitionColumns.TABLE_NAME+"_bk"));
      expDefInsert.setUseValues(true);
      expDefExprList.setExpressions(out);
      expDefInsert.setItemsList(expDefExprList);
      expDefInsert.setColumns(experimentDefinitionBkColList);
      // Adding ? for prepared stmt
      for (Column c : experimentDefinitionBkColList) {
        ((ExpressionList) expDefInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }
      statementCreateExperimentDefinition = conn.prepareStatement(expDefInsert.toString());
      statementCreateExperimentDefinition.setLong(1, experimentId);
      statementCreateExperimentDefinition.setInt(2, version);
      statementCreateExperimentDefinition.setString(3, jsonString);
     
      log.info("create exp def bk:"+ statementCreateExperimentDefinition.toString());
      statementCreateExperimentDefinition.execute();
      conn.commit();
      return true;
    } finally {
      try {
        if (statementCreateExperimentDefinition != null) {
          statementCreateExperimentDefinition.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
  }
  
  @Override
  public List<ExperimentDAO> getAllExperimentFromExperimentDefinition(Integer migrationStatus) throws SQLException {
    List<ExperimentDAO> experimentList = Lists.newArrayList();
    Connection conn = null;
    ResultSet rs = null;
    String experimentJson = null;
    ExperimentDAO experimentDao = null;
    PreparedStatement statementSelectExperimentJson = null;
    String query = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      query = QueryConstants.GET_ALL_EXPERIMENT_JSON.toString();
      if (migrationStatus == 1) {
        query = query + " and " + TempExperimentDefinitionColumns.ERROR_MESSAGE+ "  is null and  " + TempExperimentDefinitionColumns.CONVERTED_JSON + " is not null";
      }
      statementSelectExperimentJson = conn.prepareStatement(query);
      statementSelectExperimentJson.setInt(1, migrationStatus);
      log.info(query);
      rs = statementSelectExperimentJson.executeQuery();
      while(rs.next()) {
        if (migrationStatus == 0) {
          experimentJson = rs.getString(TempExperimentDefinitionColumns.SOURCE_JSON);
          experimentDao = JsonConverter.fromSingleEntityJson(experimentJson.substring(1,experimentJson.length()-1));
        } else if (migrationStatus == 1) {
          experimentJson = rs.getString(TempExperimentDefinitionColumns.CONVERTED_JSON);
          experimentDao = JsonConverter.fromSingleEntityJson(experimentJson);
        }
        experimentList.add(experimentDao);
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementSelectExperimentJson != null) {
          statementSelectExperimentJson.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    
    log.info("Retrieved " + experimentList.size() + "experiments");
    return experimentList;
  }
  
  
  @Override
  public boolean updateSplitJson(Long experimentId, Integer experimentVersion, String splitJson) throws SQLException {
    Connection conn = null;
    PreparedStatement statementUpdateExperimentDefinition = null;
    String updateQuery = QueryConstants.UPDATE_SPLIT_JSON_IN_EXPERIMENT_DEFINITION.toString();
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      
      statementUpdateExperimentDefinition = conn.prepareStatement(updateQuery);
      statementUpdateExperimentDefinition.setString(1, splitJson);
      statementUpdateExperimentDefinition.setLong(2, experimentId);
      statementUpdateExperimentDefinition.setInt(3, experimentVersion);
      
      statementUpdateExperimentDefinition.executeUpdate();
//      log.info("updated exp def: exp id:" + experimentId + "-version:" + experimentVersion + ",splitJson" + splitJson);
    } finally {
      try {
        if (statementUpdateExperimentDefinition != null) {
          statementUpdateExperimentDefinition.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
        throw ex1;
      }
    }
    return true;
  }
  
  @Override
  public boolean deleteExperiment(List<Long> experimentIds) throws SQLException {
    Connection conn = null;
    PreparedStatement statementDeleteExperimentDefinition = null;
    String updateQuery = QueryConstants.DELETE_FROM_EXPERIMENT_DEFINITION.toString();
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      
      statementDeleteExperimentDefinition = conn.prepareStatement(updateQuery);
      for (Long toBeDeletedExperimentId : experimentIds) {
        statementDeleteExperimentDefinition.setLong(1, toBeDeletedExperimentId);
        statementDeleteExperimentDefinition.addBatch();
      }
      if (experimentIds != null && experimentIds.size() >0) {
        statementDeleteExperimentDefinition.executeBatch();
      }
    } finally {
      try {
        if (statementDeleteExperimentDefinition != null) {
          statementDeleteExperimentDefinition.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
        throw ex1;
      }
    }
    return true;
  }
  
  @Override
  public boolean updateMigrationStatus(Long experimentId, Integer experimentVersion, String errorMessage) throws SQLException {
    Connection conn = null;
    PreparedStatement statementUpdateExperimentDefinition = null;
    String updateQuery1 = QueryConstants.UPDATE_MIGRATION_STATUS_IN_EXPERIMENT_DEFINITION.toString();
    String updateQuery2 = QueryConstants.UPDATE_ERROR_MESSAGE_IN_EXPERIMENT_DEFINITION.toString();
  
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      if (errorMessage == null) {
        statementUpdateExperimentDefinition = conn.prepareStatement(updateQuery1);
        statementUpdateExperimentDefinition.setLong(1, experimentId);
        statementUpdateExperimentDefinition.setInt(2, experimentVersion);
      } else {
        statementUpdateExperimentDefinition = conn.prepareStatement(updateQuery2);
        statementUpdateExperimentDefinition.setString(1, errorMessage);
        statementUpdateExperimentDefinition.setLong(2, experimentId);
        statementUpdateExperimentDefinition.setInt(3, experimentVersion);
      }
      
      int updateCt = statementUpdateExperimentDefinition.executeUpdate();
      log.info("updated exp def for expt id:" + experimentId + "-version:" + experimentVersion);
    } finally {
      try {
        if (statementUpdateExperimentDefinition != null) {
          statementUpdateExperimentDefinition.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return true;
  }
  
  @Override
  public Integer getTotalRecordsInExperimentDefinitionBackupTable() throws SQLException {
    Connection conn = null;
    ResultSet rs = null;
    int ct = 0;
    PreparedStatement statementCountExperimentDefinition = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementCountExperimentDefinition = conn.prepareStatement(QueryConstants.GET_EXPERIMENT_DEFINITION_RECORD_COUNT.toString());
      rs = statementCountExperimentDefinition.executeQuery();
      if (rs.next()) {
        ct = rs.getInt(1);
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementCountExperimentDefinition != null) {
          statementCountExperimentDefinition.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    return ct;
  }

  @Override
  public List<Long> getErroredExperimentDefinition() throws SQLException {
    List<Long> erroredExperiments = Lists.newArrayList();
    Connection conn = null;
    PreparedStatement findAllErroredExperimentsStatement = null;
    ResultSet rs = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      findAllErroredExperimentsStatement = conn.prepareStatement(QueryConstants.GET_ALL_ERRORED_EXPERIMENT_JSON.toString());
      rs = findAllErroredExperimentsStatement.executeQuery();
      while(rs.next()) {
        erroredExperiments.add( rs.getLong(TempExperimentDefinitionColumns.ID));
      }
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (findAllErroredExperimentsStatement != null) {
          findAllErroredExperimentsStatement.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    return erroredExperiments;
  }
  
  @Override
  public ExperimentDAO getExperimentDefinition(Long exptId, Integer expVersion) throws SQLException {
    Connection conn = null;
    ResultSet rs = null;
    String experimentJson = null;
    ExperimentDAO experimentDao = null;
    PreparedStatement statementSelectExperimentJson = null;
    String query = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      query = QueryConstants.GET_EXPERIMENT_JSON_FOR_EXP_ID.toString();
      if (expVersion != null) { 
        query = query +  " and " + TempExperimentDefinitionColumns.VERSION + " = ?";
      } else {
        query = query + " order by " + TempExperimentDefinitionColumns.VERSION + " desc limit 1";
      }
      statementSelectExperimentJson = conn.prepareStatement(query);
      statementSelectExperimentJson.setLong(1, exptId);
      if (expVersion != null) { 
        statementSelectExperimentJson.setLong(2, expVersion);
      }
      
      log.info(query);
      rs = statementSelectExperimentJson.executeQuery();
      while(rs.next()) {
        experimentJson = rs.getString(TempExperimentDefinitionColumns.CONVERTED_JSON);
        experimentDao = JsonConverter.fromSingleEntityJson(experimentJson);  
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementSelectExperimentJson != null) {
          statementSelectExperimentJson.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    return experimentDao;
  }
  
  
}

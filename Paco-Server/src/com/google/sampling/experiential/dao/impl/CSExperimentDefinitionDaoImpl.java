package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.cloudsql.columns.ExperimentDefinitionColumns;
import com.google.sampling.experiential.dao.CSExperimentDefinitionDao;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.QueryConstants;
import com.pacoapp.paco.shared.util.ErrorMessages;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

public class CSExperimentDefinitionDaoImpl implements CSExperimentDefinitionDao {
  public static final Logger log = Logger.getLogger(CSExperimentDefinitionDaoImpl.class.getName());
  private static List<Column> experimentDefinitionColList = Lists.newArrayList();
  private static List<Column> experimentDefinitionBkColList = Lists.newArrayList();
  
  static {
    experimentDefinitionColList.add(new Column(ExperimentDefinitionColumns.ID));
    experimentDefinitionColList.add(new Column(ExperimentDefinitionColumns.VERSION));
    experimentDefinitionColList.add(new Column(ExperimentDefinitionColumns.SOURCE_JSON));
    experimentDefinitionColList.add(new Column(ExperimentDefinitionColumns.CONVERTED_JSON));
    experimentDefinitionColList.add(new Column(ExperimentDefinitionColumns.MIGRATION_STATUS));
    experimentDefinitionColList.add(new Column(ExperimentDefinitionColumns.ERROR_MESSAGE));
    experimentDefinitionBkColList.add(new Column(ExperimentDefinitionColumns.ID));
    experimentDefinitionBkColList.add(new Column(ExperimentDefinitionColumns.VERSION));
    experimentDefinitionBkColList.add(new Column(ExperimentDefinitionColumns.SOURCE_JSON));
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
      expDefInsert.setTable(new Table(ExperimentDefinitionColumns.TABLE_NAME));
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
  public boolean insertExperimentDefinitionBk(Long experimentId, Integer version, String jsonString) throws SQLException {

    PreparedStatement statementCreateExperimentDefinition = null;
    ExpressionList expDefExprList = new ExpressionList();
    List<Expression>  out = Lists.newArrayList();
    Insert expDefInsert = new Insert();
    Connection conn = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      expDefInsert.setTable(new Table(ExperimentDefinitionColumns.TABLE_NAME+"_bk"));
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
  public boolean updateSplitJson(Long experimentId, Integer experimentVersion, String splitJson) throws SQLException {
    Connection conn = null;
    PreparedStatement statementUpdateExperimentDefinition = null;
    String updateQuery = "update experiment_definition set migration_status =migration_status+1, converted_json=? where id=? and version=?";
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
  public boolean updateMigrationStatus(Long experimentId, Integer experimentVersion, String errorMessage) throws SQLException {
    Connection conn = null;
    PreparedStatement statementUpdateExperimentDefinition = null;
    String updateQuery1 = "update experiment_definition set migration_status =migration_status+1 where id=? and version=?";
    String updateQuery2 = "update experiment_definition set error_message=? where id=? and version=?";
  
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
      log.info(statementUpdateExperimentDefinition.toString());
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
      }
    }
    return true;
  }
  
  @Override
  public Integer getTotalRecordsInExperimentDefinition() throws SQLException {
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
        erroredExperiments.add( rs.getLong(ExperimentDefinitionColumns.ID));
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
  
}

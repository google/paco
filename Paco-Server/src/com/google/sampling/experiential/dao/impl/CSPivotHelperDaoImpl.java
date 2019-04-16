package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.cloudsql.columns.PivotHelperColumns;
import com.google.sampling.experiential.dao.CSPivotHelperDao;
import com.google.sampling.experiential.dao.dataaccess.PivotHelper;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.QueryConstants;
import com.pacoapp.paco.shared.util.ErrorMessages;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

public class CSPivotHelperDaoImpl implements CSPivotHelperDao {
  public static final Logger log = Logger.getLogger(CSPivotHelperDaoImpl.class.getName());
  private static List<Column> pvhColList = Lists.newArrayList();
  static {
    pvhColList.add(new Column(PivotHelperColumns.EXPERIMENT_VERSION_GROUP_MAPPING_ID));
    pvhColList.add(new Column(PivotHelperColumns.ANON_WHO));
    pvhColList.add(new Column(PivotHelperColumns.INPUT_ID));
    pvhColList.add(new Column(PivotHelperColumns.PROCESSED));
    pvhColList.add(new Column(PivotHelperColumns.EVENTS_POSTED));
  }
  @Override
  public void incrementUpdateCtByOne(Long evmId, Integer anonWho, List<Long> inputIds) throws SQLException {
    Connection conn = null;
    PreparedStatement statementUpdateEvent = null;
    // this uses upsert command, which inserts first time, and then increments events ct by 1
    String updateQuery = QueryConstants.INSERT_TO_PIVOT_HELPER_WITH_ON_DUPLICATE_CLAUSE.toString() ;

    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementUpdateEvent = conn.prepareStatement(updateQuery);
      for (Long inputId : inputIds) {
        statementUpdateEvent.setLong(1, evmId);
        statementUpdateEvent.setInt(2, anonWho);
        statementUpdateEvent.setLong(3, inputId);
        statementUpdateEvent.setInt(4, 1);
        statementUpdateEvent.setBoolean(5, true);
        
        statementUpdateEvent.addBatch();
      }
      statementUpdateEvent.executeBatch();
      
    } finally {
      try {
        if (statementUpdateEvent != null) {
          statementUpdateEvent.close();
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
  public void insertPivotHelper(List<PivotHelper> pvList) throws SQLException {
    
    Connection conn = null;
    PreparedStatement statementCreatePivotHelper = null;
    ResultSet rs = null;
    ExpressionList insertPvhExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert pvhInsert = new Insert();
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      pvhInsert.setTable(new Table(PivotHelperColumns.TABLE_NAME));
      pvhInsert.setUseValues(true);
      insertPvhExprList.setExpressions(exp);
      pvhInsert.setItemsList(insertPvhExprList);
      pvhInsert.setColumns(pvhColList);
      // Adding ? for prepared stmt
      for (Column c : pvhColList) {
        ((ExpressionList) pvhInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }
  
      statementCreatePivotHelper = conn.prepareStatement(pvhInsert.toString());
      for (PivotHelper pivotHelper : pvList) {
        if (pivotHelper.getEventsPosted() > 0) { 
          statementCreatePivotHelper.setLong(1, pivotHelper.getExpVersionMappingId());
          statementCreatePivotHelper.setInt(2, pivotHelper.getAnonWhoId());
          statementCreatePivotHelper.setLong(3, pivotHelper.getInputId());
          statementCreatePivotHelper.setBoolean(4, pivotHelper.getProcessed());
          statementCreatePivotHelper.setLong(5, pivotHelper.getEventsPosted());
          statementCreatePivotHelper.addBatch();
        }
      } //for
      statementCreatePivotHelper.executeBatch();
      conn.commit();
    } catch(SQLException sqle) {
      log.warning("Exception while inserting to pivotHelper table:" +  sqle);
    } finally {
      try {
        if( rs != null) { 
          rs.close();
        }
        if (statementCreatePivotHelper != null) {
          statementCreatePivotHelper.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.info(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
  }
  
  @Override
  public void updatePivotHelper(List<PivotHelper> pvList) throws SQLException {
    
    Connection conn = null;
    PreparedStatement statementCreatePivotHelper = null;
    ResultSet rs = null;
    
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      statementCreatePivotHelper = conn.prepareStatement(QueryConstants.UPDATE_PIVOT_HELPER.toString());
      for (PivotHelper pivotHelper : pvList) {
        if (pivotHelper.getEventsPosted() > 0) { 
          statementCreatePivotHelper.setLong(1, pivotHelper.getEventsPosted());
          statementCreatePivotHelper.setLong(2, pivotHelper.getExpVersionMappingId());
          statementCreatePivotHelper.setInt(3, pivotHelper.getAnonWhoId());
          statementCreatePivotHelper.setLong(4, pivotHelper.getInputId());
          log.info(statementCreatePivotHelper.toString());
          statementCreatePivotHelper.addBatch();
        }
      } //for
      statementCreatePivotHelper.executeBatch();
      conn.commit();
    } catch(SQLException sqle) {
      log.warning("Exception while updating to pivotHelper table:" +  sqle);
    } finally {
      try {
        if( rs != null) { 
          rs.close();
        }
        if (statementCreatePivotHelper != null) {
          statementCreatePivotHelper.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.info(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
  }
  
  @Override
  public void insertIgnorePivotHelper(List<PivotHelper> pvList) throws SQLException {
    
    Connection conn = null;
    PreparedStatement statementCreatePivotHelper = null;
    ResultSet rs = null;
    
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      statementCreatePivotHelper = conn.prepareStatement(QueryConstants.INSERT_IGNORE_TO_PIVOT_HELPER.toString());
      for (PivotHelper pivotHelper : pvList) {
          statementCreatePivotHelper.setLong(1, pivotHelper.getExpVersionMappingId());
          statementCreatePivotHelper.setInt(2, pivotHelper.getAnonWhoId());
          statementCreatePivotHelper.setLong(3, pivotHelper.getInputId());
          statementCreatePivotHelper.setLong(4, pivotHelper.getEventsPosted());
          log.info(statementCreatePivotHelper.toString());
          statementCreatePivotHelper.addBatch();
      } //for
      statementCreatePivotHelper.executeBatch();
      log.info("finsihed");
      conn.commit();
    } catch(SQLException sqle) {
      log.warning("Exception while updating to pivotHelper table:" +  sqle);
    } finally {
      try {
        if( rs != null) { 
          rs.close();
        }
        if (statementCreatePivotHelper != null) {
          statementCreatePivotHelper.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.info(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
  }
}

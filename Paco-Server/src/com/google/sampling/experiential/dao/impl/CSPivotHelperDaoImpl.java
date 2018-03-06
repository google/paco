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
    pvhColList.add(new Column(PivotHelperColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID));
    pvhColList.add(new Column(PivotHelperColumns.ANON_WHO));
    pvhColList.add(new Column(PivotHelperColumns.INPUT_ID));
    pvhColList.add(new Column(PivotHelperColumns.PROCESSED));
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
        
          statementCreatePivotHelper.setLong(1, pivotHelper.getExpVersionMappingId());
          statementCreatePivotHelper.setInt(2, pivotHelper.getAnonWhoId());
          statementCreatePivotHelper.setLong(3, pivotHelper.getInputId());
          statementCreatePivotHelper.setBoolean(4, pivotHelper.getProcessed());
          log.info(statementCreatePivotHelper.toString());
          if (!getPivotHelper(pivotHelper.getExpVersionMappingId(),pivotHelper.getAnonWhoId(),pivotHelper.getInputId())) {
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
  public boolean getPivotHelper(Long evmId, Integer anonWho, Long inputId) throws SQLException {
    
    Connection conn = null;
    PreparedStatement statementUpdateEvent = null;
    ResultSet rs =null;
    String updateQuery = "select * from pivot_helper where experiment_group_version_mapping_id= ? and anon_who=? and input_id=?";
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      
      statementUpdateEvent = conn.prepareStatement(updateQuery);
      statementUpdateEvent.setLong(1, evmId);
      statementUpdateEvent.setInt(2, anonWho);
      statementUpdateEvent.setLong(3, inputId);
      rs = statementUpdateEvent.executeQuery();
      while (rs.next()) {
        return true;
      }
      return false;
//      log.info("chk if pv exists " + evmId + "-who:" + anonWho + ",inputid" + inputId);
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
//    return true;
  }
  
  @Override
  public boolean updatePivotHelperStatus(Long evmId, Integer anonWho, Long inputId, Long updateCt) throws SQLException {
    Connection conn = null;
    PreparedStatement statementUpdateEvent = null;
    String updateQuery = "update pivot_helper set processed =b'1', events_posted=events_posted+ " + updateCt + " where experiment_group_version_mapping_id= ? and anon_who=? and input_id=?";
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      
      statementUpdateEvent = conn.prepareStatement(updateQuery);
      statementUpdateEvent.setLong(1, evmId);
      statementUpdateEvent.setInt(2, anonWho);
      statementUpdateEvent.setLong(3, inputId);
      statementUpdateEvent.executeUpdate();
      log.info("updated pv status  as  complete for " + evmId + "-who:" + anonWho + ",inputid" + inputId + ",updateCt"+ updateCt);
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
    return true;
  }

  @Override
  public void incrementUpdateCtByOne(Long evmId, Integer anonWho, List<Long> inputIds) throws SQLException {
    Connection conn = null;
    PreparedStatement statementUpdateEvent = null;
    // this uses upsert command, which inserts first time, and then increments events ct by 1
    String updateQuery = "INSERT INTO pivot_helper (experiment_group_version_mapping_id, anon_who, input_id, events_posted, processed) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE events_posted=events_posted+1" ;

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
        log.info(updateQuery);
        log.info("updated status  as  complete for " + evmId + "-who:" + anonWho + ",inputid" + inputId);
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
}

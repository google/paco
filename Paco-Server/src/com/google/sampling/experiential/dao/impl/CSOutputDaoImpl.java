package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.cloudsql.columns.OutputServerColumns;
import com.google.sampling.experiential.dao.CSInputDao;
import com.google.sampling.experiential.dao.CSOutputDao;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.QueryConstants;
import com.google.sampling.experiential.shared.WhatDAO;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;
import com.pacoapp.paco.shared.util.ErrorMessages;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

public class CSOutputDaoImpl implements CSOutputDao {
  public static final Logger log = Logger.getLogger(CSOutputDaoImpl.class.getName());
  private static List<Column> outputNewColList = Lists.newArrayList();
  private static List<Column> outputOldColList = Lists.newArrayList();
  static {
    outputOldColList.add(new Column(OutputBaseColumns.EVENT_ID));
    outputOldColList.add(new Column(OutputBaseColumns.NAME));
    outputOldColList.add(new Column(OutputBaseColumns.ANSWER));
    outputNewColList.add(new Column(OutputBaseColumns.EVENT_ID));
    outputNewColList.add(new Column(OutputServerColumns.INPUT_ID));
    outputNewColList.add(new Column(OutputBaseColumns.ANSWER));
  }

  @Override
  public boolean insertSingleOutput(Long eventId, Long inputId, String text, String answer, Boolean oldColumnNames) throws SQLException {

    PreparedStatement statementCreateEventOutput = null;
    ExpressionList outputExprList = new ExpressionList();
    List<Expression>  out = Lists.newArrayList();
    Insert outputInsert = new Insert();
    List<Column> outputColList = null;
    Connection conn = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      outputInsert.setTable(new Table(OutputBaseColumns.TABLE_NAME));
      outputInsert.setUseValues(true);
      outputExprList.setExpressions(out);
      outputInsert.setItemsList(outputExprList);
      if (oldColumnNames) {
        outputColList = outputOldColList;
      } else {
        outputColList = outputNewColList;
      }
      outputInsert.setColumns(outputColList);
      // Adding ? for prepared stmt
      for (Column c : outputColList) {
        ((ExpressionList) outputInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }
      statementCreateEventOutput = conn.prepareStatement(outputInsert.toString());
      statementCreateEventOutput.setLong(1, eventId);
      if (oldColumnNames) {
        statementCreateEventOutput.setString(2, text);
      } else {
        statementCreateEventOutput.setLong(2, inputId);
      }
      statementCreateEventOutput.setString(3, answer);
      int insertCount = statementCreateEventOutput.executeUpdate();
      conn.commit();
      return insertCount>0;
    } finally {
      try {
        if (statementCreateEventOutput != null) {
          statementCreateEventOutput.close();
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
  public List<WhatDAO> getOutputs(Long eventId, Boolean populateEventsTableOldMethod) throws SQLException {
    List<WhatDAO> whatLst = Lists.newArrayList();
    CSInputDao inputDaoImpl = new CSInputDaoImpl();
    WhatDAO whatObj = null;
    String question = null;
    String answer = null;
    Connection conn = null;
    ResultSet rs = null;
    Long inputId = null;
    PreparedStatement statementSelectOutput = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementSelectOutput = conn.prepareStatement(QueryConstants.GET_ALL_OUTPUTS_FOR_EVENT_ID.toString());
      statementSelectOutput.setLong(1, eventId);
      log.info(statementSelectOutput.toString());
      rs = statementSelectOutput.executeQuery();
      while (rs.next()) {
        if (populateEventsTableOldMethod) { 
          question = rs.getString(OutputBaseColumns.NAME);
        } else {
          inputId = rs.getLong(OutputServerColumns.INPUT_ID);
          
          if (inputId != null) { 
            question = inputDaoImpl.getLabelForInputId(inputId);
          }
        }
        answer = rs.getString(OutputBaseColumns.ANSWER);
        whatObj = new WhatDAO(question, answer);
        whatLst.add(whatObj);
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementSelectOutput != null) {
          statementSelectOutput.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    return whatLst;
  }
  @Override
  public List<WhatDAO> getOutputsWithoutInputId(Connection conn, Long eventId) throws SQLException {
    List<WhatDAO> whatLst = Lists.newArrayList();
    WhatDAO whatObj = null;
    String question = null;
    String answer = null;
    ResultSet rs = null;
    PreparedStatement statementSelectOutput = null;
    try {
      statementSelectOutput = conn.prepareStatement(QueryConstants.GET_ALL_OUTPUTS_WITHOUT_INPUTID_FOR_EVENT_ID.toString());
      statementSelectOutput.setLong(1, eventId);
      rs = statementSelectOutput.executeQuery();
      while(rs.next()){
        question = rs.getString(OutputBaseColumns.NAME);
        answer = rs.getString(OutputBaseColumns.ANSWER);
        whatObj = new WhatDAO(question, answer);
        whatLst.add(whatObj);
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementSelectOutput != null) {
          statementSelectOutput.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    return whatLst;
  }
  
  @Override
  public Long getDistinctOutputCount(Long experimentId) throws SQLException {
    Long count = 0L;
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementSelectOutput = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementSelectOutput = conn.prepareStatement(QueryConstants.GET_DISTINCT_OUTPUTS_FOR_EXPERIMENT_ID.toString());
      statementSelectOutput.setLong(1, experimentId);
      rs = statementSelectOutput.executeQuery();
      while(rs.next()){
       count = rs.getLong(1);
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementSelectOutput != null) {
          statementSelectOutput.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    return count;
  }
}

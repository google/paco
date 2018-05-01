package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
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
  private static List<Column> outputColList = Lists.newArrayList();
  static {
    outputColList.add(new Column(OutputBaseColumns.EVENT_ID));
    outputColList.add(new Column(OutputBaseColumns.NAME));
    outputColList.add(new Column(OutputBaseColumns.ANSWER));
  }

  @Override
  public boolean insertSingleOutput(Long eventId, String text, String answer) throws SQLException {

    PreparedStatement statementCreateEventOutput = null;
    ExpressionList outputExprList = new ExpressionList();
    List<Expression>  out = Lists.newArrayList();
    Insert outputInsert = new Insert();
    Connection conn = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      outputInsert.setTable(new Table(OutputBaseColumns.TABLE_NAME));
      outputInsert.setUseValues(true);
      outputExprList.setExpressions(out);
      outputInsert.setItemsList(outputExprList);
      outputInsert.setColumns(outputColList);
      // Adding ? for prepared stmt
      for (Column c : outputColList) {
        ((ExpressionList) outputInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }
      statementCreateEventOutput = conn.prepareStatement(outputInsert.toString());
      statementCreateEventOutput.setLong(1, eventId);
      statementCreateEventOutput.setString(2, text);
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
  public List<WhatDAO> getOutputs(Long eventId) throws SQLException {
    List<WhatDAO> whatLst = Lists.newArrayList();
    WhatDAO whatObj = null;
    String question = null;
    String answer = null;
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementSelectOutput = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementSelectOutput = conn.prepareStatement(QueryConstants.GET_ALL_OUTPUTS_FOR_EVENT_ID.toString());
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
  public List<WhatDAO> getOutputsWithoutInputId(Long eventId) throws SQLException {
    List<WhatDAO> whatLst = Lists.newArrayList();
    WhatDAO whatObj = null;
    String question = null;
    String answer = null;
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementSelectOutput = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
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
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    return whatLst;
  }
}

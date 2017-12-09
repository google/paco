package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import com.google.cloud.sql.jdbc.Statement;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.dao.CSExperimentLookupDao;
import com.google.sampling.experiential.datastore.ExperimentLookupColumns;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.PacoId;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

public class CSExperimentLookupDaoImpl implements CSExperimentLookupDao {
  public static final Logger log = Logger.getLogger(CSExperimentUserDaoImpl.class.getName());
  private static List<Column> experimentLookupColList = Lists.newArrayList();

  static {
    experimentLookupColList.add(new Column(ExperimentLookupColumns.EXPERIMENT_ID));
    experimentLookupColList.add(new Column(ExperimentLookupColumns.EXPERIMENT_NAME));
    experimentLookupColList.add(new Column(ExperimentLookupColumns.GROUP_NAME));
    experimentLookupColList.add(new Column(ExperimentLookupColumns.EXPERIMENT_VERSION));
  }
  
  @Override
  public PacoId getExperimentLookupIdAndCreate(Long expId, String expName, String groupName, Integer version, boolean createOption) throws SQLException{
    PacoId returnId = new PacoId();
    Connection conn = null;
    ResultSet rs = null;
    ResultSet rs1 = null;
    int ct = 1;
    PreparedStatement statementSelectExperimentLookup = null;
    PreparedStatement statementCreateExperimentLookup = null;
    final String updateValueForLookupid1 = "select "+ ExperimentLookupColumns.EXPERIMENT_LOOKUP_ID +" from " + ExperimentLookupColumns.TABLE_NAME + " where " + ExperimentLookupColumns.EXPERIMENT_ID + " = ? and "  + ExperimentLookupColumns.GROUP_NAME + " = ? and "+ ExperimentLookupColumns.EXPERIMENT_NAME + " = ? and "  + ExperimentLookupColumns.EXPERIMENT_VERSION + " = ? " ;
    final String updateValueForLookupid2 = "select "+ ExperimentLookupColumns.EXPERIMENT_LOOKUP_ID +" from " + ExperimentLookupColumns.TABLE_NAME + " where " + ExperimentLookupColumns.EXPERIMENT_ID + " = ? and "  + ExperimentLookupColumns.GROUP_NAME + " is null and "+ ExperimentLookupColumns.EXPERIMENT_NAME + " = ? and "  + ExperimentLookupColumns.EXPERIMENT_VERSION + " = ? " ;
    
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      if (groupName == null) {
        statementSelectExperimentLookup = conn.prepareStatement(updateValueForLookupid2);
      } else {
        statementSelectExperimentLookup = conn.prepareStatement(updateValueForLookupid1);
      }
      if (expName == null) { 
        expName = Constants.BLANK;
      }
      if (version == null) {
        version = 0;
      }
      statementSelectExperimentLookup.setLong(ct++, expId);
      if (groupName != null) {
        statementSelectExperimentLookup.setString(ct++, groupName);
      }
      statementSelectExperimentLookup.setString(ct++, expName);
      statementSelectExperimentLookup.setInt(ct++, version);
      
      rs = statementSelectExperimentLookup.executeQuery();
      if (rs.next()) {
        returnId.setIsCreatedWithThisCall(false);
        returnId.setId(new Long(rs.getInt(ExperimentLookupColumns.EXPERIMENT_LOOKUP_ID)));
      } else if (createOption) {
        ExpressionList experimentExprList = new ExpressionList();
        List<Expression>  out = Lists.newArrayList();
        Insert experimentInsert = new Insert();
        experimentInsert.setTable(new Table(ExperimentLookupColumns.TABLE_NAME));
        experimentInsert.setUseValues(true);
        experimentExprList.setExpressions(out);
        experimentInsert.setItemsList(experimentExprList);
        experimentInsert.setColumns(experimentLookupColList);
        // Adding ? for prepared stmt
        for (Column c : experimentLookupColList) {
          ((ExpressionList) experimentInsert.getItemsList()).getExpressions().add(new JdbcParameter());
        }
        statementCreateExperimentLookup = conn.prepareStatement(experimentInsert.toString(), Statement.RETURN_GENERATED_KEYS);
        statementCreateExperimentLookup.setLong(1, expId);
        statementCreateExperimentLookup.setString(2, expName);
        statementCreateExperimentLookup.setString(3, groupName);
        statementCreateExperimentLookup.setInt(4, version);
        statementCreateExperimentLookup.execute();
        rs1 = statementCreateExperimentLookup.getGeneratedKeys();
        if (rs1.next()) {
          returnId.setIsCreatedWithThisCall(true);
          returnId.setId(new Long(rs1.getInt(1)));
        }
      } else {
        //TODO not sure if this is a good option to set to 0
        returnId.setIsCreatedWithThisCall(false);
        returnId.setId(0L);
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if ( rs1 != null) {
          rs1.close();
        }
        if (statementSelectExperimentLookup != null) {
          statementSelectExperimentLookup.close();
        }
        if (statementCreateExperimentLookup != null) {
          statementCreateExperimentLookup.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return returnId;
  }
}



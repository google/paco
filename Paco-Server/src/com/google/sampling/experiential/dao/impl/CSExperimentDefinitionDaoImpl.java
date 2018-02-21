package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.cloudsql.columns.ExperimentDefinitionColumns;
import com.google.sampling.experiential.dao.CSExperimentDefinitionDao;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
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
  static {
    experimentDefinitionColList.add(new Column(ExperimentDefinitionColumns.ID));
    experimentDefinitionColList.add(new Column(ExperimentDefinitionColumns.VERSION));
    experimentDefinitionColList.add(new Column(ExperimentDefinitionColumns.SOURCE_JSON));
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
      log.info("create exp def:"+ statementCreateExperimentDefinition.toString());
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
  
 
}

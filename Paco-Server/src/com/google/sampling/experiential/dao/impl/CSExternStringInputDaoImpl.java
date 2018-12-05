package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import com.google.cloud.sql.jdbc.Statement;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.cloudsql.columns.ExternStringInputColumns;
import com.google.sampling.experiential.dao.CSExternStringInputDao;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.PacoId;
import com.google.sampling.experiential.server.QueryConstants;
import com.pacoapp.paco.shared.util.ErrorMessages;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

public class CSExternStringInputDaoImpl implements CSExternStringInputDao {

  public static final Logger log = Logger.getLogger(CSExternStringInputDaoImpl.class.getName());
  private static List<Column> labelColList = Lists.newArrayList();
  static {
    labelColList.add(new Column(ExternStringInputColumns.LABEL));
  }
  @Override
  public PacoId getTextAndCreate(String label, boolean createOption) throws SQLException {
    PacoId labelId = new PacoId();
    
    Long exStringId = getExternStringId(label);
    if (exStringId != null) { 
      labelId.setId(exStringId);
      labelId.setIsCreatedWithThisCall(false);
    } else if (createOption) {
      labelId.setId(insertLabelAndRetrieveId(label));
      labelId.setIsCreatedWithThisCall(true);
    } else {
      //TODO not sure if this is a good option to set to 0
      labelId.setId(0L);
      labelId.setIsCreatedWithThisCall(false);
    }
    return labelId;
  }
  
  private Long getExternStringId(String label) throws SQLException{
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementGetIdForLabel = null;
    Long id = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementGetIdForLabel = conn.prepareStatement(QueryConstants.GET_INPUT_TEXT_ID_FOR_STRING.toString());
      if (label == null) {
        label = "";
      }
      statementGetIdForLabel.setString(1, label);
      rs = statementGetIdForLabel.executeQuery();
      while (rs.next()) {
        id = rs.getLong(ExternStringInputColumns.EXTERN_STRING_INPUT_ID);
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementGetIdForLabel != null) {
          statementGetIdForLabel.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return id;
  }
  
  private Long insertLabelAndRetrieveId(String text) throws SQLException {
    Connection conn = null;
    PreparedStatement statementCreateText = null;
    ResultSet rs = null;
    ExpressionList insertTextExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert textInsert = new Insert();
    Long textId = null;
    if (text == null) {
      text = "";
    }
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      textInsert.setTable(new Table(ExternStringInputColumns.TABLE_NAME));
      textInsert.setUseValues(true);
      insertTextExprList.setExpressions(exp);
      textInsert.setItemsList(insertTextExprList);
      textInsert.setColumns(labelColList);
      // Adding ? for prepared stmt
      for (Column c : labelColList) {
        ((ExpressionList) textInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      statementCreateText = conn.prepareStatement(textInsert.toString(), Statement.RETURN_GENERATED_KEYS);
      statementCreateText.setString(1, text);
      statementCreateText.execute();
      rs = statementCreateText.getGeneratedKeys();
      if (rs.next()) {
        textId = rs.getLong(1);
      }
      conn.commit();
    } catch(SQLException sqle) {
      log.warning("Exception while inserting to extern string text table" + text + ":" +  sqle);
      throw sqle;
    }
    finally {
      try {
        if( rs != null) { 
          rs.close();
        }
        if (statementCreateText != null) {
          statementCreateText.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.info(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return textId;
  }

}

package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import com.google.cloud.sql.jdbc.Statement;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.cloudsql.columns.ExternStringListLabelColumns;
import com.google.sampling.experiential.dao.CSExternStringListLabelDao;
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

public class CSExternStringListLabelDaoImpl implements CSExternStringListLabelDao{
  public static final Logger log = Logger.getLogger(CSExternStringListLabelDaoImpl.class.getName());
  private static List<Column> labelColList = Lists.newArrayList();
  static {
    labelColList.add(new Column(ExternStringListLabelColumns.LABEL));
  }
  
  @Override
  public PacoId getListLabelAndCreate(String label, boolean createOption) throws SQLException {
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
      statementGetIdForLabel = conn.prepareStatement(QueryConstants.GET_LABEL_ID_FOR_STRING.toString());
      statementGetIdForLabel.setString(1, label);
      rs = statementGetIdForLabel.executeQuery();
      while (rs.next()) {
        id = rs.getLong(ExternStringListLabelColumns.EXTERN_STRING_LIST_LABEL_ID);
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
  
  private Long insertLabelAndRetrieveId(String label) {
    Connection conn = null;
    PreparedStatement statementCreateLabel = null;
    ResultSet rs = null;
    ExpressionList insertLabelExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert labelInsert = new Insert();
    Long labelId = null;
    if (label != null) {
      try {
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        conn.setAutoCommit(false);
        labelInsert.setTable(new Table(ExternStringListLabelColumns.TABLE_NAME));
        labelInsert.setUseValues(true);
        insertLabelExprList.setExpressions(exp);
        labelInsert.setItemsList(insertLabelExprList);
        labelInsert.setColumns(labelColList);
        // Adding ? for prepared stmt
        for (Column c : labelColList) {
          ((ExpressionList) labelInsert.getItemsList()).getExpressions().add(new JdbcParameter());
        }
  
        statementCreateLabel = conn.prepareStatement(labelInsert.toString(), Statement.RETURN_GENERATED_KEYS);
        statementCreateLabel.setString(1, label);
        statementCreateLabel.execute();
        rs = statementCreateLabel.getGeneratedKeys();
        if (rs.next()) {
          labelId = rs.getLong(1);
        }
        conn.commit();
      } catch(SQLException sqle) {
        log.warning("Exception while inserting to listlabel table" + label + ":" +  sqle);
      }
      finally {
        try {
          if( rs != null) { 
            rs.close();
          }
          if (statementCreateLabel != null) {
            statementCreateLabel.close();
          }
          if (conn != null) {
            conn.close();
          }
        } catch (SQLException ex1) {
          log.info(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
        }
      }
    } 
    return labelId;
  }
}

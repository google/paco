package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;

import com.google.cloud.sql.jdbc.Statement;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.cloudsql.columns.GroupDetailColumns;
import com.google.sampling.experiential.dao.CSGroupDao;
import com.google.sampling.experiential.dao.dataaccess.GroupDetail;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.PacoId;
import com.pacoapp.paco.shared.util.ErrorMessages;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

public class CSGroupDaoImpl implements CSGroupDao {
  public static final Logger log = Logger.getLogger(CSGroupDaoImpl.class.getName());
  private static List<Column> groupColList = Lists.newArrayList();
 
  static {
    groupColList.add(new Column(GroupDetailColumns.NAME));
    groupColList.add(new Column(GroupDetailColumns.GROUP_TYPE_ID));
    groupColList.add(new Column(GroupDetailColumns.CUSTOM_RENDERING));
    groupColList.add(new Column(GroupDetailColumns.FIXED_DURATION));
    groupColList.add(new Column(GroupDetailColumns.START_DATE));
    groupColList.add(new Column(GroupDetailColumns.END_DATE));
    groupColList.add(new Column(GroupDetailColumns.RAW_DATA_ACCESS));
    groupColList.add(new Column(GroupDetailColumns.END_OF_DAY_GROUP));
  }

  @Override
  public void insertGroup(List<GroupDetail> groups) throws SQLException {
    Connection conn = null;
    PreparedStatement statementCreateGroup = null;
    ResultSet rs = null;
    ExpressionList insertGroupExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert groupInsert = new Insert();

    try {
//      log.info("Inserting group into group table" );
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      groupInsert.setTable(new Table(GroupDetailColumns.TABLE_NAME));
      groupInsert.setUseValues(true);
      insertGroupExprList.setExpressions(exp);
      groupInsert.setItemsList(insertGroupExprList);
      groupInsert.setColumns(groupColList);
      // Adding ? for prepared stmt
      for (Column c : groupColList) {
        ((ExpressionList) groupInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }
      
      for (GroupDetail group : groups) {
        if (group.getGroupId() == null ) {
          statementCreateGroup = conn.prepareStatement(groupInsert.toString(), Statement.RETURN_GENERATED_KEYS);
          
          statementCreateGroup.setString(1, group.getName());
          statementCreateGroup.setInt(2, group.getGroupTypeId());
          statementCreateGroup.setString(3, group.getCustomRendering());
          statementCreateGroup.setBoolean(4, group.getFixedDuration());
          statementCreateGroup.setTimestamp(5, group.getStartDate() != null ? new Timestamp(group.getStartDate().getMillis()): null);
          statementCreateGroup.setTimestamp(6, group.getEndDate() != null ? new Timestamp(group.getEndDate().getMillis()): null);
          statementCreateGroup.setBoolean(7, group.getRawDataAccess());
          statementCreateGroup.setString(8, group.getEndOfDayGroup());
          statementCreateGroup.execute();
          rs = statementCreateGroup.getGeneratedKeys();
          if (rs.next()) {
            group.setGroupId(new PacoId(rs.getLong(1), true)); 
          }
        }
      }
      conn.commit();
    } catch(SQLException sqle) {
      log.warning("Exception while inserting to group_detail table:" +  sqle);
      throw sqle;
    }
    finally {
      try {
        if( rs != null) { 
          rs.close();
        }
        if (statementCreateGroup != null) {
          statementCreateGroup.close();
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

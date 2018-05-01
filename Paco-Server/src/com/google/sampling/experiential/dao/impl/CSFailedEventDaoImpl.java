package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.cloudsql.columns.FailedEventServerColumns;
import com.google.sampling.experiential.dao.CSFailedEventDao;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.QueryConstants;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

public class CSFailedEventDaoImpl implements CSFailedEventDao {
  public static final Logger log = Logger.getLogger(CSFailedEventDaoImpl.class.getName());
  private static List<Column> failedColList = Lists.newArrayList();
  static {
    failedColList.add(new Column(FailedEventServerColumns.EVENT_JSON));
    failedColList.add(new Column(FailedEventServerColumns.REASON));
    failedColList.add(new Column(FailedEventServerColumns.COMMENTS));
    failedColList.add(new Column(FailedEventServerColumns.FAILED_INSERT_TIME));
    failedColList.add(new Column(FailedEventServerColumns.REPROCESSED));
  }
  @Override
  public boolean insertFailedEvent(String failedJson, String reason, String comments) {
    Connection conn = null;
    PreparedStatement statementCreateFailedEvent = null;
    boolean retVal = false;
    ExpressionList failedEventExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert failedEventInsert = new Insert();

    try {
      log.info("Inserting failed event");
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      failedEventInsert.setTable(new Table(FailedEventServerColumns.TABLE_NAME));
      failedEventInsert.setUseValues(true);
      failedEventExprList.setExpressions(exp);
      failedEventInsert.setItemsList(failedEventExprList);
      failedEventInsert.setColumns(failedColList);
      // Adding ? for prepared stmt
      for (Column c : failedColList) {
        ((ExpressionList) failedEventInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      statementCreateFailedEvent = conn.prepareStatement(failedEventInsert.toString());
      statementCreateFailedEvent.setString(1, failedJson);
      statementCreateFailedEvent.setString(2, reason);
      statementCreateFailedEvent.setString(3, comments);
      statementCreateFailedEvent.setTimestamp(4, new Timestamp(new DateTime().getMillis()));
      statementCreateFailedEvent.setString(5, Constants.FALSE);

      statementCreateFailedEvent.execute();
      conn.commit();
      retVal = true;
    } catch(SQLException sqle) {
      log.info("Exception while inserting to failed events table" + failedJson + ":" +  sqle);
    }
    finally {
      try {
        if (statementCreateFailedEvent != null) {
          statementCreateFailedEvent.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.info(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return retVal;
  }

  @Override
  public Map<Long, String> getFailedEvents() throws SQLException {
    Map<Long, String> failedEventsMap = Maps.newHashMap();
    Connection conn = null;
    PreparedStatement statementSelectFailedEvents = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementSelectFailedEvents = conn.prepareStatement(QueryConstants.GET_ALL_UNPROCESSED_FAILED_EVENTS.toString());
      String eventsJson = null;
      Long failedEventId = null;
      ResultSet rs = statementSelectFailedEvents.executeQuery();
      while(rs.next()){
        failedEventId = rs.getLong(FailedEventServerColumns.ID);
        eventsJson = rs.getString(FailedEventServerColumns.EVENT_JSON);
        failedEventsMap.put(failedEventId, eventsJson);
      }
    } finally {
      try {
        if (statementSelectFailedEvents != null) {
          statementSelectFailedEvents.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    return failedEventsMap;
  }
  
  @Override
  public boolean updateFailedEventsRetry(Long id, String reprocessed) throws SQLException {
    boolean isSuccess = false;
    Connection conn = null;
    PreparedStatement statementUpdateFailedEvents = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      statementUpdateFailedEvents = conn.prepareStatement(QueryConstants.UPDATE_FAILED_EVENTS_PROCESSED_STATUS_FOR_ID.toString());
      statementUpdateFailedEvents.setString(1, reprocessed);
      statementUpdateFailedEvents.setLong(2, id);
      statementUpdateFailedEvents.executeUpdate();
      conn.commit();
      isSuccess = true;
    } finally {
      try {
        if (statementUpdateFailedEvents != null) {
          statementUpdateFailedEvents.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    return isSuccess;
  }


}

package com.google.sampling.experiential.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.cloud.sql.jdbc.Statement;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.datastore.EventServerColumns;
import com.google.sampling.experiential.datastore.ExperimentLookupColumns;
import com.google.sampling.experiential.datastore.ExperimentUserColumns;
import com.google.sampling.experiential.datastore.FailedEventServerColumns;
import com.google.sampling.experiential.datastore.UserColumns;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.WhatDAO;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;
import com.pacoapp.paco.shared.util.TimeUtil;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.update.Update;

public class CloudSQLDaoImpl implements CloudSQLDao {
  public static final Logger log = Logger.getLogger(CloudSQLDaoImpl.class.getName());
  private static List<Column> eventColInsertList = Lists.newArrayList();
  private static List<Column> eventColSearchList = Lists.newArrayList();
  public static final String WHEN = "when";
  private static List<Column> outputColList = Lists.newArrayList();
  private static List<Column> failedColList = Lists.newArrayList();
  private static List<Column> userColList = Lists.newArrayList();
  private static List<Column> experimentUserColList = Lists.newArrayList();
  private static List<Column> experimentLookupColList = Lists.newArrayList();
  private static List<Column> eventColList = Lists.newArrayList();
  
  static {
    eventColSearchList.add(new Column(EventServerColumns.EXPERIMENT_ID));
    eventColSearchList.add(new Column(EventServerColumns.EXPERIMENT_NAME));
    eventColSearchList.add(new Column(EventServerColumns.EXPERIMENT_VERSION));
    eventColSearchList.add(new Column(EventServerColumns.GROUP_NAME));
    
    eventColInsertList.add(new Column(EventServerColumns.SCHEDULE_TIME_UTC));
    eventColInsertList.add(new Column(EventServerColumns.RESPONSE_TIME_UTC));
    eventColInsertList.add(new Column(EventServerColumns.ACTION_TRIGGER_ID));
    eventColInsertList.add(new Column(EventServerColumns.ACTION_TRIGGER_SPEC_ID));
    eventColInsertList.add(new Column(EventServerColumns.ACTION_ID));
    eventColInsertList.add(new Column(EventServerColumns.WHO));
    eventColInsertList.add(new Column(EventServerColumns.WHEN));
    eventColInsertList.add(new Column(EventServerColumns.WHEN_FRAC_SEC));
    eventColInsertList.add(new Column(EventServerColumns.PACO_VERSION));
    eventColInsertList.add(new Column(EventServerColumns.APP_ID));
    eventColInsertList.add(new Column(EventServerColumns.JOINED));
    eventColInsertList.add(new Column(EventServerColumns.SORT_DATE_UTC));
    eventColInsertList.add(new Column(EventServerColumns.CLIENT_TIME_ZONE));
    eventColInsertList.add(new Column(Constants.UNDERSCORE_ID));
    eventColInsertList.add(new Column(EventServerColumns.RESPONSE_TIME));
    eventColInsertList.add(new Column(EventServerColumns.SCHEDULE_TIME));
    eventColInsertList.add(new Column(EventServerColumns.SORT_DATE));
    eventColInsertList.add(new Column(EventServerColumns.EXPERIMENT_LOOKUP_ID));
    eventColSearchList.addAll(eventColInsertList);
    
    outputColList.add(new Column(OutputBaseColumns.EVENT_ID));
    outputColList.add(new Column(OutputBaseColumns.NAME));
    outputColList.add(new Column(OutputBaseColumns.ANSWER));

    failedColList.add(new Column(FailedEventServerColumns.EVENT_JSON));
    failedColList.add(new Column(FailedEventServerColumns.REASON));
    failedColList.add(new Column(FailedEventServerColumns.COMMENTS));
    failedColList.add(new Column(FailedEventServerColumns.REPROCESSED));
    
    userColList.add(new Column(UserColumns.WHO));
    
    experimentUserColList.add(new Column(ExperimentUserColumns.EXPERIMENT_ID));
    experimentUserColList.add(new Column(ExperimentUserColumns.USER_ID));
    experimentUserColList.add(new Column(ExperimentUserColumns.EXP_USER_ANON_ID));
    experimentUserColList.add(new Column(ExperimentUserColumns.USER_TYPE));
    
    experimentLookupColList.add(new Column(ExperimentLookupColumns.EXPERIMENT_ID));
    experimentLookupColList.add(new Column(ExperimentLookupColumns.EXPERIMENT_NAME));
    experimentLookupColList.add(new Column(ExperimentLookupColumns.GROUP_NAME));
    experimentLookupColList.add(new Column(ExperimentLookupColumns.EXPERIMENT_VERSION));
    
    eventColList.add(new Column(EventServerColumns.EXPERIMENT_ID));
    eventColList.add(new Column(EventServerColumns.EXPERIMENT_NAME));
    eventColList.add(new Column(EventServerColumns.EXPERIMENT_VERSION));
    eventColList.add(new Column(EventServerColumns.SCHEDULE_TIME_UTC));
    eventColList.add(new Column(EventServerColumns.RESPONSE_TIME_UTC));
    eventColList.add(new Column(EventServerColumns.GROUP_NAME));
    eventColList.add(new Column(EventServerColumns.ACTION_TRIGGER_ID));
    eventColList.add(new Column(EventServerColumns.ACTION_TRIGGER_SPEC_ID));
    eventColList.add(new Column(EventServerColumns.ACTION_ID));
    eventColList.add(new Column(EventServerColumns.WHO));
    eventColList.add(new Column(EventServerColumns.WHEN));
    eventColList.add(new Column(EventServerColumns.WHEN_FRAC_SEC));
    eventColList.add(new Column(EventServerColumns.PACO_VERSION));
    eventColList.add(new Column(EventServerColumns.APP_ID));
    eventColList.add(new Column(EventServerColumns.JOINED));
    eventColList.add(new Column(EventServerColumns.SORT_DATE_UTC));
    eventColList.add(new Column(EventServerColumns.CLIENT_TIME_ZONE));
    eventColList.add(new Column(Constants.UNDERSCORE_ID));
    eventColList.add(new Column(EventServerColumns.RESPONSE_TIME));
    eventColList.add(new Column(EventServerColumns.SCHEDULE_TIME));
    eventColList.add(new Column(EventServerColumns.SORT_DATE));

  }

  @Override
  public boolean insertEventAndOutputs(Event event) throws SQLException, ParseException {
    if (event == null) {
      log.warning(ErrorMessages.NOT_VALID_DATA.getDescription());
      return false;
    }

    Connection conn = null;
    PreparedStatement statementCreateEvent = null;
    PreparedStatement statementCreateEventOutput = null;
    boolean retVal = false;
    Timestamp whenTs = null;
    int whenFrac = 0;
    //startCount for setting parameter index
    int i = 1 ;
    ExpressionList eventExprList = new ExpressionList();
    ExpressionList outputExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    List<Expression>  out = Lists.newArrayList();
    Insert eventInsert = new Insert();
    Insert outputInsert = new Insert();
    Long expIdLong = null;

    try {
      log.info("Inserting event->" + event.getId());
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      setNames(conn);
      conn.setAutoCommit(false);
      eventInsert.setTable(new Table(EventServerColumns.TABLE_NAME));
      outputInsert.setTable(new Table(OutputBaseColumns.TABLE_NAME));
      eventInsert.setUseValues(true);
      outputInsert.setUseValues(true);
      eventExprList.setExpressions(exp);
      outputExprList.setExpressions(out);
      eventInsert.setItemsList(eventExprList);
      outputInsert.setItemsList(outputExprList);
      eventInsert.setColumns(eventColInsertList);
      outputInsert.setColumns(outputColList);
      // Adding ? for prepared stmt
      for (Column c : eventColInsertList) {
        ((ExpressionList) eventInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      for (Column c : outputColList) {
        ((ExpressionList) outputInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }
      expIdLong = Long.parseLong(event.getExperimentId());

      statementCreateEvent = conn.prepareStatement(eventInsert.toString());
      statementCreateEvent.setTimestamp(i++, event.getScheduledTime() != null ? new Timestamp(event.getScheduledTime().getTime()): null);
      statementCreateEvent.setTimestamp(i++, event.getResponseTime() != null ? new Timestamp(event.getResponseTime().getTime()): null);
      statementCreateEvent.setLong(i++, event.getActionTriggerId() != null ? new Long(event.getActionTriggerId()) : java.sql.Types.NULL);
      statementCreateEvent.setLong(i++, event.getActionTriggerSpecId() != null ? new Long(event.getActionTriggerId()) : java.sql.Types.NULL);
      statementCreateEvent.setLong(i++, event.getActionId() != null ? new Long(event.getActionId()) : java.sql.Types.NULL);
      PacoId anonId = getAnonymousIdAndCreate(expIdLong, event.getWho(), true);
      statementCreateEvent.setString(i++, anonId.getId().toString());
      if (event.getWhen() != null) {
        whenTs = new Timestamp(event.getWhen().getTime());
        whenFrac = com.google.sampling.experiential.server.TimeUtil.getFractionalSeconds(whenTs);
      }
      statementCreateEvent.setTimestamp(i++, whenTs);
      statementCreateEvent.setInt(i++, whenFrac);
      statementCreateEvent.setString(i++, event.getPacoVersion());
      statementCreateEvent.setString(i++, event.getAppId());
      Boolean joinFlag = null;
      if (event.getWhat() != null) {
        String joinedStat = event.getWhatByKey(EventServerColumns.JOINED);
        if (joinedStat != null) {
          if (joinedStat.equalsIgnoreCase(Constants.TRUE)) {
            joinFlag = true;
          } else {
            joinFlag = false;
          }
        }
      }
      if (joinFlag == null) {
        statementCreateEvent.setNull(i++, java.sql.Types.BOOLEAN);
      } else {
        statementCreateEvent.setBoolean(i++, joinFlag);
      }
      Long sortDateMillis = null;
      if (event.getResponseTime() != null) {
        sortDateMillis = event.getResponseTime().getTime();
      } else {
        sortDateMillis = event.getScheduledTime().getTime();
      }
      statementCreateEvent.setTimestamp(i++, new Timestamp(sortDateMillis));
      statementCreateEvent.setString(i++, event.getTimeZone());
      statementCreateEvent.setLong(i++, event.getId());
      statementCreateEvent.setTimestamp(i++, event.getResponseTime() != null ? new Timestamp(TimeUtil.convertToLocal(event.getResponseTime(), event.getTimeZone()).getMillis()): null);
      statementCreateEvent.setTimestamp(i++, event.getScheduledTime() != null ? new Timestamp(TimeUtil.convertToLocal(event.getScheduledTime(), event.getTimeZone()).getMillis()): null);
      statementCreateEvent.setTimestamp(i++, new Timestamp(TimeUtil.convertToLocal(new Date(sortDateMillis), event.getTimeZone()).getMillis()));
      PacoId experimentLookupId = getExperimentLookupIdAndCreate(expIdLong, event.getExperimentName(), event.getExperimentGroupName(), event.getExperimentVersion(), true);
      statementCreateEvent.setInt(i++, experimentLookupId.getId().intValue());
      
      statementCreateEvent.execute();
      Set<What> whatSet = event.getWhat();
      if (whatSet != null) {
        statementCreateEventOutput = conn.prepareStatement(outputInsert.toString());
        for (String key : event.getWhatKeys()) {
          String whatAnswer = event.getWhatByKey(key);
          statementCreateEventOutput.setLong(1, event.getId());
          statementCreateEventOutput.setString(2, key);
          statementCreateEventOutput.setString(3, whatAnswer);
          statementCreateEventOutput.addBatch();
        }
        statementCreateEventOutput.executeBatch();
      }
      
      conn.commit();
      retVal = true;
    } finally {
      try {
        if (statementCreateEventOutput != null) {
          statementCreateEventOutput.close();
        }
        if (statementCreateEvent != null) {
          statementCreateEvent.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return retVal;
  }
  
  @Override
  public boolean insertSingleEventOnly(Event event) throws SQLException, ParseException {
    if (event == null) {
      log.warning(ErrorMessages.NOT_VALID_DATA.getDescription());
      return false;
    }

    Connection conn = null;
    PreparedStatement statementCreateEvent = null;
    boolean retVal = false;
    Timestamp whenTs = null;
    Long expIdLong = null;
    int whenFrac = 0;
    //startCount for setting parameter index
    int i = 1 ;
    ExpressionList eventExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert eventInsert = new Insert();
     try {
      log.info("Inserting event->" + event.getId());
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      setNames(conn);
      conn.setAutoCommit(false);
      expIdLong = Long.parseLong(event.getExperimentId());
      eventInsert.setTable(new Table(EventServerColumns.TABLE_NAME));
      eventInsert.setUseValues(true);
      eventExprList.setExpressions(exp);
      eventInsert.setItemsList(eventExprList);
      eventInsert.setColumns(eventColInsertList);
      // Adding ? for prepared stmt
      for (Column c : eventColInsertList) {
        ((ExpressionList) eventInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      statementCreateEvent = conn.prepareStatement(eventInsert.toString());
      statementCreateEvent.setTimestamp(i++, event.getScheduledTime() != null ? new Timestamp(event.getScheduledTime().getTime()): null);
      statementCreateEvent.setTimestamp(i++, event.getResponseTime() != null ? new Timestamp(event.getResponseTime().getTime()): null);
      statementCreateEvent.setLong(i++, event.getActionTriggerId() != null ? new Long(event.getActionTriggerId()) : java.sql.Types.NULL);
      statementCreateEvent.setLong(i++, event.getActionTriggerSpecId() != null ? new Long(event.getActionTriggerId()) : java.sql.Types.NULL);
      statementCreateEvent.setLong(i++, event.getActionId() != null ? new Long(event.getActionId()) : java.sql.Types.NULL);
      PacoId anonId = getAnonymousIdAndCreate(expIdLong, event.getWho(), true);
      statementCreateEvent.setString(i++, anonId.getId().toString());
      if (event.getWhen() != null) {
        whenTs = new Timestamp(event.getWhen().getTime());
        whenFrac = com.google.sampling.experiential.server.TimeUtil.getFractionalSeconds(whenTs);
      }
      statementCreateEvent.setTimestamp(i++, whenTs);
      statementCreateEvent.setInt(i++, whenFrac);
      statementCreateEvent.setString(i++, event.getPacoVersion());
      statementCreateEvent.setString(i++, event.getAppId());
      Boolean joinFlag = null;
      if (event.getWhat() != null) {
        String joinedStat = event.getWhatByKey(EventServerColumns.JOINED);
        if (joinedStat != null) {
          if (joinedStat.equalsIgnoreCase(Constants.TRUE)) {
            joinFlag = true;
          } else {
            joinFlag = false;
          }
        }
      }
      if (joinFlag == null) {
        statementCreateEvent.setNull(i++, java.sql.Types.BOOLEAN);
      } else {
        statementCreateEvent.setBoolean(i++, joinFlag);
      }
      Long sortDateMillis = null;
      if (event.getResponseTime() != null) {
        sortDateMillis = event.getResponseTime().getTime();
      } else {
        sortDateMillis = event.getScheduledTime().getTime();
      }
      statementCreateEvent.setTimestamp(i++, new Timestamp(sortDateMillis));
      statementCreateEvent.setString(i++, event.getTimeZone());
      statementCreateEvent.setLong(i++, event.getId());
      statementCreateEvent.setTimestamp(i++, event.getResponseTime() != null ? new Timestamp(TimeUtil.convertToLocal(event.getResponseTime(), event.getTimeZone()).getMillis()): null);
      statementCreateEvent.setTimestamp(i++, event.getScheduledTime() != null ? new Timestamp(TimeUtil.convertToLocal(event.getScheduledTime(), event.getTimeZone()).getMillis()): null);
      statementCreateEvent.setTimestamp(i++, new Timestamp(TimeUtil.convertToLocal(new Date(sortDateMillis), event.getTimeZone()).getMillis()));
      PacoId experimentLookupId = getExperimentLookupIdAndCreate(Long.parseLong(event.getExperimentId()), event.getExperimentName(), event.getExperimentGroupName(), event.getExperimentVersion(), true);
      statementCreateEvent.setInt(i++, experimentLookupId.getId().intValue());
      
      statementCreateEvent.execute();

      conn.commit();
      retVal = true;
    } finally {
      try {
        if (statementCreateEvent != null) {
          statementCreateEvent.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return retVal;
  }

  @Override
  public boolean insertSingleEventOnlyWithExperimentInfo(Event event) throws SQLException, ParseException {
    if (event == null) {
      log.warning(ErrorMessages.NOT_VALID_DATA.getDescription());
      return false;
    }

    Connection conn = null;
    PreparedStatement statementCreateEvent = null;
    boolean retVal = false;
    Timestamp whenTs = null;
    int whenFrac = 0;
    //startCount for setting parameter index
    int i = 1 ;
    ExpressionList eventExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert eventInsert = new Insert();
     try {
      log.info("Inserting event->" + event.getId());
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      setNames(conn);
      conn.setAutoCommit(false);
      eventInsert.setTable(new Table(EventServerColumns.TABLE_NAME));
      eventInsert.setUseValues(true);
      eventExprList.setExpressions(exp);
      eventInsert.setItemsList(eventExprList);
      eventInsert.setColumns(eventColList);
      // Adding ? for prepared stmt
      for (Column c : eventColList) {
        ((ExpressionList) eventInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      statementCreateEvent = conn.prepareStatement(eventInsert.toString());
      statementCreateEvent.setLong(i++, Long.parseLong(event.getExperimentId()));
      statementCreateEvent.setString(i++, event.getExperimentName());
      statementCreateEvent.setInt(i++, event.getExperimentVersion());
      statementCreateEvent.setTimestamp(i++, event.getScheduledTime() != null ? new Timestamp(event.getScheduledTime().getTime()): null);
      statementCreateEvent.setTimestamp(i++, event.getResponseTime() != null ? new Timestamp(event.getResponseTime().getTime()): null);
      statementCreateEvent.setString(i++, event.getExperimentGroupName());
      statementCreateEvent.setLong(i++, event.getActionTriggerId() != null ? new Long(event.getActionTriggerId()) : java.sql.Types.NULL);
      statementCreateEvent.setLong(i++, event.getActionTriggerSpecId() != null ? new Long(event.getActionTriggerId()) : java.sql.Types.NULL);
      statementCreateEvent.setLong(i++, event.getActionId() != null ? new Long(event.getActionId()) : java.sql.Types.NULL);
      statementCreateEvent.setString(i++, event.getWho());
      if (event.getWhen() != null) {
        whenTs = new Timestamp(event.getWhen().getTime());
        whenFrac = com.google.sampling.experiential.server.TimeUtil.getFractionalSeconds(whenTs);
      }
      statementCreateEvent.setTimestamp(i++, whenTs);
      statementCreateEvent.setInt(i++, whenFrac);
      statementCreateEvent.setString(i++, event.getPacoVersion());
      statementCreateEvent.setString(i++, event.getAppId());
      Boolean joinFlag = null;
      if (event.getWhat() != null) {
        String joinedStat = event.getWhatByKey(EventServerColumns.JOINED);
        if (joinedStat != null) {
          if (joinedStat.equalsIgnoreCase(Constants.TRUE)) {
            joinFlag = true;
          } else {
            joinFlag = false;
          }
        }
      }
      if (joinFlag == null) {
        statementCreateEvent.setNull(i++, java.sql.Types.BOOLEAN);
      } else {
        statementCreateEvent.setBoolean(i++, joinFlag);
      }
      Long sortDateMillis = null;
      if (event.getResponseTime() != null) {
        sortDateMillis = event.getResponseTime().getTime();
      } else {
        sortDateMillis = event.getScheduledTime().getTime();
      }
      statementCreateEvent.setTimestamp(i++, new Timestamp(sortDateMillis));
      statementCreateEvent.setString(i++, event.getTimeZone());
      statementCreateEvent.setLong(i++, event.getId());
      statementCreateEvent.setTimestamp(i++, event.getResponseTime() != null ? new Timestamp(TimeUtil.convertToLocal(event.getResponseTime(), event.getTimeZone()).getMillis()): null);
      statementCreateEvent.setTimestamp(i++, event.getScheduledTime() != null ? new Timestamp(TimeUtil.convertToLocal(event.getScheduledTime(), event.getTimeZone()).getMillis()): null);
      statementCreateEvent.setTimestamp(i++, new Timestamp(TimeUtil.convertToLocal(new Date(sortDateMillis), event.getTimeZone()).getMillis()));

      statementCreateEvent.execute();

      conn.commit();
      retVal = true;
    } finally {
      try {
        if (statementCreateEvent != null) {
          statementCreateEvent.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return retVal;
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
      setNames(conn);
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
  public List<EventDAO> getEvents(String query, boolean withOutputs) throws SQLException, ParseException {
    List<EventDAO> evtDaoList = Lists.newArrayList();
    EventDAO event = null;
    Connection conn = null;
    Map<Long, EventDAO> eventMap = null;
    PreparedStatement statementSelectEvent = null;
    ResultSet rs = null;

    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      setNames(conn);
      // While we have to retrieve millions of records at a time, we might run
      // into java heap space issue.
      // The following two properties(rs type forward only, concur read only,
      // statement fetch size) of statement are specific to mysql.
      // This signals the driver to stream records one at a time
      statementSelectEvent = conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      statementSelectEvent.setFetchSize(Integer.MIN_VALUE);

      Long st1Time = System.currentTimeMillis();
      String selString = statementSelectEvent.toString();
      log.info("step 1 " + selString.substring(selString.indexOf(":")));
      rs = statementSelectEvent.executeQuery();
      if (rs != null) {
        // to maintain the insertion order
        eventMap = Maps.newLinkedHashMap();
        while (rs.next()) {
          event = createEvent(rs, withOutputs);
          com.google.sampling.experiential.server.TimeUtil.adjustTimeZone(event);
          // to group list of whats into event
          EventDAO oldEvent = eventMap.get(event.getId());
          if (oldEvent == null) {
            eventMap.put(event.getId(), event);
          } else {
            // add crt what to old event
            List<WhatDAO> newWhat = event.getWhat();
            if (newWhat != null && oldEvent.getWhat() != null) {
              oldEvent.getWhat().addAll(newWhat);
            }
          }
        }
      }
      log.info("query took " + (System.currentTimeMillis() - st1Time));
    } finally {
      try {
        if(rs != null) {
          rs.close();
        }
        if (statementSelectEvent != null) {
          statementSelectEvent.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    if (eventMap != null && eventMap.size() > 0) {
      evtDaoList = Lists.newArrayList(eventMap.values());
    }

    return evtDaoList;
  }

  @Override
  public JSONArray getResultSetAsJson(String query, List<String> dateColumns) throws SQLException, ParseException, JSONException {
    Connection conn = null;
    JSONArray multipleRecords = null;
    PreparedStatement statementSelectEvent = null;
    ResultSet rs = null;
    DateTime dateFromDb = null;
    DateTime dateInLocal = null;
    String offsetHrsStr = null;
    int offsetHrs = 0;
    String colName = null;
    String colValue = null;
    Object anyObject = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      setNames(conn);

      statementSelectEvent = conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      rs = statementSelectEvent.executeQuery();
      if (rs != null) {
        ResultSetMetaData rsmd = rs.getMetaData();
        multipleRecords = new JSONArray();
        JSONObject eachRecord = null;
        while (rs.next()) {
          eachRecord = new JSONObject();
          for ( int i = 1; i <= rsmd.getColumnCount(); i++) {
            colName = rsmd.getColumnName(i);
            anyObject = rs.getObject(i);
            colValue =  anyObject != null ? anyObject.toString() : null;
            //if client timezone, then do not write to json
            if (!(colName.equalsIgnoreCase(EventServerColumns.CLIENT_TIME_ZONE))) {
              //if date columns in projection, then display along with corresponding timezone
              if(dateColumns.contains(colName)) {
                offsetHrsStr =  rs.getString(EventServerColumns.CLIENT_TIME_ZONE);
                offsetHrs = com.google.sampling.experiential.server.TimeUtil.getIntFromOffsetString(offsetHrsStr);
                dateFromDb = new DateTime(rs.getTimestamp(colName).getTime());
                // change the tz with value stored in db
                dateInLocal = dateFromDb.withZoneRetainFields(DateTimeZone.forOffsetHours(offsetHrs));
                colValue = dateInLocal.toString();
              }
              eachRecord.put(colName, colValue);
            }
          }
          multipleRecords.put(eachRecord);
        }
      }
    } finally {
      try {
       if( rs != null) {
         rs.close();
       }
        if (statementSelectEvent != null) {
          statementSelectEvent.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
   return multipleRecords;
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
      setNames(conn);
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

  private EventDAO createEvent(ResultSet rs, boolean withOutputs) {
    EventDAO event = new EventDAO();
    List<WhatDAO> whatList = Lists.newArrayList();
    WhatDAO singleWhat = null;
    try {
      event.setExperimentId(rs.getLong(ExperimentLookupColumns.EXPERIMENT_ID));
      event.setExperimentName(rs.getString(ExperimentLookupColumns.EXPERIMENT_NAME));
      event.setExperimentVersion(rs.getInt(ExperimentLookupColumns.EXPERIMENT_VERSION));

      Date scheduleDate = rs.getTimestamp(EventServerColumns.SCHEDULE_TIME);
      DateTime scheduledDateTime = scheduleDate != null ? new DateTime(scheduleDate): null;
      event.setScheduledTime(scheduledDateTime);

      Date responseDate = rs.getTimestamp(EventServerColumns.RESPONSE_TIME);
      DateTime responseDateTime = responseDate != null ? new DateTime(responseDate) : null;
      event.setResponseTime(responseDateTime);

      event.setExperimentGroupName(rs.getString(ExperimentLookupColumns.GROUP_NAME));
      event.setActionTriggerId(rs.getLong(EventServerColumns.ACTION_TRIGGER_ID));
      event.setActionTriggerSpecId(rs.getLong(EventServerColumns.ACTION_TRIGGER_SPEC_ID));
      event.setActionId(rs.getLong(EventServerColumns.ACTION_ID));
      event.setWho(rs.getString(EventServerColumns.WHO));
      event.setWhen(new DateTime(rs.getTimestamp(WHEN).getTime() + rs.getInt(EventServerColumns.WHEN_FRAC_SEC)));
      event.setPaco_version(rs.getString(EventServerColumns.PACO_VERSION));
      event.setAppId(rs.getString(EventServerColumns.APP_ID));
      event.setJoined(rs.getBoolean(EventServerColumns.JOINED));
      //sort date cannot be null, so its safe to have new datetime
      event.setSortDate(new DateTime(rs.getTimestamp(EventServerColumns.SORT_DATE)));
      event.setTimezone(rs.getString(EventServerColumns.CLIENT_TIME_ZONE));
      event.setId(rs.getLong(Constants.UNDERSCORE_ID));
      if (withOutputs) {
        String tempWhatText = rs.getString(OutputBaseColumns.NAME);
        if(tempWhatText != null) {
          singleWhat = new WhatDAO(tempWhatText, rs.getString(OutputBaseColumns.ANSWER));
          whatList.add(singleWhat);
        }
        event.setWhat(whatList);
      }
    } catch (SQLException sqle) {
      log.warning(ErrorMessages.SQL_EXCEPTION.getDescription() + sqle);
    }

    return event;
  }

  public boolean setNames(Connection conn) throws SQLException {
    boolean isDone = false;
    java.sql.Statement statementSetNames = null;

    try {
      statementSetNames = conn.createStatement();
      statementSetNames.execute(QueryConstants.SET_NAMES.toString());
      isDone = true;
    } finally {
      try {
        if (statementSetNames != null) {
          statementSetNames.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return isDone;
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
      setNames(conn);
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
      statementCreateFailedEvent.setString(4, Constants.FALSE);

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
      setNames(conn);
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
      setNames(conn);
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


  private boolean insertIntoExperimentUsers(Long experimentId, List<PacoUser> users) throws SQLException {
    PreparedStatement statementCreateExperimentUsers = null;
    ExpressionList experimentUserExprList = new ExpressionList();
    List<Expression>  out = Lists.newArrayList();
    Insert experimentUserInsert = new Insert();
    Connection conn = null;
    boolean isSuccess = false;
    if (users == null || users.size() == 0) {
      log.warning("inserting to experiment_users:"+ experimentId + ", with users "+ users.size());
      return false;
    }
    
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      setNames(conn);
      conn.setAutoCommit(false);
      experimentUserInsert.setTable(new Table(ExperimentUserColumns.TABLE_NAME));
      experimentUserInsert.setUseValues(true);
      experimentUserExprList.setExpressions(out);
      experimentUserInsert.setItemsList(experimentUserExprList);
      experimentUserInsert.setColumns(experimentUserColList);
      // Adding ? for prepared stmt
      for (Column c : experimentUserColList) {
        ((ExpressionList) experimentUserInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }
      statementCreateExperimentUsers = conn.prepareStatement(experimentUserInsert.toString());
      for (PacoUser eachUser : users) {
        statementCreateExperimentUsers.setLong(1, experimentId);
        statementCreateExperimentUsers.setLong(2, eachUser.getId());
        statementCreateExperimentUsers.setInt(3, eachUser.getAnonId());
        statementCreateExperimentUsers.setString(4, eachUser.getType().toString());
        statementCreateExperimentUsers.addBatch();
        log.info("inserting to experiment_users:"+ experimentId + ", with user: "+ eachUser.getAnonId());
      }
      statementCreateExperimentUsers.executeBatch();
      conn.commit();
      isSuccess = true;
    } finally {
      try {
        if (statementCreateExperimentUsers != null) {
          statementCreateExperimentUsers.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return isSuccess;
  }
  
  private boolean updateUserTypesForExperiment(Long experimentId, Set<Long> asAdmin, Set<Long> asParticipant) throws SQLException {
    if ((asAdmin == null && asParticipant == null) || (asAdmin.size() == 0 && asParticipant.size() == 0)) {
      log.info("update user types with admin and participant lists empty or null "+ experimentId );
      return false;
    }
    PreparedStatement statementUpdateAsAdminExperimentUsers = null;
    PreparedStatement statementUpdateAsParticipantExperimentUsers = null;
    Update update = new Update(); 
    List<Expression> adminExpressionList = Lists.newArrayList();
    List<Expression> participantExpressionList = Lists.newArrayList();
    ExpressionList adminIds = new ExpressionList();
    ExpressionList participantIds = new ExpressionList();
    
    Connection conn = null;
    boolean isSuccess = false;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      setNames(conn);
      conn.setAutoCommit(false);
      
      List<Expression> updateUserExprLst = Lists.newArrayList();
      update.setExpressions(updateUserExprLst);
      
      List<Table> updTableLst = Lists.newArrayList();
      updTableLst.add(new Table(ExperimentUserColumns.TABLE_NAME));
      
      List<Column> updateUserTypeColumnList = Lists.newArrayList();
      updateUserTypeColumnList.add(new Column(ExperimentUserColumns.USER_TYPE));
      
      for (Column c : updateUserTypeColumnList) {
        updateUserExprLst.add(new JdbcParameter());
      }
    
      EqualsTo experimentIdEqualsToCondition = new EqualsTo();
      experimentIdEqualsToCondition.setLeftExpression(new Column(ExperimentUserColumns.EXPERIMENT_ID));
      experimentIdEqualsToCondition.setRightExpression(new JdbcParameter());
      
      if (asAdmin != null && asAdmin.size() > 0) {
        Iterator<Long> itr = asAdmin.iterator();
        while (itr.hasNext()) {
          adminExpressionList.add(new LongValue(itr.next()));
        }
        adminIds.setExpressions(adminExpressionList);
      }
      if (asParticipant != null && asParticipant.size() > 0) {
        Iterator<Long> itr = asParticipant.iterator();
        while (itr.hasNext()) {
          participantExpressionList.add(new LongValue(itr.next()));
        }
        participantIds.setExpressions(participantExpressionList);
      }
      
      InExpression userIdInCondition = new InExpression();
      userIdInCondition.setLeftExpression(new Column(ExperimentUserColumns.USER_ID));
      
      AndExpression andExpr = new AndExpression(experimentIdEqualsToCondition, userIdInCondition);
      
      update.setColumns(updateUserTypeColumnList);
      update.setTables(updTableLst);
      update.setWhere(andExpr);

      // for admins
      if (asAdmin != null && asAdmin.size() > 0) {
        userIdInCondition.setRightItemsList(adminIds);
        statementUpdateAsAdminExperimentUsers = conn.prepareStatement(update.toString());
        statementUpdateAsAdminExperimentUsers.setString(1, ExperimentUserColumns.ADMIN_TYPE);
        statementUpdateAsAdminExperimentUsers.setLong(2, experimentId);
        log.info(statementUpdateAsAdminExperimentUsers.toString());
        statementUpdateAsAdminExperimentUsers.execute();
      }
      // for participants
      if (asParticipant != null && asParticipant.size() > 0) {
        userIdInCondition.setRightItemsList(participantIds);
        statementUpdateAsParticipantExperimentUsers = conn.prepareStatement(update.toString());
        statementUpdateAsParticipantExperimentUsers.setString(1, ExperimentUserColumns.PARTICIPANT_TYPE);
        statementUpdateAsParticipantExperimentUsers.setLong(2, experimentId);
        log.info(statementUpdateAsParticipantExperimentUsers.toString());
        statementUpdateAsParticipantExperimentUsers.execute();
      }
      conn.commit();
      isSuccess = true;
    } finally {
      try {
        if (statementUpdateAsAdminExperimentUsers != null) {
          statementUpdateAsAdminExperimentUsers.close();
        }
        if (statementUpdateAsParticipantExperimentUsers != null) {
          statementUpdateAsParticipantExperimentUsers.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return isSuccess;
  }
  
  @Override
  public List<PacoUser> getAllUsersForExperiment(Long experimentId) throws SQLException {
    List<PacoUser> pacoUsersForExperiment = Lists.newArrayList();
    Connection conn = null;
    PreparedStatement findAllUsersStatement = null;
    PacoUser pUser = null;
    ResultSet rs = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      setNames(conn);
      findAllUsersStatement = conn.prepareStatement(QueryConstants.GET_ALL_USERS_FOR_EXPERIMENT.toString());
      log.info("Getting all users for experiment Id "+ experimentId);
      findAllUsersStatement.setLong(1, experimentId);
      rs = findAllUsersStatement.executeQuery();
      while(rs.next()){
        pUser = new PacoUser();
        pUser.setType(rs.getString(ExperimentUserColumns.USER_TYPE).charAt(0));
        pUser.setId(rs.getLong(ExperimentUserColumns.USER_ID));
        pUser.setAnonId(rs.getInt(ExperimentUserColumns.EXP_USER_ANON_ID));
        pUser.setEmail(rs.getString(UserColumns.WHO));
        pacoUsersForExperiment.add(pUser);
      }
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (findAllUsersStatement != null) {
          findAllUsersStatement.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    return pacoUsersForExperiment;
  }
  
  @Override
  public Map<String, Long> getUserIdsForEmails(Set<String> userEmailLst) throws SQLException {
    Connection conn = null;
    PreparedStatement findUsersStatement = null;
    ResultSet rs = null;
    Map<String, Long> userIds = Maps.newHashMap();
    int ct = 1;
    try {
      if (userEmailLst != null) {
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        setNames(conn);
        
        List<Expression> lstExpr = Lists.newArrayList();
        Iterator<String> emailIterator = userEmailLst.iterator();
        while (emailIterator.hasNext()) {
          lstExpr.add(new JdbcParameter());
          emailIterator.next();
        }
        
        ExpressionList exprList = new ExpressionList();
        exprList.setExpressions(lstExpr);
        
        InExpression emailInClause = new InExpression();
        emailInClause.setLeftExpression(new Column(UserColumns.WHO));
        emailInClause.setRightItemsList(exprList);
        
        SelectItem userId = new SelectExpressionItem();
        ((SelectExpressionItem)userId).setExpression(new Column(UserColumns.USER_ID));
        SelectItem userEmail = new SelectExpressionItem();
        ((SelectExpressionItem)userEmail).setExpression(new Column(UserColumns.WHO));
        
        List<SelectItem> selectColLst = Lists.newArrayList();
        selectColLst.add(userId);
        selectColLst.add(userEmail);
        
        PlainSelect selectIdEmailQry = new PlainSelect(); 
        selectIdEmailQry.setFromItem(new Table(UserColumns.TABLE_NAME));
        selectIdEmailQry.setSelectItems(selectColLst);
        selectIdEmailQry.setWhere(emailInClause);
  
        findUsersStatement = conn.prepareStatement(selectIdEmailQry.toString());
        for (String s: userEmailLst) {
          findUsersStatement.setString(ct++, s);
        }
        log.info("find all ids" + findUsersStatement.toString());
        rs = findUsersStatement.executeQuery();
        while (rs.next()){
          userIds.put(rs.getString(UserColumns.WHO), rs.getLong(UserColumns.USER_ID));
        }
      } else {
        log.warning("user email list is " +  userEmailLst);
      }
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (findUsersStatement != null) {
          findUsersStatement.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    return userIds;
  }
  
  private Long insertUserAndRetrieveId(String email) throws SQLException {
    Connection conn = null;
    PreparedStatement statementCreateUser = null;
    ResultSet rs = null;
    ExpressionList insertEventExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert userInsert = new Insert();
    Long userId = null;
    if (email != null) {
      try {
        log.info("Inserting user into user table" + email);
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        setNames(conn);
        conn.setAutoCommit(false);
        userInsert.setTable(new Table(UserColumns.TABLE_NAME));
        userInsert.setUseValues(true);
        insertEventExprList.setExpressions(exp);
        userInsert.setItemsList(insertEventExprList);
        userInsert.setColumns(userColList);
        // Adding ? for prepared stmt
        for (Column c : userColList) {
          ((ExpressionList) userInsert.getItemsList()).getExpressions().add(new JdbcParameter());
        }
  
        statementCreateUser = conn.prepareStatement(userInsert.toString(), Statement.RETURN_GENERATED_KEYS);
        statementCreateUser.setString(1, email);
        statementCreateUser.execute();
        rs = statementCreateUser.getGeneratedKeys();
        if (rs.next()) {
          userId = rs.getLong(1);
        }
        conn.commit();
      } catch(SQLException sqle) {
        log.warning("Exception while inserting to user table" + email + ":" +  sqle);
      }
      finally {
        try {
          if( rs != null) { 
            rs.close();
          }
          if (statementCreateUser != null) {
            statementCreateUser.close();
          }
          if (conn != null) {
            conn.close();
          }
        } catch (SQLException ex1) {
          log.info(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
        }
      }
    } else {
      log.warning("insert user email:"+ email);
    }
    return userId;
  }
  
  private void insertUserForExperiment(Long experimentId, String email) throws SQLException {
    PacoUser pacoUser = null;
    Integer newAnonId = null;
    if (experimentId == null || email == null) {
      experimentId = 0L;
      log.info("create Anon Id with expId" + experimentId + " with email : "+ email);
      return;
    } 
   
    try { 
      PacoId userId = getUseridAndCreate(email, true);
      List<PacoUser> toBeInsertedIntoExptUserTable = Lists.newArrayList();

      // Check the current admin status for email for this experiment and then update experiment_users table
      boolean isAdmin = ExperimentAccessManager.isAdminForExperiment(email, experimentId);
      newAnonId = getMaxAnonId(experimentId) + 1;
      if (isAdmin) {
        pacoUser = new PacoUser(userId.getId(), newAnonId, ExperimentUserColumns.ADMIN_TYPE.charAt(0), email);
      } else {
        pacoUser = new PacoUser(userId.getId(), newAnonId, ExperimentUserColumns.PARTICIPANT_TYPE.charAt(0), email);
      }
      toBeInsertedIntoExptUserTable.add(pacoUser);
      insertIntoExperimentUsers(experimentId, toBeInsertedIntoExptUserTable);
    } catch (SQLException sqle) {
      insertFailedEvent(experimentId.toString(), ErrorMessages.SQL_INSERT_EXCEPTION.getDescription() + "Admin/Participant", sqle.getMessage());
      log.warning(ErrorMessages.SQL_INSERT_EXCEPTION.getDescription() + " for  Admin/ Participant request: " + experimentId + " : " + ExceptionUtil.getStackTraceAsString(sqle));
    } catch (Exception e) {
      insertFailedEvent(experimentId.toString(), ErrorMessages.GENERAL_EXCEPTION.getDescription(), e.getMessage());
      log.warning(ErrorMessages.GENERAL_EXCEPTION.getDescription() + " for  Admin/ Participant request: " + experimentId + " : " + ExceptionUtil.getStackTraceAsString(e));
    }
  }
 
  @Override
  public void ensureUserId(Long expId, Set<String> adminEmailsInRequest, Set<String> participantEmailsInRequest) {
    if (adminEmailsInRequest == null && participantEmailsInRequest == null) {
      return;
    }
    
    // insert to cloud sql
    List<PacoUser> pacoUsersInDb = null;
    Set<Long> adminIdsInDb = Sets.newHashSet();
    Set<Long> participantIdsInDb = Sets.newHashSet();
    Set<Long> adminIdsInRequest = Sets.newHashSet();
    Set<Long> participantIdsInRequest = Sets.newHashSet();
    List<PacoUser> toBeInsertedIntoExptUserTable = Lists.newArrayList();
    Set<Long> idsToBeUpdatedAsAdmin = Sets.newHashSet();
    Set<Long> idsToBeUpdatedAsParticipant = Sets.newHashSet();
    Set<String> allUsersEmailsInRequest = Sets.newHashSet();
    
    if (adminEmailsInRequest != null) {
      log.info("Persisting users for experiment id:"+ expId + "with adminList size:"+adminEmailsInRequest.size());
      allUsersEmailsInRequest.addAll(adminEmailsInRequest);
    }
    if (participantEmailsInRequest != null) {
      log.info("Persisting users for experiment id:"+ expId + "with participantList size:"+participantEmailsInRequest.size());
      allUsersEmailsInRequest.addAll(participantEmailsInRequest);
    }
    
    // if same id is requested as admin and participant, admin takes precedence
    if (participantEmailsInRequest != null) {
      participantEmailsInRequest.removeAll(adminEmailsInRequest);
    }
    
    try {  
      // find all the user ids in user table for all emails (all admin and all participant) in request
      Map<String, Long> requestedEmailIdsInUserTable = getUserIdsForEmails(allUsersEmailsInRequest);
      
      // for all emails in request, insert email into user table if not present already and update map with the newly generated id
      for (String email : allUsersEmailsInRequest) {
        if (requestedEmailIdsInUserTable.get(email) == null) {
          Long genId = getUseridAndCreate(email, true).getId();
          requestedEmailIdsInUserTable.put(email, genId);
        }
      }
      
      // get all users associated with an expt, and identify adminIds and participant Ids  that are stored in database
      pacoUsersInDb = getAllUsersForExperiment(expId);
      Iterator<PacoUser> pacoUsrItr = pacoUsersInDb.iterator();
      while (pacoUsrItr.hasNext()) { 
        PacoUser crtUser = pacoUsrItr.next();
        if (crtUser != null && crtUser.getType().equals(ExperimentUserColumns.ADMIN_TYPE.charAt(0))) {
          adminIdsInDb.add(crtUser.getId());
        } else {
          participantIdsInDb.add(crtUser.getId());
        }
      }
      
      // get max of anon id for this list of paco users
      Integer maxAnonId = getMaxAnonId(pacoUsersInDb);
   // TODO Commented code needs to be removed. Checking it in, to review the commonality between the two blocks
      // For admin type
      identifyChangesToExperimentUserMappingForEachUserType(adminEmailsInRequest, requestedEmailIdsInUserTable, adminIdsInRequest, adminIdsInDb, participantIdsInDb, maxAnonId, idsToBeUpdatedAsAdmin, toBeInsertedIntoExptUserTable, ExperimentUserColumns.ADMIN_TYPE); 
      
//      if (adminEmailsInRequest != null) {
//        Iterator<String> adminItr = adminEmailsInRequest.iterator();
//        // for every admin Email in request
//        while (adminItr.hasNext()) {
//          String adminEmailInRequest = adminItr.next();
//          Long adminIdInRequest = requestedEmailIdsInUserTable.get(adminEmailInRequest);
//          adminIdsInRequest.add(adminIdInRequest);
//          // if admin id in request is not present as admin in db
//          if (!adminIdsInDb.contains(adminIdInRequest)) {
//            // if admin id in request is stored in db as participant. This means email is moved from participant status to admin status.
//            if (participantIdsInDb.contains(adminIdInRequest)) {
//              // update email in db as admin 
//              idsToBeUpdatedAsAdmin.add(adminIdInRequest);
//            } else {
//              // insert a new admin user
//              maxAnonId = maxAnonId + 1;
//              pu = new PacoUser(adminIdInRequest, maxAnonId, 'A', adminEmailInRequest);
//              toBeInsertedIntoExptUserTable.add(pu);  
//            }
//          }
//        }
//      }
      if (toBeInsertedIntoExptUserTable != null && toBeInsertedIntoExptUserTable.size() >= 1) {
        maxAnonId = getMaxAnonId(toBeInsertedIntoExptUserTable);
      }
      // For participant type
      identifyChangesToExperimentUserMappingForEachUserType(participantEmailsInRequest, requestedEmailIdsInUserTable, participantIdsInRequest, participantIdsInDb, adminIdsInDb, maxAnonId, idsToBeUpdatedAsParticipant, toBeInsertedIntoExptUserTable, ExperimentUserColumns.PARTICIPANT_TYPE); 
      
//      if (participantEmailsInRequest != null) {
//        Iterator<String> partItr = participantEmailsInRequest.iterator();
//        while (partItr.hasNext()) {
//          String partInRequest = partItr.next();
//          Long participantIdInRequest =  requestedEmailIdsInUserTable.get(partInRequest);
//          participantIdsInRequest.add(participantIdInRequest);
//          if (!participantIdsInDb.contains(participantIdInRequest)) {
//            // email is stored in db as admin, but now coming in as participant
//            if (adminIdsInDb.contains(participantIdInRequest)) {
//              idsToBeUpdatedAsParticipant.add(participantIdInRequest);
//            } else {
//              //plain new participant so, add a new paco user
//              maxAnonId = maxAnonId + 1;
//              pu = new PacoUser(participantIdInRequest, maxAnonId, 'P', partInRequest);
//              toBeInsertedIntoExptUserTable.add(pu);
//            }
//          }
//        }
//      }
      
      // find if admin ids in database has been removed from current request. This means 'not in admin request email list or in participant request email list'
      adminIdsInDb.removeAll(adminIdsInRequest);
      adminIdsInDb.removeAll(participantIdsInRequest);
      Iterator<Long> adminIdsRemovedItr = adminIdsInDb.iterator();
      // when ids have been removed as admin, then they should be treated as participants. Since we cannot lose the data we might have got until now with that id as admin
      while (adminIdsRemovedItr.hasNext()) {
        idsToBeUpdatedAsParticipant.add(adminIdsRemovedItr.next());
      }
      // new records to be inserted into experiment_user table
      insertIntoExperimentUsers(expId, toBeInsertedIntoExptUserTable);
      // old records that need modification in their user type. From admin to participant or vice versa
      updateUserTypesForExperiment(expId, idsToBeUpdatedAsAdmin, idsToBeUpdatedAsParticipant);
    } catch (SQLException sqle) {
      insertFailedEvent(expId.toString(), ErrorMessages.SQL_INSERT_EXCEPTION.getDescription() + "Admin/Participant", sqle.getMessage());
      log.warning(ErrorMessages.SQL_INSERT_EXCEPTION.getDescription() + " for  Admin/ Participant request: " + expId + " : " + ExceptionUtil.getStackTraceAsString(sqle));
    } catch (Exception e) {
      insertFailedEvent(expId.toString(), ErrorMessages.GENERAL_EXCEPTION.getDescription(), e.getMessage());
      log.warning(ErrorMessages.GENERAL_EXCEPTION.getDescription() + " for  Admin/ Participant request: " + expId + " : " + ExceptionUtil.getStackTraceAsString(e));
    }
  }
  
  private void identifyChangesToExperimentUserMappingForEachUserType(Set<String> type1EmailsInRequest,  Map<String, Long> requestedEmailIdsInUserTable, Set<Long> userIdsOfType1InRequest, Set<Long> type1IdsInDb, Set<Long> otherTypeIdsInDb, Integer maxAnonId, Set<Long> idsToBeUpdatedAsType1, List<PacoUser> toBeInsertedIntoExptUserTable, String statusTypeOfType1 ) {
    PacoUser pu = null;
    if (type1EmailsInRequest != null) {
      Iterator<String> type1EmailsInRequestItr = type1EmailsInRequest.iterator();
      // for every user Email of type1, Eg type 1 is Admin and other type is Participant
      while (type1EmailsInRequestItr.hasNext()) {
        String type1EmailInRequest = type1EmailsInRequestItr.next();
        Long type1IdInRequest = requestedEmailIdsInUserTable.get(type1EmailInRequest);
        userIdsOfType1InRequest.add(type1IdInRequest);
        // if admin id in request is not present as admin in db
        if (!type1IdsInDb.contains(type1IdInRequest)) {
          // if admin id in request is stored in db as participant. This means email is moved from participant status to admin status.
          if (otherTypeIdsInDb.contains(type1IdInRequest)) {
            // update email in db as admin 
            idsToBeUpdatedAsType1.add(type1IdInRequest);
          } else {
            // insert a new admin user
            maxAnonId = maxAnonId + 1;
            pu = new PacoUser(type1IdInRequest, maxAnonId, statusTypeOfType1.charAt(0), type1EmailInRequest);
            toBeInsertedIntoExptUserTable.add(pu);  
          }
        }
      }
    }
  }
  
  private Integer getMaxAnonId(Long expId) throws SQLException {
    List<PacoUser> userLst = getAllUsersForExperiment(expId);
    return getMaxAnonId(userLst);
  }
  
  private Integer getMaxAnonId(List<PacoUser> userLst) throws SQLException {
    Iterator<PacoUser> itr = userLst.iterator();
    int maxAnonId = 0;
    while (itr.hasNext()) { 
      PacoUser crtUser = itr.next();
      if (maxAnonId < crtUser.getAnonId()) {
        maxAnonId = crtUser.getAnonId();
      }
    }
    log.info("max anon id:" + maxAnonId);
    return maxAnonId;
  }

  private Integer getAnonymousId(Long experimentId, String email) throws SQLException {
    Connection conn = null;
    ResultSet rs = null;
    Integer anonId = null;
    PreparedStatement statementGetAnonId = null;
    if (experimentId != null && email != null) {
      log.info("get anonymous id with experiment id: "+ experimentId + " and email " + email);
     
      try {
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        setNames(conn);
        statementGetAnonId = conn.prepareStatement(QueryConstants.GET_ANON_ID_FOR_EMAIL.toString());
        statementGetAnonId.setLong(1, experimentId);
        statementGetAnonId.setString(2, email);
        rs = statementGetAnonId.executeQuery();
        if (rs.next()){
          anonId = rs.getInt(ExperimentUserColumns.EXP_USER_ANON_ID);
        }
      } finally {
        try {
          if ( rs != null) {
            rs.close();
          }
          if (statementGetAnonId != null) {
            statementGetAnonId.close();
          }
          if (conn != null) {
            conn.close();
          }
        } catch (SQLException ex1) {
          log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
        }
      }
    } else {
      log.info("get anonymous id with experiment id: "+ experimentId );
    }
    return anonId;
  }

  @Override
  public PacoId getUseridAndCreate(String email, boolean createOption) throws SQLException {
    PacoId userId = new PacoId();
    Set<String> userSet = Sets.newHashSet();
    userSet.add(email);
    Map<String, Long> singleUserMap = getUserIdsForEmails(userSet);
    Set<String> key = singleUserMap.keySet();
    Iterator<String> itr = key.iterator();
    if (itr.hasNext()) { 
      userId.setId(singleUserMap.get(itr.next()));
      userId.setIsCreatedWithThisCall(false);
    } else if (createOption) {
      userId.setId(insertUserAndRetrieveId(email));
      userId.setIsCreatedWithThisCall(true);
    } else {
      //TODO not sure if this is a good option to set to 0
      userId.setId(0L);
      userId.setIsCreatedWithThisCall(false);
    }
    return userId;
  }

  @Override
  public PacoId getAnonymousIdAndCreate(Long experimentId, String email, boolean createOption) throws SQLException{
    PacoId pacoAnonId = new PacoId();
    Integer anonId = getAnonymousId(experimentId, email);
    if (anonId != null) {
      pacoAnonId.setId(anonId.longValue());
      pacoAnonId.setIsCreatedWithThisCall(false);
    } else if (createOption) {
      insertUserForExperiment(experimentId, email);
      pacoAnonId.setId(getAnonymousId(experimentId, email).longValue());
      pacoAnonId.setIsCreatedWithThisCall(true);
    } else {
      //TODO not sure if this is a good option to set to 0
      pacoAnonId.setId(0L);
      pacoAnonId.setIsCreatedWithThisCall(false);
    }
    log.info("gawc: done"+ pacoAnonId.getId() + "--" + pacoAnonId.getIsCreatedWithThisCall());
    return pacoAnonId;
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
      setNames(conn);
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

package com.google.sampling.experiential.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.datastore.EventServerColumns;
import com.google.sampling.experiential.datastore.FailedEventServerColumns;
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
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

public class CloudSQLDaoImpl implements CloudSQLDao {
  public static final Logger log = Logger.getLogger(CloudSQLDaoImpl.class.getName());
  private static Map<String, Integer> eventsOutputColumns = null;
  private static List<Column> eventColList = Lists.newArrayList();
  public static final String WHEN = "when";
  private static List<Column> outputColList = Lists.newArrayList();
  private static List<Column> failedColList = Lists.newArrayList();

  static {
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

    outputColList.add(new Column(OutputBaseColumns.EVENT_ID));
    outputColList.add(new Column(OutputBaseColumns.NAME));
    outputColList.add(new Column(OutputBaseColumns.ANSWER));
    eventsOutputColumns = new HashMap<String, Integer>();

    for(int ct = 1; ct <= eventColList.size(); ct ++) {
      eventsOutputColumns.put(eventColList.get(ct-1).getColumnName(), ct);
    }
    for(int ct = 0; ct < outputColList.size(); ct ++) {
      eventsOutputColumns.put(outputColList.get(ct).getColumnName(), eventsOutputColumns.size() + 1);
    }

    failedColList.add(new Column(FailedEventServerColumns.EVENT_JSON));
    failedColList.add(new Column(FailedEventServerColumns.REASON));
    failedColList.add(new Column(FailedEventServerColumns.COMMENTS));
    failedColList.add(new Column(FailedEventServerColumns.REPROCESSED));
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
      eventInsert.setColumns(eventColList);
      outputInsert.setColumns(outputColList);
      // Adding ? for prepared stmt
      for (Column c : eventColList) {
        ((ExpressionList) eventInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      for (Column c : outputColList) {
        ((ExpressionList) outputInsert.getItemsList()).getExpressions().add(new JdbcParameter());
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
      event.setExperimentId(rs.getLong(EventServerColumns.EXPERIMENT_ID));
      event.setExperimentName(rs.getString(EventServerColumns.EXPERIMENT_NAME));
      event.setExperimentVersion(rs.getInt(EventServerColumns.EXPERIMENT_VERSION));

      Date scheduleDate = rs.getTimestamp(EventServerColumns.SCHEDULE_TIME);
      DateTime scheduledDateTime = scheduleDate != null ? new DateTime(scheduleDate): null;
      event.setScheduledTime(scheduledDateTime);

      Date responseDate = rs.getTimestamp(EventServerColumns.RESPONSE_TIME);
      DateTime responseDateTime = responseDate != null ? new DateTime(responseDate) : null;
      event.setResponseTime(responseDateTime);

      event.setExperimentGroupName(rs.getString(EventServerColumns.GROUP_NAME));
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

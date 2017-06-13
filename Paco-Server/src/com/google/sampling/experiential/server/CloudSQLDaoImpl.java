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

import org.joda.time.DateTimeZone;


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
  public static final String ID = "_id";
  private static List<Column> outputColList = Lists.newArrayList();
  private static List<Column> failedColList = Lists.newArrayList();
  private static final String selectOutputsSql = "select * from " + OutputBaseColumns.TABLE_NAME + " where " + OutputBaseColumns.EVENT_ID + " = ?";
  private static final String selectFailedEventsSql = "select * from " + FailedEventServerColumns.TABLE_NAME + " where " + FailedEventServerColumns.REPROCESSED + "='false'";
  private static final String updateFailedEventsSql = "update "+ FailedEventServerColumns.TABLE_NAME +" set "+ FailedEventServerColumns.REPROCESSED+ " = ? where " + FailedEventServerColumns.ID + "= ?";
  private static final String GET_EVENT_FOR_ID_QUERY = "select * from " + EventServerColumns.TABLE_NAME + " where " + Constants.UNDERSCORE_ID+ " =?";

  static {
    eventColList.add(new Column(EventServerColumns.EXPERIMENT_ID));
    eventColList.add(new Column(EventServerColumns.EXPERIMENT_NAME));
    eventColList.add(new Column(EventServerColumns.EXPERIMENT_VERSION));
    eventColList.add(new Column(EventServerColumns.SCHEDULE_TIME));
    eventColList.add(new Column(EventServerColumns.RESPONSE_TIME));
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
    eventColList.add(new Column(EventServerColumns.SORT_DATE));
    eventColList.add(new Column(EventServerColumns.CLIENT_TIME_ZONE));
    eventColList.add(new Column(Constants.UNDERSCORE_ID));
    
    outputColList.add(new Column(OutputBaseColumns.EVENT_ID));
    outputColList.add(new Column(OutputBaseColumns.NAME));
    outputColList.add(new Column(OutputBaseColumns.ANSWER));
    eventsOutputColumns = new HashMap<String, Integer>();
        
    for(int ct = 1; ct <= eventColList.size(); ct ++) {
      eventsOutputColumns.put(eventColList.get(ct-1).getColumnName(), ct);
    }
    for(int ct = 0; ct < outputColList.size(); ct ++) {
      eventsOutputColumns.put(outputColList.get(ct).getColumnName(), eventsOutputColumns.size() + ct);
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
      statementCreateEvent.setTimestamp(i++, event.getResponseTime()!= null ? new Timestamp(event.getResponseTime().getTime()): new Timestamp(event.getScheduledTime().getTime()));
      statementCreateEvent.setString(i++, event.getTimeZone());
      statementCreateEvent.setLong(i++, event.getId());
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
      statementCreateEvent.setTimestamp(i++, event.getResponseTime()!= null ? new Timestamp(event.getResponseTime().getTime()): new Timestamp(event.getScheduledTime().getTime()));
      statementCreateEvent.setString(i++, event.getTimeZone());
      statementCreateEvent.setLong(i++, event.getId());
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
    Connection conn = CloudSQLConnectionManager.getInstance().getConnection();
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
  }
  
  @Override
  public List<EventDAO> getEvents(Long eventId) throws SQLException, ParseException{
    return getEvents(GET_EVENT_FOR_ID_QUERY, null, eventId);
  }
 
  @Override
  public List<EventDAO> getEvents(String query, DateTimeZone tzForClient, Long eventId) throws SQLException, ParseException {
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
      if(eventId != null && query.contains("?")) {
        statementSelectEvent.setLong(1, eventId);
      } else {
        String selString = statementSelectEvent.toString();
        log.info("step 1 " + selString.substring(selString.indexOf(":")));
      }
      rs = statementSelectEvent.executeQuery();
      if (rs != null) {
        // to maintain the insertion order
        eventMap = Maps.newLinkedHashMap();
        while (rs.next()) {
          event = createEvent(rs);
          adjustTimeZone(event);
          EventDAO oldEvent = eventMap.get(event.getId());
          if (oldEvent == null) {
            // get all outputs for this event, and add to this event
            event.setWhat(getOutputs(event.getId()));
            eventMap.put(event.getId(), event);
          }
        }
      }
    } finally {
      try {
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
    if (eventMap != null) {
      evtDaoList = Lists.newArrayList(eventMap.values());
    }

    return evtDaoList;
  }
  
  @Override
  public List<WhatDAO> getOutputs(Long eventId) throws SQLException {
    List<WhatDAO> whatLst = Lists.newArrayList();
    WhatDAO whatObj = null;
    String question = null;
    String answer = null;
    Connection conn = null;
    PreparedStatement statementSelectOutput = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementSelectOutput = conn.prepareStatement(selectOutputsSql);
      statementSelectOutput.setLong(1, eventId);
      ResultSet rs = statementSelectOutput.executeQuery();
      while(rs.next()){
        question = rs.getString(OutputBaseColumns.NAME);
        answer = rs.getString(OutputBaseColumns.ANSWER);
        whatObj = new WhatDAO(question, answer);
        whatLst.add(whatObj);
      }
    } finally {
      try {
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

  private void adjustTimeZone(EventDAO event) throws ParseException {
    String tz = event.getTimezone();
    event.setScheduledTime(TimeUtil.convertToLocal(event.getScheduledTime(), tz));
    event.setResponseTime(TimeUtil.convertToLocal(event.getResponseTime(), tz));
  }

  private EventDAO createEvent(ResultSet rs) {
    EventDAO event = new EventDAO();
    List<WhatDAO> whatList = Lists.newArrayList();
    // setting an empty map for possible outputs. Even if the qry does not ask
    // for output fields, we send an empty output map
    event.setWhat(whatList);
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      for (int i = 1; i <= rsmd.getColumnCount(); i++) {
        String tempColNameInRS = rsmd.getColumnName(i);
        String colLower = tempColNameInRS.toLowerCase();
        Integer colIndex = eventsOutputColumns.get(colLower);
        if (colIndex != null) {
          switch (colIndex) {
          case 1:
            event.setExperimentId(rs.getLong(i));
            break;
          case 2:
            event.setExperimentName(rs.getString(i));
            break;
          case 3:
            event.setExperimentVersion(rs.getInt(i));
            break;
          case 4:
            event.setScheduledTime(rs.getTimestamp(i));
            break;
          case 5:
            event.setResponseTime(rs.getTimestamp(i));
            break;
          case 6:
            event.setExperimentGroupName(rs.getString(i));
            break;
          case 7:
            event.setActionTriggerId(rs.getLong(i));
            break;
          case 8:
            event.setActionTriggerSpecId(rs.getLong(i));
            break;
          case 9:
            event.setActionId(rs.getLong(i));
            break;
          case 10:
            event.setWho(rs.getString(i));
            break;
          case 11:
            event.setWhen(rs.getTimestamp(i));
            break;
          case 12:
            if( event.getWhen() != null) {
              long whTime = event.getWhen().getTime() ;
              event.setWhen(new Date(whTime + rs.getInt(i)));
            }
            break;
          case 13:
            event.setPaco_version(rs.getString(i));
            break;
          case 14:
            event.setAppId(rs.getString(i));
            break;
          case 15:
            event.setJoined(rs.getBoolean(i));
            break;
          case 16:
            event.setSortDate(rs.getTimestamp(i));
            break;
          case 17:
            event.setTimezone(rs.getString(i));
            break;
          case 18:
          case 19:
            event.setId(rs.getLong(i));
            break;
          case 20:
            List<WhatDAO> whTextLst = event.getWhat();
            whTextLst.add(new WhatDAO(OutputBaseColumns.NAME, rs.getString(i)));
            break;
          case 21:
            List<WhatDAO> whAnsLst = event.getWhat();
            whAnsLst.add(new WhatDAO(OutputBaseColumns.ANSWER, rs.getString(i)));
            break;
          }
        }
      }
    } catch (SQLException ex) {
      log.warning(ErrorMessages.SQL_EXCEPTION.getDescription() + ex);
    }
    return event;
  }
  
  public boolean setNames(Connection conn) throws SQLException { 
    boolean isDone = false;
    java.sql.Statement statementSetNames = null;
  
    try {
      statementSetNames = conn.createStatement();
      final String setNamesSql = "SET NAMES  'utf8mb4'";
      statementSetNames.execute(setNamesSql);
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
      log.info("Exception while inserting to failed events table" + failedJson);
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
      statementSelectFailedEvents = conn.prepareStatement(selectFailedEventsSql);
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
      statementUpdateFailedEvents = conn.prepareStatement(updateFailedEventsSql);
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

package com.google.sampling.experiential.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
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
  private static List<Column> outputColList = Lists.newArrayList();
  private static List<Column> failedColList = Lists.newArrayList();
  private static final String selectOutputsSql = "select * from outputs where event_id =?";

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
    eventColList.add(new Column(EventServerColumns.PACO_VERSION));
    eventColList.add(new Column(EventServerColumns.APP_ID));
    eventColList.add(new Column(EventServerColumns.JOINED));
    eventColList.add(new Column(EventServerColumns.SORT_DATE));
    eventColList.add(new Column(EventServerColumns.CLIENT_TIME_ZONE));
    eventColList.add(new Column(Constants.UNDERSCORE_ID));
    eventColList.add(new Column(EventServerColumns.INT_RESPONSE_TIME));
    
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
  }

  @Override
  public boolean insertEvent(Event event) throws SQLException, ParseException {
    if (event == null) {
      log.warning(ErrorMessages.NOT_VALID_DATA.getDescription());
      return false;
    }

    Connection conn = null;
    PreparedStatement statementCreateEvent = null;
    PreparedStatement statementCreateEventOutput = null;
    boolean retVal = false;
    //startCount for setting paramter index
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
      statementCreateEvent.setTimestamp(i++, event.getWhen() != null ? new Timestamp(event.getWhen().getTime()): null);
      statementCreateEvent.setString(i++, event.getPacoVersion());
      statementCreateEvent.setString(i++, event.getAppId());
      statementCreateEvent.setNull(i++, java.sql.Types.BOOLEAN);
      if (event.getWhat() != null) {
        String joinedStat = event.getWhatByKey(EventServerColumns.JOINED);
        if (joinedStat != null) {
          if (joinedStat.equalsIgnoreCase(Constants.TRUE)) {
            statementCreateEvent.setBoolean(i++, true);
          } else {
            statementCreateEvent.setBoolean(i++, false);
          }
        }
      }
      statementCreateEvent.setTimestamp(i++, event.getResponseTime()!= null ? new Timestamp(event.getResponseTime().getTime()): new Timestamp(event.getScheduledTime().getTime()));
      statementCreateEvent.setString(i++, event.getTimeZone());
      statementCreateEvent.setLong(i++, event.getId());
      statementCreateEvent.setLong(i++, event.getResponseTime() != null ? event.getResponseTime().getTime(): null);
      
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
  public List<EventDAO> getEvents(String query, DateTimeZone tzForClient) throws SQLException, ParseException {
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
      rs = statementSelectEvent.executeQuery();
      Long st2Time = System.currentTimeMillis();
      
      log.info("step 1 " + query + "took" + (st2Time- st1Time));
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
      // TODO step 2 sometimes takes 4 times longer to execute than the query.
      // Not sure why??
      log.info("step 2 took" + (System.currentTimeMillis() - st2Time));
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
  
  private boolean setNames(Connection conn) throws SQLException { 
    boolean isDone = false;
    java.sql.Statement statementSetNames = null;
  
    try {
      statementSetNames = conn.createStatement();
      final String setNamesSql = "SET NAMES  'utf8mb4'";
      statementSetNames.execute(setNamesSql);
      log.info("set names");

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
  
  private List<WhatDAO> getOutputs(Long eventId) throws SQLException {
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
            event.setPaco_version(rs.getString(i));
            break;
          case 13:
            event.setAppId(rs.getString(i));
            break;
          case 14:
            event.setJoined(rs.getBoolean(i));
            break;
          case 15:
            event.setSortDate(rs.getTimestamp(i));
            break;
          case 16:
            event.setTimezone(rs.getString(i));
            break;
          case 17:
          case 18:
            event.setId(rs.getLong(i));
            break;
          case 19:
            List<WhatDAO> whTextLst = event.getWhat();
            whTextLst.add(new WhatDAO(OutputBaseColumns.NAME, rs.getString(i)));
            break;
          case 20:
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

  @Override
  public String createTables() throws SQLException {
    String retString = null;
    Connection conn = null;
    PreparedStatement statementCreateEvent = null;
    PreparedStatement statementCreateOutput = null;
    PreparedStatement statementCreateFailedEvent = null;

    try {

      conn = CloudSQLConnectionManager.getInstance().getConnection();
      // TODO Sub Partition size for the experiment hash bucket
      final String createEventsTableSql = "CREATE TABLE `" + EventServerColumns.TABLE_NAME 
                                          + "` (" +"`" + Constants.UNDERSCORE_ID + "` bigint(20) NOT NULL ,"+ "`"
                                          + EventServerColumns.EXPERIMENT_ID + "` bigint(20) NOT NULL," + "`"
                                          + EventServerColumns.EXPERIMENT_NAME + "` varchar(45) DEFAULT NULL," + "`"
                                          + EventServerColumns.EXPERIMENT_VERSION + "` int(11) DEFAULT NULL," + "`"
                                          + EventServerColumns.SCHEDULE_TIME + "` datetime DEFAULT NULL," + "`"
                                          + EventServerColumns.RESPONSE_TIME + "` datetime DEFAULT NULL," + "`"
                                          + EventServerColumns.INT_RESPONSE_TIME + "` bigint(20) DEFAULT NULL," + "`"
                                          + EventServerColumns.GROUP_NAME + "` varchar(45) DEFAULT NULL," + "`"
                                          + EventServerColumns.ACTION_ID + "` bigint(20) DEFAULT NULL," + "`"
                                          + EventServerColumns.ACTION_TRIGGER_ID + "` bigint(20) DEFAULT NULL," + "`"
                                          + EventServerColumns.ACTION_TRIGGER_SPEC_ID + "` bigint(20) DEFAULT NULL," + "`"
                                          + EventServerColumns.WHO + "` varchar(45) NOT NULL," + "`"
                                          + EventServerColumns.PACO_VERSION + "` varchar(45) DEFAULT NULL," + "`"
                                          + EventServerColumns.APP_ID + "` varchar(45) DEFAULT NULL," 
                                          // when column already has the back tick
                                          + EventServerColumns.WHEN + " datetime DEFAULT NULL," + "`"
                                          + EventServerColumns.ARCHIVE_FLAG + "` tinyint(4) NOT NULL DEFAULT '0'," + "`"
                                          + EventServerColumns.JOINED + "` tinyint(1)  DEFAULT NULL," + "`"
                                          + EventServerColumns.SORT_DATE + "` datetime  DEFAULT NULL," + "`"
                                          + EventServerColumns.CLIENT_TIME_ZONE + "` varchar(20) DEFAULT NULL,"
                                          + "PRIMARY KEY (`" + Constants.UNDERSCORE_ID + "`)," + "KEY `when_index` ("
                                          + EventServerColumns.WHEN + ")," + "KEY `exp_id_resp_time_index` (`"
                                          + EventServerColumns.EXPERIMENT_ID + "`,`" + EventServerColumns.RESPONSE_TIME
                                          + "`)," + "KEY `exp_id_when_index` (`" + EventServerColumns.EXPERIMENT_ID
                                          + "`," + EventServerColumns.WHEN + ")," + "KEY `exp_id_who_when_index` (`"
                                          + EventServerColumns.EXPERIMENT_ID + "`,`" + EventServerColumns.WHO + "`,"
                                          + EventServerColumns.WHEN + ")" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

      final String createOutputsTableSql = "CREATE TABLE `" + OutputBaseColumns.TABLE_NAME+ "` (" + "`" 
                                           + OutputBaseColumns.EVENT_ID + "` bigint(20) NOT NULL," + "`"
                                           + OutputBaseColumns.NAME + "` varchar(500) NOT NULL," + "`"
                                           + OutputBaseColumns.ANSWER + "` varchar(500) DEFAULT NULL," + "`"
                                           + OutputBaseColumns.ARCHIVE_FLAG + "` tinyint(4) NOT NULL DEFAULT '0',"
                                           + "PRIMARY KEY (`" + OutputBaseColumns.EVENT_ID + "`,`"
                                           + OutputBaseColumns.NAME + "`)," + "KEY `event_id_index` (`"
                                           + OutputBaseColumns.EVENT_ID + "`)," + "KEY `text_index` (`"
                                           + OutputBaseColumns.NAME + "`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
      
      final String createFailedEventsTableSql = "CREATE TABLE `" +  FailedEventServerColumns.TABLE_NAME +  "` (" + "`" 
                                            + FailedEventServerColumns.ID + "` bigint(20) NOT NULL AUTO_INCREMENT," + "`"
                                            + FailedEventServerColumns.EVENT_JSON + "` varchar(3000) NOT NULL," + "`"
                                            + FailedEventServerColumns.FAILED_INSERT_TIME + "` datetime  DEFAULT NULL," + "`"
                                            + FailedEventServerColumns.REASON + "` varchar(500) DEFAULT NULL," + "`"
                                            + FailedEventServerColumns.COMMENTS + "` varchar(1000) DEFAULT NULL,"
                                            + "PRIMARY KEY (`" + FailedEventServerColumns.ID + "`)"+") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4";
     
      statementCreateEvent = conn.prepareStatement(createEventsTableSql);

      statementCreateEvent.execute();
      log.info("created events");
      // TODO better handling
      retString = "created events table. ";
      
      statementCreateOutput = conn.prepareStatement(createOutputsTableSql);
      statementCreateOutput.execute();
      log.info("created outputs");
      // TODO better handling
      retString = retString + "Created outputs table";
      
      statementCreateFailedEvent = conn.prepareStatement(createFailedEventsTableSql);
      statementCreateFailedEvent.execute();
      log.info("created failed events");
      // TODO better handling
      retString = retString + "Created FailedEvents table";
      
    } finally {
      try {
        if (statementCreateEvent != null) {
          statementCreateEvent.close();
        }
        if (statementCreateOutput != null) {
          statementCreateOutput.close();
        }
        if (statementCreateFailedEvent != null) {
          statementCreateFailedEvent.close();
        }
        if (conn != null) {
          conn.close();
        }

      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return retString;
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
      
      statementCreateFailedEvent.execute();
      conn.commit();
      retVal = true;
    } catch(SQLException sqle) { 
      log.info("Exception while inserting to failed events table" + failedJson);
      System.out.println("Exception while inserting to failed events table" + failedJson);
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
  
}

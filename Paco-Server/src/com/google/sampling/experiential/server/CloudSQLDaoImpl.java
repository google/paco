package com.google.sampling.experiential.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;

public class CloudSQLDaoImpl implements CloudSQLDao {
  public static final Logger log = Logger.getLogger(CloudSQLDaoImpl.class.getName());
  private static Map<String, Integer> eventsOutputColumns = null;

  private static void loadColumnTableAssociationMap() {
    if (eventsOutputColumns == null) {
      eventsOutputColumns = new HashMap<String, Integer>();
      eventsOutputColumns.put(EventBaseColumns.EXPERIMENT_ID, 1);
      eventsOutputColumns.put(EventBaseColumns.EXPERIMENT_NAME, 2);
      eventsOutputColumns.put(EventBaseColumns.EXPERIMENT_VERSION, 3);
      eventsOutputColumns.put(EventBaseColumns.SCHEDULE_TIME, 4);
      eventsOutputColumns.put(EventBaseColumns.RESPONSE_TIME, 5);
      eventsOutputColumns.put(EventBaseColumns.GROUP_NAME, 6);
      eventsOutputColumns.put(EventBaseColumns.ACTION_TRIGGER_ID, 7);
      eventsOutputColumns.put(EventBaseColumns.ACTION_TRIGGER_SPEC_ID, 8);
      eventsOutputColumns.put(EventBaseColumns.ACTION_ID, 9);
      eventsOutputColumns.put(EventBaseColumns.WHO, 10);
      eventsOutputColumns.put(EventBaseColumns.PACO_VERSION, 11);
      eventsOutputColumns.put(EventBaseColumns.APP_ID, 12);
      eventsOutputColumns.put("EVENTS._ID", 13);
      eventsOutputColumns.put("_ID", 14);
      eventsOutputColumns.put(OutputBaseColumns.NAME, 15);
      eventsOutputColumns.put(OutputBaseColumns.ANSWER, 16);
    }
  }

  @Override
  public boolean insertEvent(Event event) throws SQLException {
    Connection conn = null;
    PreparedStatement statementCreateEvent = null;
    PreparedStatement statementCreateEventOutput = null;

    if (event == null) {
      log.warning("nothing to insert");
      return false;
    }

    try {
      log.info("inserting event->" + event.getId());
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      String insertEventSql = "INSERT INTO events (" + "_ID," + EventBaseColumns.EXPERIMENT_ID + ","
                              + EventBaseColumns.EXPERIMENT_NAME + "," + EventBaseColumns.EXPERIMENT_VERSION + ","
                              + EventBaseColumns.RESPONSE_TIME + "," + EventBaseColumns.SCHEDULE_TIME + ","
                              + EventBaseColumns.GROUP_NAME + "," + EventBaseColumns.ACTION_ID + ","
                              + EventBaseColumns.ACTION_TRIGGER_ID + "," + EventBaseColumns.ACTION_TRIGGER_SPEC_ID + ","
                              + EventBaseColumns.WHO + ","
                              // Since when is a keyword in mysql, we should
                              // mark it with a back tick
                              + "`" + EventBaseColumns.WHEN + "`," + EventBaseColumns.PACO_VERSION + ","
                              + EventBaseColumns.APP_ID + ")" + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
      statementCreateEvent = conn.prepareStatement(insertEventSql);
      statementCreateEvent.setLong(1, event.getId() == null ? 0L : event.getId());
      statementCreateEvent.setString(2, event.getExperimentId() == null ? "" : event.getExperimentId());
      statementCreateEvent.setString(3, event.getExperimentName() == null ? "" : event.getExperimentName());
      statementCreateEvent.setInt(4, event.getExperimentVersion() == null ? 0 : event.getExperimentVersion());
      statementCreateEvent.setTimestamp(5,
                                        event.getResponseTime() == null ? null
                                                                        : new java.sql.Timestamp(event.getResponseTime()
                                                                                                      .getTime()));
      statementCreateEvent.setTimestamp(6,
                                        event.getScheduledTime() == null ? null
                                                                         : new java.sql.Timestamp(event.getScheduledTime()
                                                                                                       .getTime()));
      statementCreateEvent.setString(7, event.getExperimentGroupName() == null ? "" : event.getExperimentGroupName());
      statementCreateEvent.setLong(8, event.getActionId() == null ? 0L : event.getActionId());
      statementCreateEvent.setLong(9, event.getActionTriggerId() == null ? 0L : event.getActionTriggerId());
      statementCreateEvent.setLong(10, event.getActionTriggerSpecId() == null ? 0L : event.getActionTriggerSpecId());
      statementCreateEvent.setString(11, event.getWho());
      statementCreateEvent.setTimestamp(12,
                                        event.getWhen() == null ? null
                                                                : new java.sql.Timestamp(event.getWhen().getTime()));
      statementCreateEvent.setString(13, event.getPacoVersion());
      statementCreateEvent.setString(14, event.getAppId());

      statementCreateEvent.execute();

      String insertEventOutputsSql = "INSERT INTO outputs (" + OutputBaseColumns.EVENT_ID + "," + OutputBaseColumns.NAME
                                     + "," + OutputBaseColumns.ANSWER + ") VALUES (?, ?, ?)";
      statementCreateEventOutput = conn.prepareStatement(insertEventOutputsSql);
      for (String key : event.getWhatKeys()) {
        String whatAnswer = event.getWhatByKey(key);
        statementCreateEventOutput.setLong(1, event.getId() == null ? 0L : event.getId());
        statementCreateEventOutput.setString(2, key);
        statementCreateEventOutput.setString(3, whatAnswer == null ? "" : whatAnswer);
        statementCreateEventOutput.addBatch();
      }
      statementCreateEventOutput.executeBatch();
      conn.commit();
      return true;
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
        log.warning("sqlexception while inserting event close conn" + ex1);
      }
    }
  }

  @Override
  public List<EventDAO> getEvents(String query) throws SQLException {
    List<EventDAO> evtList = Lists.newArrayList();
    EventDAO event = null;
    Connection conn = null;
    Map<Long, EventDAO> eventMap = null;
    PreparedStatement statementSelectEvent = null;
    ResultSet rs = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementSelectEvent = conn.prepareStatement(query
       ,ResultSet.TYPE_FORWARD_ONLY,
       ResultSet.CONCUR_READ_ONLY
      );
       statementSelectEvent.setFetchSize(Integer.MIN_VALUE);
      Long stTime = System.nanoTime();
      rs = statementSelectEvent.executeQuery();

      log.info("step 1 query " + query + "took" + (System.nanoTime() - stTime));

      // to maintain the insertion order
      eventMap = Maps.newLinkedHashMap();
      Long dt = System.nanoTime();
      if (rs != null) {
        evtList = Lists.newArrayList();
        while (rs.next()) {
          event = createEvent(rs);
          EventDAO oldEvent = eventMap.get(event.getId());
          if (oldEvent == null) {
            eventMap.put(event.getId(), event);
          } else {
            // Will go through following when the query contains text in
            // ('a','b','v') or answer in(....)
            Map<String, String> oldOut = oldEvent.getWhat();
            Map<String, String> newOut = event.getWhat();
            // add all new output to the existing event in map
            oldOut.putAll(newOut);
          }
        }
      }
      // TODO step 2 sometimes takes 4 times longer to execute than the query.
      // Not sure why??
      log.info("step 2 proc took" + (System.nanoTime() - dt));
    } finally {
      try {
        if (statementSelectEvent != null) {
          statementSelectEvent.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning("sqlexception while inserting event close conn" + ex1);
      }
    }
    if (eventMap != null) {
      evtList = Lists.newArrayList(eventMap.values());
    }
    return evtList;
  }

  private EventDAO createEvent(ResultSet rs) {
    loadColumnTableAssociationMap();
    EventDAO e = new EventDAO();
    // setting an empty map for possible outputs. Even if the qry does not ask
    // for output fields, we send an empty output map
    e.setWhat(new HashMap<String, String>());
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      for (int i = 1; i <= rsmd.getColumnCount(); i++) {
        String tempColNameInRS = rsmd.getColumnName(i);
        String colUpper = tempColNameInRS.toUpperCase();
        Integer colIndex = eventsOutputColumns.get(colUpper);
        if (colIndex != null) {
          switch (colIndex) {
          case 1:
            e.setExperimentId(rs.getLong(i));
            break;
          case 2:
            e.setExperimentName(rs.getString(i));
            break;
          case 3:
            e.setExperimentVersion(rs.getInt(i));
            break;
          case 4:
            e.setScheduledTime(rs.getTimestamp(i));
            break;
          case 5:
            e.setResponseTime(rs.getTimestamp(i));
            break;
          case 6:
            e.setExperimentGroupName(rs.getString(i));
            break;
          case 7:
            e.setActionTriggerId(rs.getLong(i));
            break;
          case 8:
            e.setActionTriggerSpecId(rs.getLong(i));
            break;
          case 9:
            e.setActionId(rs.getLong(i));
            break;
          case 10:
            e.setWho(rs.getString(i));
            break;
          case 11:
            e.setPaco_version(rs.getString(i));
            break;
          case 12:
            e.setAppId(rs.getString(i));
            break;
          case 13:
          case 14:
            e.setId(rs.getLong(i));
            break;
          case 15:
            Map<String, String> hm = e.getWhat();
            hm.put(OutputBaseColumns.NAME, rs.getString(i));
            break;
          case 16:
            Map<String, String> hma = e.getWhat();
            hma.put(OutputBaseColumns.ANSWER, rs.getString(i));
            break;
          }
        }
      }
    } catch (SQLException ex) {
      log.warning("exception while mapping resultset to pojo" + ex);
    }
    return e;
  }

  @Override
  public String createTables() throws SQLException {
    String retString = null;
    Connection conn = null;
    PreparedStatement statementCreateEvent = null;
    PreparedStatement statementCreateOutput = null;

    try {

      conn = CloudSQLConnectionManager.getInstance().getConnection();
      // TODO Sub Partition size for the experiment hash bucket
      final String createEventsTableSql = "CREATE TABLE `events` (" +

                                          "`_id` bigint(20) NOT NULL," + "`" + EventBaseColumns.EXPERIMENT_ID
                                          + "` bigint(20) NOT NULL," + "`" + EventBaseColumns.EXPERIMENT_NAME
                                          + "` varchar(45) DEFAULT NULL," + "`" + EventBaseColumns.EXPERIMENT_VERSION
                                          + "` int(11) DEFAULT NULL," + "`" + EventBaseColumns.SCHEDULE_TIME
                                          + "` datetime DEFAULT NULL," + "`" + EventBaseColumns.RESPONSE_TIME
                                          + "` datetime DEFAULT NULL," + "`" + EventBaseColumns.GROUP_NAME
                                          + "` varchar(45) DEFAULT NULL," + "`" + EventBaseColumns.ACTION_ID
                                          + "` bigint(20) DEFAULT NULL," + "`" + EventBaseColumns.ACTION_TRIGGER_ID
                                          + "` bigint(20) DEFAULT NULL," + "`" + EventBaseColumns.ACTION_TRIGGER_SPEC_ID
                                          + "` bigint(20) DEFAULT NULL," + "`" + EventBaseColumns.WHO
                                          + "` varchar(45) DEFAULT NULL," + "`" + EventBaseColumns.PACO_VERSION
                                          + "` varchar(45) DEFAULT NULL," + "`" + EventBaseColumns.APP_ID
                                          + "` varchar(45) DEFAULT NULL," + "`" + EventBaseColumns.WHEN
                                          + "` datetime DEFAULT NULL," + "`" + EventBaseColumns.ARCHIVE_FLAG
                                          + "` tinyint(4) NOT NULL DEFAULT '0'," + "PRIMARY KEY (`_id`),"
                                          + "KEY `when_index` (`" + EventBaseColumns.WHEN + "`),"
                                          + "KEY `exp_id_resp_time_index` (`" + EventBaseColumns.EXPERIMENT_ID
                                          + "`,`response_time`)," + "KEY `exp_id_when_index` (`"
                                          + EventBaseColumns.EXPERIMENT_ID + "`,`when`),"
                                          + "KEY `exp_id_who_when_index` (`" + EventBaseColumns.EXPERIMENT_ID
                                          + "`,`who`,`when`)" + ") ENGINE=InnoDB DEFAULT CHARSET=latin1";

      final String createOutputsTableSql = "CREATE TABLE `outputs` (" + "`" + OutputBaseColumns.EVENT_ID
                                           + "` bigint(20) NOT NULL," + "`" + OutputBaseColumns.NAME
                                           + "` varchar(45) NOT NULL," + "`" + OutputBaseColumns.ANSWER
                                           + "` varchar(45) DEFAULT NULL," + "`" + OutputBaseColumns.ARCHIVE_FLAG
                                           + "` tinyint(4) NOT NULL DEFAULT '0'," + "PRIMARY KEY (`"
                                           + OutputBaseColumns.EVENT_ID + "`,`" + OutputBaseColumns.NAME + "`),"
                                           + "KEY `event_id_index` (`" + OutputBaseColumns.EVENT_ID + "`),"
                                           + "KEY `text_index` (`" + OutputBaseColumns.NAME + "`)"
                                           + ") ENGINE=InnoDB DEFAULT CHARSET=latin1";

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
    } finally {
      try {
        if (statementCreateEvent != null) {
          statementCreateEvent.close();
        }
        if (statementCreateOutput != null) {
          statementCreateOutput.close();
        }
        if (conn != null) {
          conn.close();
        }

      } catch (SQLException ex1) {
        log.warning("sqlexception while creating event close conn" + ex1);
      }
    }
    return retString;
  }
}
